package ag.boersego.bgjs.sample

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import java.util.*

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 */
internal object Samples {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<SampleItem> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    var ITEM_MAP: MutableMap<Int, SampleItem> = HashMap()

    fun getItem(position: Int): SampleItem {
        return ITEMS[position]
    }

    init {
        // Add 3 sample items.
        addItem(SampleItem(1, "Run in Ejecta-v8 View", {
            val fragment = DemoEjectaFragment()
            val arguments = Bundle()
            arguments.putString(DemoEjectaFragment.ARG_ITEM_ID, "demo.html")
            fragment.arguments = arguments
            fragment
        }))
        addItem(SampleItem(2, "Run in WebView", {
            val fragment = DemoWebviewFragment()
            val arguments = Bundle()
            arguments.putString(DemoEjectaFragment.ARG_ITEM_ID, "demo.html")
            fragment.arguments = arguments
            fragment
        }))
        addItem(SampleItem(3, "Run plasma in external browser", { ctx ->
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("https://godmodelabs.github.io/ejecta-v8-sample/demo.html")
            ctx.startActivity(i)
            null
        }))

        addItem(SampleItem(4, "Native integration sample", { NativeSampleFragment()  }))
    }

    private fun addItem(item: SampleItem) {
        ITEMS.add(item)
        ITEM_MAP[item.id] = item
    }

    /**
     * A dummy item representing a piece of content.
     */
    class SampleItem(val id: Int, val title: String, val callback: ((Context) -> Fragment?)) {

        override fun toString(): String {
            return title
        }
    }
}
