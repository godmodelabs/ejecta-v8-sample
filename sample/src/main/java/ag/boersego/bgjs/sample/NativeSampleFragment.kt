package ag.boersego.bgjs.sample

import ag.boersego.bgjs.*
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.common.logger.LogView
import timber.log.Timber

/**
 * A simple sample of embedding a v8 instance in a native Android app in Java/Kotlin and then creating interfaces
 * between the JS and Java worlds. Embeds logging and a Text-to-Speech API.
 *
 * Created by Kevin Read <me@kevin-read.com> on 03.04.18 for ejecta-v8-sample.
 * Copyright (c) 2018 Kevin Read. All rights reserved.
 */
class NativeSampleFragment : Fragment() {

    // Our v8 instance
    private lateinit var v8Instance: V8Engine

    // Reference to ejecta console object so we can restore it when this Fragment gets destroyed
    private var oldConsoleObj: Any? = null

    /**
     * Accept a logging request from Javascript and log it to Timber
     */
    private class LogCallback(val logPriority: Int) : JNIV8Function.Handler {
        override fun Callback(jsThis: Any?, args: Array<out Any>?): Any {
            // An empty log is a no-op
            if (args == null || args.isEmpty() || args[0] is JNIV8Undefined) {
                return JNIV8Undefined.GetInstance()
            }

            // There are two possible formats for JS console log commands (see https://developer.mozilla.org/en-US/docs/Web/API/Console)
            // For this example we implement the simple and more widely used one that concatenates all arguments into a string
            val builder = StringBuilder()
            argLoop@ for (argument in args) {
                when (argument) {
                    is JNIV8Undefined, "" -> continue@argLoop
                    is Int, Float, Double, Long, String -> builder.append(argument)
                    is JNIV8Object -> builder.append(argument.toString())   // toString actually calls the JS string method
                    else -> builder.append(argument)
                }
                // Leave space between each argument
                builder.append(" ")
            }

            Timber.tag(TAG).log(logPriority, builder.toString())
            return JNIV8Undefined.GetInstance()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        v8Instance = V8Engine.getInstance(activity?.application)

        // Setup custom console object in JS, this will overwrite the built-in console commands
        // that only write to logcat
        // First, we create the object itself
        val consoleObj = JNIV8GenericObject.Create(v8Instance)

        // Add the commands we want to execute
        // console.log is equal to console.debug
        val debugLevelLog = JNIV8Function.Create(v8Instance, LogCallback(Log.DEBUG))
        consoleObj.setV8Field("log", debugLevelLog)
        consoleObj.setV8Field("debug", debugLevelLog)

        consoleObj.setV8Field("info", JNIV8Function.Create(v8Instance, LogCallback(Log.INFO)))
        consoleObj.setV8Field("warn", JNIV8Function.Create(v8Instance, LogCallback(Log.WARN)))
        consoleObj.setV8Field("error", JNIV8Function.Create(v8Instance, LogCallback(Log.ERROR)))

        // Then save a reference to the original version
        oldConsoleObj = v8Instance.globalObject.getV8Field("console")
        // And set out console object instead
        v8Instance.globalObject.setV8Field("console", consoleObj)
    }

    private var logView: LogView? = null

    // These are the references to the functions we export from JS
    private lateinit var sayMoreCB: JNIV8Function
    private lateinit var saySomethingCB: JNIV8Function
    private lateinit var logCB: JNIV8Function
    private lateinit var crashCB: JNIV8Function

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_native_sample, container, false)
        logView = layout.findViewById(R.id.log_view)

        // Plant our Timber tree so log messages are visible also in the UI
        Timber.plant(logView?.tree ?: throw RuntimeException("Logview could not be found"))
        // Register click handlers to the respective JS functions
        layout.findViewById<Button>(R.id.button_say_something).setOnClickListener({ saySomethingCB.callAsV8Function() })
        layout.findViewById<Button>(R.id.button_say_more).setOnClickListener({ sayMoreCB.callAsV8Function() })
        layout.findViewById<Button>(R.id.button_crash).setOnClickListener({
            // Catch the exception, no use in us crashing
            try {
                crashCB.callAsV8Function()
            } catch (tr: Exception) {
                Timber.tag(TAG).e(tr)
            } })
        layout.findViewById<Button>(R.id.button_log).setOnClickListener({ logCB.callAsV8Function() })

        // Execute the JS in the next tick as our logging setup is ready, it returns a standard JS object
        val scriptInstance = v8Instance.require("js/native_sample.js") as JNIV8GenericObject
        // From the returned export object, we retrieve the JS functions that were exported
        saySomethingCB = scriptInstance.getV8Field<JNIV8Function>("saySomething")
        sayMoreCB = scriptInstance.getV8Field<JNIV8Function>("sayMore")
        logCB = scriptInstance.getV8Field<JNIV8Function>("logSomething")
        crashCB = scriptInstance.getV8Field<JNIV8Function>("crash")
        return layout
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Timber.uproot(logView?.tree!!)

        // Tell v8 that we don't need the references to the bound functions anymore. This is optional but speeds up GC a little bit
        sayMoreCB.dispose()
        saySomethingCB.dispose()
        logCB.dispose()
        crashCB.dispose()

        logView = null
    }

    override fun onDestroy() {
        super.onDestroy()

        // Re-attach the original console object since we're using a shared v8 instance (== Isolate) here
        if (oldConsoleObj != null) {
            v8Instance.globalObject.setV8Field("console", oldConsoleObj)
        }
    }

    companion object {
        val TAG = NativeSampleFragment::class.java.simpleName!!
    }
}