package com.yuyh.jsonviewer.library

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yuyh.jsonviewer.library.adapter.*
import com.yuyh.jsonviewer.library.view.JsonItemView
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.sqrt

class JsonRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var mAdapter: JsonViewerAdapter? = null
    var depth = 0
        set(value) {
            field = value
            mAdapter?.let { it.depth = value }
        }

    init {
        layoutManager = LinearLayoutManager(context)
    }

    fun bindJson(json: String) {
        mAdapter = null
        mAdapter = JsonViewerAdapter(json)
        mAdapter?.let { it.depth = depth }
        adapter = mAdapter
    }

    fun bindJson(jsonArray: JSONArray) {
        mAdapter = null
        mAdapter = JsonViewerAdapter(jsonArray)
        mAdapter?.let { it.depth = depth }
        adapter = mAdapter
    }

    fun bindJson(jsonObject: JSONObject) {
        mAdapter = null
        mAdapter = JsonViewerAdapter(jsonObject)
        mAdapter?.let { it.depth = depth }
        adapter = mAdapter
    }

    fun setKeyColor(color: Int) {
        keyColor = color
    }

    fun setValueTextColor(color: Int) {
        textColor = color
    }

    fun setValueNumberColor(color: Int) {
        numberColor = color
    }

    fun setValueBooleanColor(color: Int) {
        booleanColor = color
    }

    fun setValueUrlColor(color: Int) {
        urlColor = color
    }

    fun setValueNullColor(color: Int) {
        numberColor = color
    }

    fun setBracesColor(color: Int) {
        bracesColor = color
    }

    fun setTextSize(sizeDP: Float) {
        val boundedSize = when {
            sizeDP < 10 -> 10f
            sizeDP > 30 -> 30f
            else -> sizeDP
        }

        if (textSizeDp != boundedSize) {
            textSizeDp = boundedSize
            mAdapter?.let{ updateAll(boundedSize) }
        }
    }

    private fun updateAll(textSize: Float) {
        val count = layoutManager?.childCount ?: 0
        for (i in 0 until count) {
            val view = layoutManager?.getChildAt(i)
            loop(view, textSize)
        }
    }

    private fun loop(view: View?, textSize: Float) {
        if (view is JsonItemView) {
            view.textSizeDp = textSize
            val childCount = view.childCount
            for (i in 0 until childCount) {
                val view1 = view.getChildAt(i)
                loop(view1, textSize)
            }
        }
    }

    fun setScaleEnable(enable: Boolean) {
        when(enable){
            true -> addOnItemTouchListener(touchListener)
            else -> removeOnItemTouchListener(touchListener)
        }
    }

    var mode = 0
    var oldDist = 0f
    private fun zoom(f: Float) {
        setTextSize(textSizeDp * f)
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y.toDouble()).toFloat()
    }

    private val touchListener: OnItemTouchListener = object : OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, event: MotionEvent): Boolean {
            when (event.action and event.actionMasked) {
                MotionEvent.ACTION_DOWN -> mode = 1
                MotionEvent.ACTION_UP -> mode = 0
                MotionEvent.ACTION_POINTER_UP -> mode -= 1
                MotionEvent.ACTION_POINTER_DOWN -> {
                    oldDist = spacing(event)
                    mode += 1
                }
                MotionEvent.ACTION_MOVE -> if (mode >= 2) {
                    val newDist = spacing(event)
                    if (Math.abs(newDist - oldDist) > 0.5f) {
                        zoom(newDist / oldDist)
                        oldDist = newDist
                    }
                }
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, event: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }

    fun expandAllToDepth(depth: Int) {
        mAdapter?.let {
            it.depth = depth //Just in case new items are displayed, this shows them at the correct depth
            val childCount = childCount
            var i = 0
            while (i < childCount) {
                val holder = getChildViewHolder(getChildAt(i)) as JsonItemViewHolder
                if (holder.jsonItemView.isCollapsed) {
                    it.expandToDepth(holder.jsonItemView, depth) //this shows the currently displayed items at the correct depth
                }
                ++i
            }
        }
    }

    fun collapseAll() {
        mAdapter?.let {
            it.depth = 0 //Just in case new items are displayed, this shows them at the correct depth
            val childCount = childCount
            var i = 0
            while (i < childCount) {
                val holder = getChildViewHolder(getChildAt(i)) as JsonItemViewHolder
                if (!holder.jsonItemView.isCollapsed) {
                    it.toggleExpandCollapse(holder.jsonItemView) //this shows the currently displayed items at the correct depth
                }
                ++i
            }
        }
    }
}