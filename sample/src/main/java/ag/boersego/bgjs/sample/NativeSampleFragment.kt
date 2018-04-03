package ag.boersego.bgjs.sample

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

/**
 * Created by Kevin Read <me@kevin-read.com> on 03.04.18 for ejecta-v8-sample.
 * Copyright (c) 2018 ${ORGANIZATION_NAME}. All rights reserved.
 */
class NativeSampleFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layout = inflater.inflate(R.layout.fragment_native_sample, container, false)
        layout.findViewById<Button>(R.id.button_allocate_simple).setOnClickListener(View.OnClickListener {  })
    }
}