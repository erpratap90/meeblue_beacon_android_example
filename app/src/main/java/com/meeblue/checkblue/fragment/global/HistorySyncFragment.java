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

package com.meeblue.checkblue.fragment.global;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.meeblue.checkblue.MyApp;
import com.meeblue.checkblue.R;
import com.meeblue.checkblue.utils.LocalBroadcastManager;
import com.meeblue.checkblue.ble.callback.BLEMainDataCallback;
import com.meeblue.checkblue.ble.profile.MEEBLUE_Defines;
import com.meeblue.checkblue.ble.struct.Reading_Sensor_State_t;
import com.meeblue.checkblue.ble.struct.Sensor_Configure_Data_t;
import com.meeblue.checkblue.ble.struct.Sensor_data_t;
import com.meeblue.checkblue.core.BaseFragment;
import com.meeblue.checkblue.fragment.utils.BLEUtils;
import com.meeblue.checkblue.fragment.utils.CSVUtil;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.utils.DensityUtils;
import com.xuexiang.xui.widget.dialog.DialogLoader;
import com.xuexiang.xui.widget.grouplist.XUICommonListItemView;
import com.xuexiang.xui.widget.grouplist.XUIGroupListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import no.nordicsemi.android.ble.data.Data;

import static java.lang.String.format;

/**
 * author alvin
 * since 2020-03-17
 */
@Page(name = "History")
public class HistorySyncFragment extends BaseFragment {

    @BindView(R.id.groupListView)
    XUIGroupListView mGroupListView;

    private boolean OnlyLoad_Once = false;

    private HashMap<String, XUICommonListItemView> m_table_hash_map = new HashMap<String, XUICommonListItemView>();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_settings;
    }


    //SYNC
    Sensor_Configure_Data_t Sensor_Configure = new Sensor_Configure_Data_t();
    //ArrayList<byte[]>       m_History = new ArrayList<byte[]>();
    HashMap<Long, byte[]>       m_History = new HashMap<Long, byte[]>();
    HashMap<Long, byte[]>       Story_History = new HashMap<Long, byte[]>();
    Reading_Sensor_State_t Reading_State = new Reading_Sensor_State_t();
    int m_curent_percent = 0;

    @Override
    protected void initViews() {

        if (MyApp.Main_Service.get_ble_gatt().getService(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE) != null)
            addActionRightAction("Export");
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        Story_History = CSVUtil.readTHBytesToBuffer(requireContext(), MyApp.Main_Service.get_ble_gatt().getDevice());
        UpdateTable_View();
        ReadConfigureData();
    }

    @Override
    protected void onRightActionClick() {
        super.onRightActionClick();
        ArrayList<String> export = new ArrayList<String>();
        Object[] keys =  m_History.keySet().toArray();
        String ALL = "time"+CSVUtil.csv_segmentation_symbol+"Temperature"+CSVUtil.csv_segmentation_symbol+"Relative Humidity";
        export.add(ALL);
        BLEUtils.DEBUG_PRINTF("Here");
        showLoadingDialog("Trying to generate csv file");
        for (Object temp : keys)
        {
            long time = (long)temp;
            byte[] value = m_History.get(temp);

            String Temperature = "Temperature: "+ BLEUtils.Convert_Temperature(BLEUtils.getUnsignedInt16(BLEUtils.extractBytes(value,0, 2), false));
            String Humidity = "Relative Humidity: "+ BLEUtils.Convert_Temperature(BLEUtils.getUnsignedInt16(BLEUtils.extractBytes(value,2, 2), false));
            String Time = BLEUtils.Convert_TimeSeconed(time);
            ALL = Time+CSVUtil.csv_segmentation_symbol+Temperature+CSVUtil.csv_segmentation_symbol+Humidity;
            export.add(ALL);
        }
        String File = CSVUtil.exportCsv(CSVUtil.get_share_file_path_by_mac(requireContext(), MyApp.Main_Service.get_ble_gatt().getDevice().getAddress().replace(":", "")), export);
        if (File != null)
        {
            dismissDialog();
            CSVUtil.share_file(requireContext(), File);
        }
        else {
            showErrorDialog("Failed to export");
        }

        BLEUtils.DEBUG_PRINTF("Here1");
    }

    public void ReadConfigureData()
    {
        m_curent_percent = 0;
        showLoadingDialog("Trying to sync data...");
        MyApp.Main_Service.readCharacteristic(MEEBLUE_Defines.MEEBLUE_TH_SERVICE, MEEBLUE_Defines.MEEBLUE_TH_TIME, new BLEMainDataCallback() {

            @Override
            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                super.onOptionState(device, data, state);
                if (state && data != null) {
                    Sensor_Configure.setCombination(data.getValue());
                    MyApp.Main_Service.enableCharacteristicNotifications(MEEBLUE_Defines.MEEBLUE_TH_SERVICE, MEEBLUE_Defines.MEEBLUE_TH_SYNC, new BLEMainDataCallback() {
                        @Override
                        public void onNotificatioinState(@NonNull BluetoothDevice device, boolean state) {
                            super.onNotificatioinState(device, state);
                            if (state) {
                                Sensor_Configure_Data_t temp = new Sensor_Configure_Data_t();
                                temp.current_time = System.currentTimeMillis()/1000;
                                temp.sync_max_time = 0;//the latest record time, 0 for all data
                                temp.option_command = 0x81;//Sync Time to Device | Sync Data From Device
                                temp.option_size = 45;
                                Reading_State.start_time = System.currentTimeMillis();
                                MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_TH_SERVICE, MEEBLUE_Defines.MEEBLUE_TH_TIME, new Data(temp.getCombination()), new BLEMainDataCallback() {
                                    @Override
                                    public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                                        super.onOptionState(device, data, state);
                                        if (state) {
                                            BLEUtils.DEBUG_PRINTF("started");
                                        }
                                        else{
                                            showErrorDialog("Sync failed");
                                        }
                                    }
                                });
                            }
                            else if (!state){
                                showErrorDialog("Sync failed");
                            }
                        }

                        @Override
                        public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                            super.onDataReceived(device, data);
                            BLEUtils.DEBUG_PRINTF("Length:"+data.getValue().length+" "+BLEUtils.byteToStringValue(data.getValue(), false));

                            if (!(data.getValue().length%4 == 0 && data.getValue().length >= 20))
                            {
                                showErrorDialog("Current Beacon Not Supported");
                                return ;
                            }
                            else
                            {
                                int length = (int)data.getValue().length/4;
                                byte[] readvalue = data.getValue();
                                long time = BLEUtils.getUnsignedInt32(readvalue, false);
                                //BLEUtils.DEBUG_PRINTF(BLEUtils.Convert_TimeSeconed(time));
                                for (int i = 1; i < length; i++) {
                                    byte[] sub_temp = BLEUtils.subBytes(readvalue, 4*i, 4);
                                    if (!BLEUtils.isAllFFValue(sub_temp)) {
                                        Sensor_data_t temp = new Sensor_data_t();
                                        temp.setData(sub_temp);
                                        temp.time = time-(i-1)*60;
                                        m_History.put(temp.time, sub_temp);
                                        Reading_State.current_count += 1;
                                    }
                                    else{
                                        Reading_State.end_time = System.currentTimeMillis();

                                        dismissDialog();
                                        Save_New_Data();
                                        return;
                                    }
                                }

                                float total = Sensor_Configure.current_max_count;
                                float current = Reading_State.current_count;
                                total = current/total*100;
                                int per = (int) total;
                                if (per > m_curent_percent) {
                                    m_curent_percent = per;
                                    String STRR =  "Has synchronized "+Reading_State.current_count+" records";
                                    showLoadingDialog(STRR);
                                }
                            }
                        }
                    });
                }
                else{
                    showErrorDialog("Sync failed");
                }
            }
        });
    }


    public void Clear_All_Data()
    {
        Sensor_Configure_Data_t temp = new Sensor_Configure_Data_t();
        temp.current_time = System.currentTimeMillis()/1000;
        temp.sync_max_time = 0;
        temp.current_max_count = 0;
        temp.option_command = 0xC0;//Clear All
        temp.option_size = 0;
        Reading_State.start_time = temp.current_time;
        Reading_State.end_time = temp.current_time;
        showLoadingDialog("Trying to clear flash data");
        MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_TH_SERVICE, MEEBLUE_Defines.MEEBLUE_TH_TIME, new Data(temp.getCombination()), new BLEMainDataCallback() {
            @Override
            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                super.onOptionState(device, data, state);
                if (state) {
                    Reading_State.current_count = 0;
                    m_History.clear();
                    //[self.m_table_view reloadData];
                    dismissDialog();
                }
                else{
                    showErrorDialog("Clear failed");
                }
            }
        });
    }

    private boolean isContainInList(long NUM)
    {
        return m_History.containsKey(NUM);
    }

    private void Save_New_Data()
    {
        Object[] keys =  m_History.keySet().toArray();
        Arrays.sort(keys);
        showLoadingDialog("Trying to save...");
        CSVUtil.writeTHBytesToFile(m_History, requireContext(), MyApp.Main_Service.get_ble_gatt().getDevice(), new BLEMainDataCallback() {
            @Override
            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                super.onOptionState(device, data, state);
                if (state){
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showSuccessDialog("History data saved successfully");
                        }
                    });

                }
                else{
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showErrorDialog("History data save failed");
                        }
                    });

                }
            }
        });
        Story_History.putAll(m_History);
        //m_History = [NSMutableArray arrayWithArray:history];
        //[self.m_table_view reloadData];
        UpdateTable_View();
    }

    public class GroupListItemObject extends Object {
        public int section = 0;
        public int row = 0;
        public boolean right = false;

        GroupListItemObject(int s, int r, boolean rt) {
            super();
            section = s;
            row = r;
            right = rt;
        }
    }

    private XUICommonListItemView CreatItemView(String Title, String Subtitle, int Section, int Row, boolean right) {
        String KEY = "Section:" + Section + "-Row:" + Row;
        XUICommonListItemView itemWithDetailBelow = m_table_hash_map.get(KEY);

        if (itemWithDetailBelow != null) {
            itemWithDetailBelow.setText(Title);
            itemWithDetailBelow.setDetailText(Subtitle);
            if (right) {
                itemWithDetailBelow.setAccessoryType(XUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
                FrameLayout view = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.acc_right_view, null);
                itemWithDetailBelow.addAccessoryCustomView(view);
            } else {
                itemWithDetailBelow.setAccessoryType(XUICommonListItemView.ACCESSORY_TYPE_NONE);
            }

            return itemWithDetailBelow;
        }

        itemWithDetailBelow = mGroupListView.createItemView(Title);
        itemWithDetailBelow.setOrientation(XUICommonListItemView.HORIZONTAL);
        itemWithDetailBelow.setDetailText(Subtitle);
        itemWithDetailBelow.setTag(new GroupListItemObject(Section, Row, right));

        itemWithDetailBelow.getTextView().setTextSize(15);
        itemWithDetailBelow.getDetailTextView().setTextColor(getResources().getColor(R.color.colorPrimary));
        itemWithDetailBelow.getDetailTextView().setTextSize(15);

        if (right) {
            itemWithDetailBelow.setAccessoryType(XUICommonListItemView.ACCESSORY_TYPE_CUSTOM);
            FrameLayout view = (FrameLayout) LayoutInflater.from(getActivity()).inflate(R.layout.acc_right_view, null);
            itemWithDetailBelow.addAccessoryCustomView(view);
        } else {
            itemWithDetailBelow.setAccessoryType(XUICommonListItemView.ACCESSORY_TYPE_NONE);
        }
        m_table_hash_map.put(KEY, itemWithDetailBelow);
        return itemWithDetailBelow;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof XUICommonListItemView) {
                OnTableItemClick((GroupListItemObject) v.getTag());
            }
        }
    };


    private void OnTableItemClick(GroupListItemObject object) {
        didSelectRowAtIndexPath(object);
    }

    private void initGroupListView() {
        int size = DensityUtils.dp2px(getContext(), 20);

        if (!OnlyLoad_Once) {
            for (int section = 0; section < numberOfSectionsInTableView(); section++) {
                String Title = titleForHeaderInSection(section);
                XUIGroupListView.Section temp = XUIGroupListView.newSection(getContext());
                temp.setTitle(Title);
                temp.setDescription("");
                for (int row = 0; row < numberOfRowsInSection(section); row++) {
                    XUICommonListItemView TempView = cellForRowAtIndexPath(new GroupListItemObject(section, row, false));
                    if (TempView != null) temp.addItemView(TempView, onClickListener);
                }
                temp.addTo(mGroupListView);
            }
        } else//更新数据即可
        {
            for (int section = 0; section < numberOfSectionsInTableView(); section++) {
                for (int row = 0; row < numberOfRowsInSection(section); row++) {
                    cellForRowAtIndexPath(new GroupListItemObject(section, row, false));
                }
            }
        }
        OnlyLoad_Once = true;
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(BLEStateBroadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(BLEStateBroadcastReceiver, new IntentFilter(MEEBLUE_Defines.BROADCAST_SERVICES_DISCONNECTED));
    }

    private final BroadcastReceiver BLEStateBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            popToBack();
        }
    };


    public int numberOfRowsInSection(int section) {
        switch (section) {
            case 0://basic
                return 6;
            case 1://trigger mode
                return m_History.size();
        }
        return 0;
    }


    public int numberOfSectionsInTableView() {
        return 1;
    }


    public String titleForHeaderInSection(int section) {
        switch (section) {
            case 0:
                return "\nData Synchronization Status";
            case 1:
                return "\nT&H History Data";
            default:
                break;
        }
        return "";
    }

    private XUICommonListItemView cellForBasic(GroupListItemObject indexPath)
    {
        Object[] keys = Story_History.keySet().toArray();
        Arrays.sort(keys);

        long TEMP = 0;
        switch (indexPath.row) {
            case 0:
                if (Story_History.size() > 0) {
                    return CreatItemView("Latest Record Time", BLEUtils.Convert_TimeSeconed((long)keys[keys.length-1]), indexPath.section, indexPath.row, false);
                }else{
                    return CreatItemView("Latest Record Time", "--", indexPath.section, indexPath.row, false);
                }
            case 1:
                if (Story_History.size() > 0) {
                    return CreatItemView("Oldest Record Time", BLEUtils.Convert_TimeSeconed((long)keys[0]), indexPath.section, indexPath.row, false);
                }else{
                    return CreatItemView("Oldest Record Time", "--", indexPath.section, indexPath.row, false);
                }

            case 2:
                long second = (Reading_State.end_time - Reading_State.start_time);
                return CreatItemView("Time Consuming for Sync", second+"ms", indexPath.section, indexPath.row, false);
            case 3:
                return CreatItemView("Totel Count Sync from Device", Reading_State.current_count+"", indexPath.section, indexPath.row, false);
            case 4:
                return CreatItemView("Total Count Saved on phone", m_History.size()+"", indexPath.section, indexPath.row, false);
            case 5:
                return CreatItemView("Clear All Flash Data", "Clear", indexPath.section, indexPath.row, true);
            default:
                break;
        }

        return null;
    }


    private XUICommonListItemView cellForRowAtIndexPath(GroupListItemObject indexPath) {
        switch (indexPath.section) {
            case 0:
                return cellForBasic(indexPath);
        }
        return null;
    }

    protected void showSureLoader(String Message)
    {
        DialogLoader.getInstance().showConfirmDialog(
                getContext(),
                getString(R.string.warn_tip),
                Message,
                "Sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Clear_All_Data();
                    }
                }, "Cancel", null);
    }

    private void didSelectRowAtIndexPath(GroupListItemObject indexPath) {

        switch (indexPath.section) {
            case 0:
                switch (indexPath.row) {
                    case 5://清理所有数据
                        showSureLoader("Do you really sure to clear all flash data?");
                        break;
                }
                break;
            default:
                break;
        }
    }

    protected void UpdateTable_View() {
        initGroupListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateTable_View();
    }
}
