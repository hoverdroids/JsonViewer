package com.yuyh.jsonviewer.library.adapter

import androidx.recyclerview.widget.RecyclerView
import com.yuyh.jsonviewer.library.view.JsonItemView

class JsonItemViewHolder internal constructor(val jsonItemView: JsonItemView)
    : RecyclerView.ViewHolder(jsonItemView) {
    init { setIsRecyclable(false) }
}