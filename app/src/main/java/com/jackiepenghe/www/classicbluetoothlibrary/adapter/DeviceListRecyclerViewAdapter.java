package com.jackiepenghe.www.classicbluetoothlibrary.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;


import java.util.List;

public class DeviceListRecyclerViewAdapter extends BaseQuickAdapter<BluetoothDevice, BaseViewHolder> {
  /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data        A new list is created out of this one to avoid mutable list
     */
    public DeviceListRecyclerViewAdapter(@Nullable List<BluetoothDevice> data) {
        super(android.R.layout.simple_list_item_2, data);
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param helper A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    @Override
    protected void convert(BaseViewHolder helper, BluetoothDevice item) {
        String name = item.getName();
        if (null == name || "".equals(name)) {
            name = "NULL";
        }
        helper.setText(android.R.id.text1, name)
                .setText(android.R.id.text2, item.getAddress());
    }
}
