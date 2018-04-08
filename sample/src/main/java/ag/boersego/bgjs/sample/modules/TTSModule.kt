package ag.boersego.bgjs.sample.modules

import ag.boersego.bgjs.JNIV8Function
import ag.boersego.bgjs.JNIV8GenericObject
import ag.boersego.bgjs.JNIV8Module
import ag.boersego.bgjs.V8Engine
import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger

/**
 * This is a simple V8 module that wraps Google's Text-To-Speech API on Android.
 *
 * Created by Kevin Read <me@kevin-read.com> on 08.04.18 for ejecta-v8-sample.
 * Copyright (c) 2018 Kevin Read. All rights reserved.
 */


class TTSModule(applicationContext: Context) : JNIV8Module("tts"), TextToSpeech.OnInitListener {
    private var initStatus: Int = -1

    override fun onInit(status: Int) {
        initStatus = status
        ttsInstance.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
            override fun onError(utteranceId: String?) {
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId != null) {
                    val callbackWeak = utteranceTracker[utteranceId]
                    val callback = callbackWeak?.get()
                    callback?.callAsV8Function("done")

                    // Tell v8 that we do not need this persistent function reference anymore
                    callback?.dispose()
                    // And clear the weak ref and the map entry
                    callbackWeak?.clear()
                    utteranceTracker.remove(utteranceId)
                }
            }

            override fun onStart(utteranceId: String?) {
                val callback = utteranceTracker[utteranceId]
                callback?.get()?.callAsV8Function("start")
            }

        })
    }

    private val utteranceTracker: MutableMap<String, WeakReference<JNIV8Function>> = hashMapOf()

    private val ttsInstance = TextToSpeech(applicationContext, this)

    private val nextUtteranceId = AtomicInteger(1)

    override fun Require(engine: V8Engine, module: JNIV8GenericObject?) {
        // As per the CommonJS standard, create the exports
        val exports = JNIV8GenericObject.Create(engine)

        exports.setV8Field("speak", JNIV8Function.Create(engine, { _, args ->
            if (args == null || args.isEmpty() || args[0] !is String) {
                throw RuntimeException("First argument needs to be String to speak")
            }
            if (args.size > 1 && args[1] !is JNIV8Function) {
                throw RuntimeException("Second argument is optional completion callback and must be a function")
            }

            // We return bool for success
            if (initStatus != TextToSpeech.SUCCESS) {
                false
            } else {
                // If the caller wanted feedback on the progress of the utterance, we need to set the listener
                val callback = args[1] as JNIV8Function
                val utteranceId = nextUtteranceId.incrementAndGet().toString()
                utteranceTracker[utteranceId] = WeakReference(callback)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ttsInstance.speak(args[0] as String, TextToSpeech.QUEUE_ADD, null, utteranceId)
                } else {
                    val params = hashMapOf(Pair(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId))
                    @Suppress("DEPRECATION")
                    ttsInstance.speak(args[0] as String, TextToSpeech.QUEUE_ADD, params)
                }
                true
            }
        }))

        module?.setV8Field("exports", exports)
    }
}