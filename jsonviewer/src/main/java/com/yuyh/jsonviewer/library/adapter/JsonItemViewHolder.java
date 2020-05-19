package com.yuyh.jsonviewer.library.adapter;

import androidx.recyclerview.widget.RecyclerView;

import com.yuyh.jsonviewer.library.view.JsonItemView;

public class JsonItemViewHolder extends RecyclerView.ViewHolder {

    private JsonItemView itemView;

    JsonItemViewHolder(JsonItemView itemView) {
        super(itemView);
        setIsRecyclable(false);
        this.itemView = itemView;
    }

    public JsonItemView getItemView() {
        return itemView;
    }
}
