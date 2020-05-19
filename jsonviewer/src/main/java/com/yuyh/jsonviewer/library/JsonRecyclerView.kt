package com.yuyh.jsonviewer.library

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yuyh.jsonviewer.library.adapter.JsonItemViewHolder
import com.yuyh.jsonviewer.library.adapter.JsonViewerAdapter
import com.yuyh.jsonviewer.library.view.JsonItemView
import org.json.JSONArray
import org.json.JSONObject

class JsonRecyclerView constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : RecyclerView(context, attrs, defStyle) {

    private var mAdapter: JsonViewerAdapter? = null

    private fun initView() {
        layoutManager = LinearLayoutManager(context)
    }

    fun bindJson(json: String) {
        mAdapter = null
        mAdapter = JsonViewerAdapter(json)
        adapter = mAdapter
    }

    fun bindJson(jsonArray: JSONArray) {
        mAdapter = null
        mAdapter = JsonViewerAdapter(jsonArray)
        adapter = mAdapter
    }

    fun bindJson(jsonObject: JSONObject) {
        mAdapter = null
        mAdapter = JsonViewerAdapter(jsonObject)
        adapter = mAdapter
    }

    fun setKeyColor(color: Int) {
        mAdapter?.keyColor = color
    }

    fun setValueTextColor(color: Int) {
        mAdapter?.textColor = color
    }

    fun setValueNumberColor(color: Int) {
        mAdapter?.numberColor = color
    }

    fun setValueBooleanColor(color: Int) {
        mAdapter?.booleanColor = color
    }

    fun setValueUrlColor(color: Int) {
        mAdapter?.urlColor = color
    }

    fun setValueNullColor(color: Int) {
        mAdapter?.numberColor = color
    }

    fun setBracesColor(color: Int) {
        mAdapter?.bracesColor = color
    }

    fun setTextSize(sizeDP: Float) {
        var sizeDP = sizeDP
        if (sizeDP < 10) {
            sizeDP = 10f
        } else if (sizeDP > 30) {
            sizeDP = 30f
        }
        if (mAdapter?.textSizeDp != sizeDP) {
            mAdapter?.textSizeDp = sizeDP
            if (mAdapter != null) {
                updateAll(sizeDP)
            }
        }
    }

    fun setScaleEnable(enable: Boolean) {
        if (enable) {
            addOnItemTouchListener(touchListener)
        } else {
            removeOnItemTouchListener(touchListener)
        }
    }

    fun updateAll(textSize: Float) {
        val manager = layoutManager
        val count = manager!!.childCount
        for (i in 0 until count) {
            val view = manager.getChildAt(i)
            loop(view, textSize)
        }
    }

    private fun loop(view: View?, textSize: Float) {
        if (view is JsonItemView) {
            val group = view
            group.setTextSize(textSize)
            val childCount = group.childCount
            for (i in 0 until childCount) {
                val view1 = group.getChildAt(i)
                loop(view1, textSize)
            }
        }
    }

    var mode = 0
    var oldDist = 0f
    private fun zoom(f: Float) {
        mAdapter?.textSizeDp?.let { setTextSize(it) }
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt(x * x + y * y.toDouble()).toFloat()
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
        val adapter = adapter as JsonViewerAdapter?
        adapter!!.depth = depth //Just in case new items are displayed, this shows them at the correct depth
        val childCount = childCount
        var i = 0
        while (i < childCount) {
            val holder = getChildViewHolder(getChildAt(i)) as JsonItemViewHolder
            if (holder.jsonItemView.isCollapsed) {
                adapter.expandToDepth(holder.jsonItemView, depth) //this shows the currently displayed items at the correct depth
            }
            ++i
        }
    }

    fun collapseAll() {
        val adapter = adapter as JsonViewerAdapter?
        adapter!!.depth = 0 //Just in case new items are displayed, this shows them at the correct depth
        val childCount = childCount
        var i = 0
        while (i < childCount) {
            val holder = getChildViewHolder(getChildAt(i)) as JsonItemViewHolder
            if (!holder.jsonItemView.isCollapsed) {
                adapter.toggleExpandCollapse(holder.jsonItemView) //this shows the currently displayed items at the correct depth
            }
            ++i
        }
    }

    init {
        initView()
    }
}