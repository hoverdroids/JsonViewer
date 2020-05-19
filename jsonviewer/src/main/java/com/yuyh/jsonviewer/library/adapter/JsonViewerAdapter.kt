package com.yuyh.jsonviewer.library.adapter

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yuyh.jsonviewer.library.*
import com.yuyh.jsonviewer.library.utils.Utils
import com.yuyh.jsonviewer.library.view.JsonItemView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.util.*

class JsonViewerAdapter : RecyclerView.Adapter<JsonItemViewHolder>, View.OnClickListener {
    private var jsonStr: String? = null
    private var mJSONObject: JSONObject? = null
    private var mJSONArray: JSONArray? = null
    var depth = 0

    constructor(jsonStr: String?) {
        this.jsonStr = jsonStr
        var `object`: Any? = null
        try {
            `object` = JSONTokener(jsonStr).nextValue()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        if (`object` is JSONObject) {
            mJSONObject = `object`
        } else if (`object` is JSONArray) {
            mJSONArray = `object`
        } else {
            throw IllegalArgumentException("jsonStr is illegal.")
        }
    }

    constructor(jsonObject: JSONObject?) {
        mJSONObject = jsonObject
        requireNotNull(mJSONObject) { "jsonObject can not be null." }
    }

    constructor(jsonArray: JSONArray?) {
        mJSONArray = jsonArray
        requireNotNull(mJSONArray) { "jsonArray can not be null." }
    }

    //This creates a viewHolder for each top-level JSON object. The viewHolder is not recyclable,
    //and hence each top-level JSON object has its own unique viewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JsonItemViewHolder {
        return JsonItemViewHolder(JsonItemView(parent.context))
    }

    //A viewHolder is created anew each time one of the top-level JSON objects is about to be displayed
    override fun onBindViewHolder(holder: JsonItemViewHolder, position: Int) {

        //Create the top-level view for the top-level JSON object
        val itemView: JsonItemView = holder.jsonItemView
        itemView.textSizeDp = textSizeDp
        itemView.setRightColor(bracesColor)

        //The top level can't be both an Object and Array. So, only one of the following does
        //anything for any given JSON string
        handleTopLevelJsonObject(mJSONObject, itemView, position)
        handleTopLevelJsonArray(mJSONArray, itemView, position)

        //Automatically get the children and show them as expanded or collapsed based on depth
        expandToDepth(itemView, depth)
    }

    override fun getItemCount(): Int {
        //The initial count is the total number of top-level JSON objects plus two for curly brakets
        var count = 0
        if (mJSONObject != null) {
            count = if (mJSONObject!!.names() != null) {
                mJSONObject!!.names().length() + 2
            } else {
                2
            }
        } else if (mJSONArray != null) {
            count = mJSONArray!!.length() + 2
        }
        return count
    }

    private fun handleTopLevelJsonObject(jsonObject: JSONObject?, itemView: JsonItemView, position: Int) {
        if (jsonObject == null) return
        if (position == 0) {
            //First item, prefix with opening curly
            itemView.hideLeft()
            itemView.hideIcon()
            itemView.showRight("{")
            return
        } else if (position == itemCount - 1) {
            //last item, suffix with closing curly
            itemView.hideLeft()
            itemView.hideIcon()
            itemView.showRight("}")
            return
        } else if (jsonObject.names() == null) {
            return
        }

        //Update the itemView with the Json data
        val key = jsonObject.names().optString(position - 1)
        val value = jsonObject.opt(key)
        if (position < itemCount - 2) {
            handleJsonObject(key, value, itemView, true, 1)
        } else {
            handleJsonObject(key, value, itemView, false, 1) // 最后一组，结尾不需要逗号
        }
    }

    private fun handleTopLevelJsonArray(jsonArray: JSONArray?, itemView: JsonItemView, position: Int) {
        if (jsonArray == null) return
        if (position == 0) {
            itemView.hideLeft()
            itemView.hideIcon()
            itemView.showRight("[")
            return
        } else if (position == itemCount - 1) {
            itemView.hideLeft()
            itemView.hideIcon()
            itemView.showRight("]")
            return
        }
        val value = jsonArray.opt(position - 1)
        if (position < itemCount - 2) {
            handleJsonArray(value, itemView, true, 1)
        } else {
            handleJsonArray(value, itemView, false, 1)
        }
    }

    private fun handleJsonObject(key: String, value: Any, itemView: JsonItemView, appendComma: Boolean, hierarchy: Int) {
        val keyBuilder = SpannableStringBuilder(Utils.getHierarchyStr(hierarchy))
        keyBuilder.append("\"").append(key).append("\"").append(":")
        keyBuilder.setSpan(ForegroundColorSpan(keyColor), 0, keyBuilder.length - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        keyBuilder.setSpan(ForegroundColorSpan(bracesColor), keyBuilder.length - 1, keyBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        itemView.showLeft(keyBuilder)
        handleValue(value, itemView, appendComma, hierarchy)
    }

    private fun handleJsonArray(value: Any, itemView: JsonItemView, appendComma: Boolean, hierarchy: Int) {
        itemView.showLeft(SpannableStringBuilder(Utils.getHierarchyStr(hierarchy)))
        handleValue(value, itemView, appendComma, hierarchy)
    }

    private fun handleValue(value: Any?, itemView: JsonItemView, appendComma: Boolean, hierarchy: Int) {
        val valueBuilder = SpannableStringBuilder()
        if (value is Number) {
            valueBuilder.append(value.toString())
            valueBuilder.setSpan(ForegroundColorSpan(numberColor), 0, valueBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        } else if (value is Boolean) {
            valueBuilder.append(value.toString())
            valueBuilder.setSpan(ForegroundColorSpan(booleanColor), 0, valueBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        } else if (value is JSONObject) {
            valueBuilder.append("Object{...}")
            valueBuilder.setSpan(ForegroundColorSpan(bracesColor), 0, valueBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            itemView.showIcon(true)
            itemView.value = value
            itemView.appendComma = appendComma
            itemView.hierarchy = hierarchy + 1
            itemView.setOnClickListener(this)

        } else if (value is JSONArray) {
            valueBuilder.append("Array[").append(value.length().toString()).append("]")
            val len = valueBuilder.length
            valueBuilder.setSpan(ForegroundColorSpan(bracesColor), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            valueBuilder.setSpan(ForegroundColorSpan(numberColor), 6, len - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            valueBuilder.setSpan(ForegroundColorSpan(bracesColor), len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            itemView.showIcon(true)
            itemView.value = value
            itemView.appendComma = appendComma
            itemView.hierarchy = hierarchy + 1
            itemView.setOnClickListener(this)

        } else if (value is String) {
            itemView.hideIcon()
            valueBuilder.append("\"").append(value.toString()).append("\"")
            if (Utils.isUrl(value.toString())) {
                valueBuilder.setSpan(ForegroundColorSpan(textColor), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                valueBuilder.setSpan(ForegroundColorSpan(urlColor), 1, valueBuilder.length - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                valueBuilder.setSpan(ForegroundColorSpan(textColor), valueBuilder.length - 1, valueBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                valueBuilder.setSpan(ForegroundColorSpan(textColor), 0, valueBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

        } else if (valueBuilder.isEmpty() || value == null) {
            itemView.hideIcon()
            valueBuilder.append("null")
            valueBuilder.setSpan(ForegroundColorSpan(nullColor), 0, valueBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (appendComma) {
            valueBuilder.append(",")
        }
        itemView.showRight(valueBuilder)
    }

    fun toggleExpandCollapse(itemView: JsonItemView): List<JsonItemView> {
        val newItemViews: MutableList<JsonItemView> = ArrayList()
        val value = itemView.value
        if (value !is JSONArray && value !is JSONObject) {
            //Do nothing as the value is just a bracket or brace
        } else if (itemView.childCount == 1) {
            val hierarchy = itemView.hierarchy
            val isJsonArray = itemView.isJsonArray

            //Expand...
            itemView.isCollapsed = false
            itemView.showIcon(false)
            itemView.tag = itemView.rightText
            itemView.showRight(if (isJsonArray) "[" else "{")

            //...add children if there are any
            val array = if (isJsonArray) value as JSONArray? else (value as JSONObject).names()
            var i = 0
            while (array != null && i < array.length()) {
                val newItemView = addJsonItemView(itemView, value, array.opt(i), hierarchy, isJsonArray, i < array.length() - 1)
                newItemViews.add(newItemView)
                i++
            }
            addRightBracket(itemView, hierarchy, isJsonArray, itemView.appendComma)
        } else {
            //Collapse if expanded and vice versa. Applies to the last object in the JSON tree branch
            showExpandCollapse(itemView)
            itemView.isCollapsed = !itemView.isCollapsed
        }
        return newItemViews
    }

    private fun showExpandCollapse(itemView: JsonItemView) {
        val temp = itemView.rightText
        itemView.showRight(itemView.tag as CharSequence)
        itemView.tag = temp
        itemView.showIcon(!itemView.isCollapsed)
        for (i in 1 until itemView.childCount) {
            itemView.getChildAt(i).visibility = if (itemView.isCollapsed) View.VISIBLE else View.GONE
        }
    }

    private fun createRightBracketView(context: Context, hierarchy: Int, isJsonArray: Boolean, appendComma: Boolean): View {
        val childItemView = JsonItemView(context)
        childItemView.textSizeDp = textSizeDp
        childItemView.setRightColor(bracesColor)
        val builder = StringBuilder(Utils.getHierarchyStr(hierarchy - 1))
        builder.append(if (isJsonArray) "]" else "}").append(if (appendComma) "," else "")
        childItemView.showRight(builder)
        return childItemView
    }

    private fun addRightBracket(itemView: JsonItemView, hierarchy: Int, isJsonArray: Boolean, appendComma: Boolean) {
        val rightBracket = createRightBracketView(itemView.context, hierarchy, isJsonArray, appendComma)
        itemView.addViewNoInvalidate(rightBracket)
        itemView.requestLayout()
        itemView.invalidate()
    }

    private fun addJsonItemView(parentItemView: JsonItemView, value: Any, childValue: Any, hierarchy: Int, isJsonArray: Boolean, appendComma: Boolean): JsonItemView {
        val itemView = JsonItemView(parentItemView.context)
        itemView.textSizeDp = textSizeDp
        itemView.setRightColor(bracesColor)
        if (isJsonArray) {
            handleJsonArray(childValue, itemView, appendComma, hierarchy)
        } else {
            handleJsonObject(childValue as String, (value as JSONObject).opt(childValue), itemView, appendComma, hierarchy)
        }
        parentItemView.addViewNoInvalidate(itemView)
        return itemView
    }

    override fun onClick(view: View) {
        if (view is JsonItemView) {
            //The user clicked the container
            toggleExpandCollapse(view)
        } else {
            //The user click on of the children; need to get the container
            toggleExpandCollapse(view.parent.parent as JsonItemView)
        }
    }

    fun expandToDepth(itemView: JsonItemView, depth: Int) {
        //Depth 0 means 0 levels down from the itemView, so just return.
        if (depth == 0) return

        //This will get you one down
        val childItemViews = toggleExpandCollapse(itemView)
        if (depth > 1) {
            for (i in childItemViews.indices) {
                expandToDepth(childItemViews[i], depth - 1)
            }
        }
    }

}