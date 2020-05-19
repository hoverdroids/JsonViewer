package com.yuyh.jsonviewer

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.yuyh.jsonviewer.library.adapter.JsonViewerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private var isExpanded = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rv_json.depth = 3
        rv_json.setScaleEnable(true)
        rv_json.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {

            override fun onInterceptTouchEvent(rv: RecyclerView, event: MotionEvent): Boolean {
                when (event.action and event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> { }
                    MotionEvent.ACTION_UP -> { }
                    MotionEvent.ACTION_POINTER_UP -> hsv.requestDisallowInterceptTouchEvent(false)
                    MotionEvent.ACTION_POINTER_DOWN -> hsv.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_MOVE -> { }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
        Thread(Runnable {
            var `is`: InputStream? = null
            try {
                `is` = assets.open("demo_smaller.json")
                val lenght = `is`.available()
                val buffer = ByteArray(lenght)
                `is`.read(buffer)
                val result = String(buffer, Charsets.UTF_8)
                `is`.close()
                runOnUiThread { rv_json.bindJson(result) }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()

        val toggle = findViewById<View>(R.id.toggle)
        toggle.setOnClickListener {
            if (isExpanded) {
                rv_json.collapseAll()
            } else {
                rv_json.expandAllToDepth(2)
            }
            isExpanded = !isExpanded
        }
    }
}