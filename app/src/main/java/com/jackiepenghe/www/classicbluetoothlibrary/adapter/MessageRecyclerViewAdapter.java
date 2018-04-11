package com.jackiepenghe.www.classicbluetoothlibrary.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * Socket通讯，用于显示信息的适配器
 *
 * @author jackie
 */
public class MessageRecyclerViewAdapter extends BaseQuickAdapter<String,BaseViewHolder>{
    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data        A new list is created out of this one to avoid mutable list
     */
    public MessageRecyclerViewAdapter( @Nullable List<String> data) {
        super(android.R.layout.simple_list_item_1, data);
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param helper A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(android.R.id.text1,item);
    }
}
