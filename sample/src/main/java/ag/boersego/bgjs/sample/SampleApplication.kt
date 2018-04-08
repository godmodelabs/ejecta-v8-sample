@file:Suppress("unused")

package ag.boersego.bgjs.sample

import ag.boersego.bgjs.V8Engine
import ag.boersego.bgjs.sample.modules.TTSModule
import android.app.Application

/**
 * Created by Kevin Read <me@kevin-read.com> on 08.04.18 for ejecta-v8-sample.
 * Copyright (c) 2018 ${ORGANIZATION_NAME}. All rights reserved.
 */
class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Register one instance of the TTS module
        // Note that this is not the ideal way since we're blocking app startup here slightly.
        V8Engine.getInstance(this).registerModule(TTSModule(this))
    }
}