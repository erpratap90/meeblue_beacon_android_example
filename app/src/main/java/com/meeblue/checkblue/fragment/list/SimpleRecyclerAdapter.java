/*
 * Copyright (C) 2020 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.meeblue.checkblue.fragment.list;

import android.view.View;
import android.view.ViewGroup;

import com.meeblue.checkblue.eddystone.eddystonevalidator.Constants;
import com.meeblue.checkblue.fragment.utils.BLELocalInfo;
import com.meeblue.checkblue.fragment.utils.BLEUtils;
import com.scwang.smartrefresh.layout.adapter.SmartRecyclerAdapter;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;
import com.meeblue.checkblue.R;

import java.util.Collection;

/**
 * @author Alvin
 * @since 2020/3/1 11:04
 */
public class SimpleRecyclerAdapter extends SmartRecyclerAdapter<BLELocalInfo> {

    private int view_type = 0;

    public void setViewType(int type)
    {
        view_type = type;
    }
    public SimpleRecyclerAdapter() {
        super(R.layout.global_basic_list_item);
    }


    public SimpleRecyclerAdapter(Collection<BLELocalInfo> data) {
        super(data, R.layout.global_basic_list_item);
    }

    @Override
    protected View generateItemView(ViewGroup parent, int viewType) {
        return getInflate(parent, R.layout.global_basic_list_item);
    }

    @Override
    public int getItemViewType(int position) {
        return view_type;
    }

    /**
     * @param holder
     * @param model
     * @param position
     */
    @Override
    protected void onBindViewHolder(SmartViewHolder holder, BLELocalInfo model, int position) {

        show_global_basic_view(holder, model, position);
    }


    private void show_global_basic_view(SmartViewHolder holder, BLELocalInfo model, int position)
    {
        holder.text(R.id.device_connectable, "Connectable: "+(model.getResult().isConnectable()? "true" : "false"));
        holder.text(R.id.device_mac, "Mac Adress: "+model.getResult().getDevice().getAddress());

        String TX = (model.getResult().getScanRecord().getTxPowerLevel() < -1000) ? BLELocalInfo.NULL_STRING_SHOW : (""+model.getResult().getScanRecord().getTxPowerLevel());
        holder.text(R.id.tx_power, "Broadcast Tx Power: "+ TX);


        String DeviceName = "N/A";
        if (model.getResult().getScanRecord().getDeviceName() != null && model.getResult().getScanRecord().getDeviceName().length() > 0)
        {
            DeviceName = model.getResult().getScanRecord().getDeviceName();
        }
        else if (model.getResult().getDevice().getName() != null && model.getResult().getDevice().getName().length() > 0)
        {
            DeviceName = model.getResult().getDevice().getName();
        }
        holder.text(R.id.device_name, DeviceName);
        holder.text(R.id.device_rssi, "Rssi: "+model.getResult().getRssi());

        String Local_name = BLELocalInfo.NULL_STRING_SHOW;
        if (model.getResult().getScanRecord().getDeviceName() != null && model.getResult().getScanRecord().getDeviceName().length() > 0)
        {
            Local_name = model.getResult().getScanRecord().getDeviceName();
        }

        holder.text(R.id.device_local_name, "Local Name: "+ Local_name);
    }

}
