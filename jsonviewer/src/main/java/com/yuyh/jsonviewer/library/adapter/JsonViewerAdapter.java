package com.yuyh.jsonviewer.library.adapter;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;

import com.yuyh.jsonviewer.library.utils.Utils;
import com.yuyh.jsonviewer.library.view.JsonItemView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuyuhang on 2017/11/29.
 */
public class JsonViewerAdapter extends BaseJsonViewerAdapter<JsonItemViewHolder>
        implements View.OnClickListener {

    private String jsonStr;

    private JSONObject mJSONObject;
    private JSONArray mJSONArray;

    private int depth = 0;

    public JsonViewerAdapter(String jsonStr) {
        this.jsonStr = jsonStr;

        Object object = null;
        try {
            object = new JSONTokener(jsonStr).nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (object instanceof JSONObject) {
            mJSONObject = (JSONObject) object;
        } else if (object instanceof JSONArray) {
            mJSONArray = (JSONArray) object;
        } else {
            throw new IllegalArgumentException("jsonStr is illegal.");
        }
    }

    public JsonViewerAdapter(JSONObject jsonObject) {
        this.mJSONObject = jsonObject;
        if (mJSONObject == null) {
            throw new IllegalArgumentException("jsonObject can not be null.");
        }
    }

    public JsonViewerAdapter(JSONArray jsonArray) {
        this.mJSONArray = jsonArray;
        if (mJSONArray == null) {
            throw new IllegalArgumentException("jsonArray can not be null.");
        }
    }

    //This creates a viewHolder for each top-level JSON object. The viewHolder is not recyclable,
    //and hence each top-level JSON object has its own unique viewHolder
    @Override
    public JsonItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new JsonItemViewHolder(new JsonItemView(parent.getContext()));
    }

    //A viewHolder is created anew each time one of the top-level JSON objects is about to be displayed
    @Override
    public void onBindViewHolder(JsonItemViewHolder holder, int position) {

        //Create the top-level view for the top-level JSON object
        JsonItemView itemView = holder.getItemView();
        itemView.setTextSize(TEXT_SIZE_DP);
        itemView.setRightColor(BRACES_COLOR);

        //The top level can't be both an Object and Array. So, only one of the following does
        //anything for any given JSON string
        handleTopLevelJsonObject(mJSONObject, itemView, position);
        handleTopLevelJsonArray(mJSONArray, itemView, position);

        //Automatically get the children and show them as expanded or collapsed based on depth
        expandToDepth(itemView, depth);
    }

    @Override
    public int getItemCount() {
        //The initial count is the total number of top-level JSON objects plus two for curly brakets
        int count = 0;
        if (mJSONObject != null) {
            if (mJSONObject.names() != null) {
                count = mJSONObject.names().length() + 2;
            } else {
                count = 2;
            }
        } else if (mJSONArray != null) {
            count = mJSONArray.length() + 2;
        }
        return count;
    }

    private void handleTopLevelJsonObject(JSONObject jsonObject, JsonItemView itemView, int position) {
        if (jsonObject == null) return;

        if (position == 0) {
            //First item, prefix with opening curly
            itemView.hideLeft();
            itemView.hideIcon();
            itemView.showRight("{");
            return;

        } else if (position == getItemCount() - 1) {
            //last item, suffix with closing curly
            itemView.hideLeft();
            itemView.hideIcon();
            itemView.showRight("}");
            return;

        } else if (jsonObject.names() == null) {
            return;
        }

        //Update the itemView with the Json data
        String key = jsonObject.names().optString(position - 1);
        Object value = jsonObject.opt(key);
        if (position < getItemCount() - 2) {
            handleJsonObject(key, value, itemView, true, 1);
        } else {
            handleJsonObject(key, value, itemView, false, 1); // 最后一组，结尾不需要逗号
        }
    }

    private void handleTopLevelJsonArray(JSONArray jsonArray, JsonItemView itemView, int position) {
        if (jsonArray == null) return;

        if (position == 0) {
            itemView.hideLeft();
            itemView.hideIcon();
            itemView.showRight("[");
            return;
        } else if (position == getItemCount() - 1) {
            itemView.hideLeft();
            itemView.hideIcon();
            itemView.showRight("]");
            return;
        }

        Object value = jsonArray.opt(position - 1);
        if (position < getItemCount() - 2) {
            handleJsonArray(value, itemView, true, 1);
        } else {
            handleJsonArray(value, itemView, false, 1);
        }
    }

    private void handleJsonObject(String key, Object value, JsonItemView itemView, boolean appendComma, int hierarchy) {
        SpannableStringBuilder keyBuilder = new SpannableStringBuilder(Utils.getHierarchyStr(hierarchy));
        keyBuilder.append("\"").append(key).append("\"").append(":");
        keyBuilder.setSpan(new ForegroundColorSpan(KEY_COLOR), 0, keyBuilder.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        keyBuilder.setSpan(new ForegroundColorSpan(BRACES_COLOR), keyBuilder.length() - 1, keyBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        itemView.showLeft(keyBuilder);

        handleValue(value, itemView, appendComma, hierarchy);
    }

    private void handleJsonArray(Object value, JsonItemView itemView, boolean appendComma, int hierarchy) {
        itemView.showLeft(new SpannableStringBuilder(Utils.getHierarchyStr(hierarchy)));

        handleValue(value, itemView, appendComma, hierarchy);
    }

    private void handleValue(Object value, JsonItemView itemView, boolean appendComma, int hierarchy) {
        SpannableStringBuilder valueBuilder = new SpannableStringBuilder();
        if (value instanceof Number) {
            valueBuilder.append(value.toString());
            valueBuilder.setSpan(new ForegroundColorSpan(NUMBER_COLOR), 0, valueBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        } else if (value instanceof Boolean) {
            valueBuilder.append(value.toString());
            valueBuilder.setSpan(new ForegroundColorSpan(BOOLEAN_COLOR), 0, valueBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        } else if (value instanceof JSONObject) {

            valueBuilder.append("Object{...}");
            valueBuilder.setSpan(new ForegroundColorSpan(BRACES_COLOR), 0, valueBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            itemView.showIcon(true);
            itemView.setValue(value);
            itemView.setAppendComma(appendComma);
            itemView.setHierarchy(hierarchy + 1);
            itemView.setOnClickListener(this);

        } else if (value instanceof JSONArray) {

            valueBuilder.append("Array[").append(String.valueOf(((JSONArray) value).length())).append("]");
            int len = valueBuilder.length();
            valueBuilder.setSpan(new ForegroundColorSpan(BRACES_COLOR), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            valueBuilder.setSpan(new ForegroundColorSpan(NUMBER_COLOR), 6, len - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            valueBuilder.setSpan(new ForegroundColorSpan(BRACES_COLOR), len - 1, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            itemView.showIcon(true);
            itemView.setValue(value);
            itemView.setAppendComma(appendComma);
            itemView.setHierarchy(hierarchy + 1);
            itemView.setOnClickListener(this);

        } else if (value instanceof String) {
            itemView.hideIcon();
            valueBuilder.append("\"").append(value.toString()).append("\"");
            if (Utils.isUrl(value.toString())) {
                valueBuilder.setSpan(new ForegroundColorSpan(TEXT_COLOR), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                valueBuilder.setSpan(new ForegroundColorSpan(URL_COLOR), 1, valueBuilder.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                valueBuilder.setSpan(new ForegroundColorSpan(TEXT_COLOR), valueBuilder.length() - 1, valueBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                valueBuilder.setSpan(new ForegroundColorSpan(TEXT_COLOR), 0, valueBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        } else if (valueBuilder.length() == 0 || value == null) {
            itemView.hideIcon();
            valueBuilder.append("null");
            valueBuilder.setSpan(new ForegroundColorSpan(NULL_COLOR), 0, valueBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (appendComma) {
            valueBuilder.append(",");
        }

        itemView.showRight(valueBuilder);
    }

    public List<JsonItemView> toggleExpandCollapse(JsonItemView itemView) {
        List<JsonItemView> newItemViews = new ArrayList<JsonItemView>();
        Object value = itemView.getValue();

        if (!(value instanceof JSONArray) && !(value instanceof JSONObject)) {
            //value instanceof String && (value.equals("{") || value.equals("}") || value.equals("[") || value.equals("]"))
            //Do nothing as the value is just a
        } else if (itemView.getChildCount() == 1) {
            int hierarchy = itemView.getHierarchy();
            boolean isJsonArray = itemView.isJsonArray();

            //Expand...
            itemView.setCollapsed(false);
            itemView.showIcon(false);
            itemView.setTag(itemView.getRightText());
            itemView.showRight(isJsonArray ? "[" : "{");

            //...add children if there are any
            JSONArray array = isJsonArray ? (JSONArray) value : ((JSONObject) value).names();
            for (int i = 0; array != null && i < array.length(); i++) {
                JsonItemView newItemView = addJsonItemView(itemView, value, array.opt(i), hierarchy, isJsonArray, i < array.length() - 1);
                newItemViews.add(newItemView);
            }
            addRightBracket(itemView, hierarchy, isJsonArray, itemView.doAppendComma());

        } else {
            //Collapse if expanded and vice versa. Applies to the last object in the JSON tree branch
            showExpandCollapse(itemView);
            itemView.setCollapsed(!itemView.isCollapsed());
        }

        return newItemViews;
    }

    private void showExpandCollapse(JsonItemView itemView) {
        CharSequence temp = itemView.getRightText();
        itemView.showRight((CharSequence) itemView.getTag());
        itemView.setTag(temp);
        itemView.showIcon(!itemView.isCollapsed());
        for (int i = 1; i < itemView.getChildCount(); i++) {
            itemView.getChildAt(i).setVisibility(itemView.isCollapsed() ? View.VISIBLE : View.GONE);
        }
    }

    private View createRightBracketView(Context context, int hierarchy, boolean isJsonArray, boolean appendComma) {
        JsonItemView childItemView = new JsonItemView(context);
        childItemView.setTextSize(TEXT_SIZE_DP);
        childItemView.setRightColor(BRACES_COLOR);

        StringBuilder builder = new StringBuilder(Utils.getHierarchyStr(hierarchy - 1));
        builder.append(isJsonArray ? "]" : "}").append(appendComma ? "," : "");
        childItemView.showRight(builder);

        return childItemView;
    }

    private void addRightBracket(JsonItemView itemView, int hierarchy, boolean isJsonArray, boolean appendComma) {
        View rightBracket = createRightBracketView(itemView.getContext(), hierarchy, isJsonArray, appendComma);
        itemView.addViewNoInvalidate(rightBracket);
        itemView.requestLayout();
        itemView.invalidate();
    }

    private JsonItemView addJsonItemView(JsonItemView parentItemView, Object value, Object childValue, int hierarchy, boolean isJsonArray, boolean appendComma) {
        JsonItemView itemView = new JsonItemView(parentItemView.getContext());
        itemView.setTextSize(TEXT_SIZE_DP);
        itemView.setRightColor(BRACES_COLOR);

        if (isJsonArray) {
            handleJsonArray(childValue, itemView, appendComma, hierarchy);
        } else {
            handleJsonObject((String) childValue, ((JSONObject) value).opt((String) childValue), itemView, appendComma, hierarchy);
        }
        parentItemView.addViewNoInvalidate(itemView);
        return itemView;
    }

    @Override
    public void onClick(View view) {
        if (view instanceof JsonItemView) {
            //The user clicked the container
            toggleExpandCollapse((JsonItemView) view);
        } else {
            //The user click on of the children; need to get the container
            toggleExpandCollapse((JsonItemView) view.getParent().getParent());
        }
    }

    public void expandToDepth(JsonItemView itemView, int depth) {
        //Depth 0 means 0 levels down from the itemView, so just return.
        if (depth == 0) return;

        //This will get you one down
        List<JsonItemView> childItemViews = toggleExpandCollapse(itemView);
        if (depth > 1) {
            for(int i = 0; i < childItemViews.size(); i++) {
                expandToDepth(childItemViews.get(i), depth - 1);
            }
        }
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
