package com.yuyh.jsonviewer.library.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.yuyh.jsonviewer.library.R
import com.yuyh.jsonviewer.library.bracesColor
import com.yuyh.jsonviewer.library.databinding.JsonItemViewBinding
import org.json.JSONArray
import kotlinx.android.synthetic.main.json_item_view.view.*

class JsonItemView @JvmOverloads constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(mContext, attrs, defStyleAttr) {
    
    var textSizeDp = 12f
        set(value) {
            field = when {
                value < 12 -> 12f
                value > 30 -> 30f
                else -> value
            }

            tv_left.textSize = field
            tv_right.textSize = field
            tv_right.setTextColor(bracesColor)

            // align the vertically expand/collapse icon to the text
            val textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, field, resources.displayMetrics).toInt()
            val layoutParams = iv_icon.layoutParams as LayoutParams
            layoutParams.height = textSize
            layoutParams.width = textSize
            layoutParams.topMargin = textSize / 5
            iv_icon.layoutParams = layoutParams
        }

    var value: Any? = null
        set(value) {
            field = value
            isJsonArray = value is JSONArray
        }

    var appendComma = false
    var hierarchy = 0
    var isCollapsed = true
    var isJsonArray = false

    init {
        orientation = VERTICAL
        JsonItemViewBinding.inflate(LayoutInflater.from(mContext), this, true)
    }

    fun setRightColor(color: Int) {
        tv_right.setTextColor(color)
    }

    fun hideLeft() {
        tv_left.visibility = View.GONE
    }

    fun showLeft(text: CharSequence?) {
        tv_left.visibility = View.VISIBLE
        if (text != null) {
            tv_left.text = text
        }
    }

    fun hideRight() {
        tv_right.visibility = View.GONE
    }

    fun showRight(text: CharSequence?) {
        tv_right.visibility = View.VISIBLE
        if (text != null) {
            tv_right.text = text
        }
    }

    val rightText: CharSequence
        get() = tv_right.text

    fun hideIcon() {
        iv_icon.visibility = View.GONE
    }

    fun showIcon(isPlus: Boolean) {
        iv_icon.visibility = View.VISIBLE
        iv_icon.setImageResource(if (isPlus) R.drawable.jsonviewer_plus else R.drawable.jsonviewer_minus)
        iv_icon.contentDescription = resources.getString(if (isPlus) R.string.jsonViewer_icon_plus else R.string.jsonViewer_icon_minus)
    }

    //If anything in this row is clicked, let's expand/collapse because it's too hard to just
    //click the +/- button
    override fun setOnClickListener(listener: OnClickListener) {
        iv_icon.setOnClickListener(listener)
        tv_left.setOnClickListener(listener)
        tv_right.setOnClickListener(listener)
        super.setOnClickListener(listener)
    }

    fun addViewNoInvalidate(child: View) {
        var params = child.layoutParams
        if (params == null) {
            params = generateDefaultLayoutParams()
            requireNotNull(params) { "generateDefaultLayoutParams() cannot return null" }
        }
        addViewInLayout(child, -1, params)
    }
}