package com.yuyh.jsonviewer.library.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.yuyh.jsonviewer.library.R
import com.yuyh.jsonviewer.library.bracesColor
import org.json.JSONArray

class JsonItemView @JvmOverloads constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(mContext, attrs, defStyleAttr) {

    private lateinit var mTvLeft: TextView
    private lateinit var mTvRight: TextView
    private lateinit var mIvIcon: ImageView

    var textSizeDp = 12f
        set(value) {
            field = when {
                value < 12 -> 12f
                value > 30 -> 30f
                else -> value
            }

            mTvLeft.textSize = field
            mTvRight.textSize = field
            mTvRight.setTextColor(bracesColor)

            // align the vertically expand/collapse icon to the text
            val textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, field, resources.displayMetrics).toInt()
            val layoutParams = mIvIcon.layoutParams as LayoutParams
            layoutParams.height = textSize
            layoutParams.width = textSize
            layoutParams.topMargin = textSize / 5
            mIvIcon.layoutParams = layoutParams
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
        LayoutInflater.from(mContext).inflate(R.layout.jsonviewer_layout_item_view, this, true)
        mTvLeft = findViewById(R.id.tv_left)
        mTvRight = findViewById(R.id.tv_right)
        mIvIcon = findViewById(R.id.iv_icon)
    }

    fun setRightColor(color: Int) {
        mTvRight.setTextColor(color)
    }

    fun hideLeft() {
        mTvLeft.visibility = View.GONE
    }

    fun showLeft(text: CharSequence?) {
        mTvLeft.visibility = View.VISIBLE
        if (text != null) {
            mTvLeft.text = text
        }
    }

    fun hideRight() {
        mTvRight.visibility = View.GONE
    }

    fun showRight(text: CharSequence?) {
        mTvRight.visibility = View.VISIBLE
        if (text != null) {
            mTvRight.text = text
        }
    }

    val rightText: CharSequence
        get() = mTvRight.text

    fun hideIcon() {
        mIvIcon.visibility = View.GONE
    }

    fun showIcon(isPlus: Boolean) {
        mIvIcon.visibility = View.VISIBLE
        mIvIcon.setImageResource(if (isPlus) R.drawable.jsonviewer_plus else R.drawable.jsonviewer_minus)
        mIvIcon.contentDescription = resources.getString(if (isPlus) R.string.jsonViewer_icon_plus else R.string.jsonViewer_icon_minus)
    }

    //If anything in this row is clicked, let's expand/collapse because it's too hard to just
    //click the +/- button
    override fun setOnClickListener(listener: OnClickListener) {
        mIvIcon.setOnClickListener(listener)
        mTvLeft.setOnClickListener(listener)
        mTvRight.setOnClickListener(listener)
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