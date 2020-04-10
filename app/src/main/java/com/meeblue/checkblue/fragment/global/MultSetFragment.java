/*
 * Copyright (C) 2020 meeblue
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
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.meeblue.checkblue.MyApp;
import com.meeblue.checkblue.R;
import com.meeblue.checkblue.utils.LocalBroadcastManager;
import com.meeblue.checkblue.ble.callback.BLEMainDataCallback;
import com.meeblue.checkblue.ble.struct.Beacon_All_Data_t;
import com.meeblue.checkblue.ble.profile.MEEBLUE_Defines;
import com.meeblue.checkblue.core.BaseFragment;
import com.meeblue.checkblue.fragment.utils.BLEUtils;
import com.meeblue.checkblue.fragment.utils.CSVUtil;
import com.meeblue.checkblue.fragment.utils.StringInputCallback;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.utils.DensityUtils;
import com.xuexiang.xui.widget.grouplist.XUICommonListItemView;
import com.xuexiang.xui.widget.grouplist.XUIGroupListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import no.nordicsemi.android.ble.data.Data;

/**
 * author alvin
 * since 2020-03-17
 */
@Page(name = "Mult-Set")
public class MultSetFragment extends BaseFragment {

    @BindView(R.id.groupListView)
    XUIGroupListView mGroupListView;

    private boolean OnlyLoad_Once = false;

    private HashMap<String, XUICommonListItemView> m_table_hash_map  = new HashMap<String, XUICommonListItemView>();

    HashMap<Long, byte[]> history_data = new HashMap<Long, byte[]>();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    protected void initViews() {

        if (MyApp.Main_Service.get_ble_gatt().getService(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE) != null)
            addActionRightAction("Refresh");
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        history_data = CSVUtil.readTHBytesToBuffer(requireContext(), MyApp.Main_Service.get_ble_gatt().getDevice());
        UpdateTableView();
    }

    @Override
    protected void onRightActionClick() {
        UpdateTableView();
        super.onRightActionClick();
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


    private void UpdateTableView()
    {
        showLoadingDialog("Reading configurations...");
        history_data = CSVUtil.readTHBytesToBuffer(requireContext(), MyApp.Main_Service.get_ble_gatt().getDevice());
        MyApp.Main_Service.read_all_meeblue_data_from_device(new BLEMainDataCallback() {
            @Override
            public void onDataReadProcess(@NonNull BluetoothDevice device, int percent) {
                super.onDataReadProcess(device, percent);
                showLoadingDialog("Reading...finished:"+percent+"%");
            }

            @Override
            public void onReadAllFinished(@NonNull BluetoothDevice device) {
                super.onReadAllFinished(device);
                dismissDialog();
                UpdateTable_View();
            }
        });
    }

    private XUICommonListItemView CreatItemView(String Title, String Subtitle, int Section, int Row, boolean right) {
        //XUICommonListItemView itemWithDetailBelow = mGroupListView.createItemView(Title);
        String KEY = "Section:"+Section+"-Row:"+Row;
        XUICommonListItemView itemWithDetailBelow = m_table_hash_map.get(KEY);

        if (itemWithDetailBelow != null)
        {
            itemWithDetailBelow.setText(Title);
            itemWithDetailBelow.setDetailText(Subtitle);
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
        //ToastUtils.toast("Section:" + object.section + "  Row：" + object.row);
        didSelectRowAtIndexPath(object);
    }

    private void initGroupListView() {
        int size = DensityUtils.dp2px(getContext(), 20);

        if (!OnlyLoad_Once) {
            for (int section = 0; section < numberOfSectionsInTableView(); section++)
            {
                String Title = titleForHeaderInSection(section);
                XUIGroupListView.Section temp = XUIGroupListView.newSection(getContext());
                temp.setTitle(Title);
                temp.setDescription("");
                for (int row = 0; row < numberOfRowsInSection(section); row++)
                {
                    XUICommonListItemView TempView = cellForRowAtIndexPath(new GroupListItemObject(section, row, false));
                    if (TempView != null)temp.addItemView(TempView, onClickListener);
                }
                temp.addTo(mGroupListView);
            }
        }
        else//更新数据即可
        {
            for (int section = 0; section < numberOfSectionsInTableView(); section++)
            {
                for (int row = 0; row < numberOfRowsInSection(section); row++)
                {
                    cellForRowAtIndexPath(new GroupListItemObject(section, row, false));
                }
            }
        }
        OnlyLoad_Once = true;
    }

    @Override
    public void onDestroyView() {
        MyApp.Main_Service.cancel_connect();
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
            case 0://adv data
                return 3;
            case 1://beacon state
            {
                int count = 6;
                if (MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_ACC_SERVICE, MEEBLUE_Defines.MEEBLUE_MOTION_1ST) != null)
                    count = count + 2;
                if (MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_TH_SERVICE, MEEBLUE_Defines.MEEBLUE_TH_DATA) != null)
                    count = count + 1;
                return count;
            }
            case 2://peripheral
                if (MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_ACC_SERVICE, MEEBLUE_Defines.MEEBLUE_MOTION_1ST) == null)
                    return 2;
                return 4;
            case 3://T&H Sensor
                if (MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_TH_SERVICE, MEEBLUE_Defines.MEEBLUE_TH_DATA) == null)
                    return 0;
                if (MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_TH_SERVICE, MEEBLUE_Defines.MEEBLUE_TH_SYNC) != null) {
                    return 7;
                } else {
                    return 3;
                }
            case 4://Device information
                return 5;
            case 5://导出所有数据
                return 1;
        }
        return 0;
    }


    public int numberOfSectionsInTableView() {
        return 6;
    }


    public String titleForHeaderInSection(int section) {
        switch (section) {
            case 0:
                return "\nAdvertisement Data";
            case 1:
                return "\nBeacon State Control";
            case 2:
                return "\nDevice Funtion";
            case 3:
                return "\nT&H Sensor Data";
            case 4:
                return "\nDevice Information";
            case 5:
                return "\nConfigurations For production";
            default:
                break;
        }
        return "";
    }

    private XUICommonListItemView cellForState(GroupListItemObject indexPath) {
        switch (indexPath.row) {
            case 0:
                return CreatItemView("Advertisment Mode:", "" + MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Beacon_State, indexPath.section, indexPath.row, true);
            case 1:
                return CreatItemView("Broadcast Interval:", MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Beacon_Broadcast + "*0.625ms", indexPath.section, indexPath.row, true);
            case 2:
                return CreatItemView("Tx Power Code:", MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_txPower + " dBm", indexPath.section, indexPath.row, true);
            case 3:
                if (MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Connect_State == 0) {
                    return CreatItemView("Keep Connect Max Time:", "No limit", indexPath.section, indexPath.row, true);
                } else {
                    return CreatItemView("Keep Connect Max Time:", MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Connect_State + " min", indexPath.section, indexPath.row, true);
                }
            case 4:
                return CreatItemView("Trigger Mode Adv Time:", MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Trigger_Mode_Adv_Time + " s", indexPath.section, indexPath.row, true);
            case 5:
                if (MyApp.Main_Service.getBeacon_All_Data().Beacon_State.Low_Power_Enable == 0x01) {
                    return CreatItemView("Low Power Broadcast:", "Enable", indexPath.section, indexPath.row, true);
                }
                return CreatItemView("Low Power Broadcast:", "Disable", indexPath.section, indexPath.row, true);
            case 6:
                if ((MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_ACC_SERVICE, MEEBLUE_Defines.MEEBLUE_MOTION_1ST) == null)) {
                    return CreatItemView("T&H Data Update Interval:", MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Temp_Save_Interval + " min", indexPath.section, indexPath.row, true);
                }
                return CreatItemView("INT1 Motion Detect Level:", MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Motion_Strength_One + "*16mg", indexPath.section, indexPath.row, true);
            case 7:
                return CreatItemView("INT2 Motion Detect Level:", MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Motion_Strength_Two + "*16mg", indexPath.section, indexPath.row, true);

            case 8:
                return CreatItemView("T&H Data Update Interval:", MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Temp_Save_Interval + " min", indexPath.section, indexPath.row, true);

            default:
                break;
        }
        return null;
    }

    private XUICommonListItemView cellForINformation(GroupListItemObject indexPath) {
        switch (indexPath.row) {
            case 0://name
                return CreatItemView("Device Name:", new String(MyApp.Main_Service.getBeacon_All_Data().mDevice_Name), indexPath.section, indexPath.row, true);

            case 1://batt
                return CreatItemView("Battery Voltage:", MyApp.Main_Service.getBeacon_All_Data().m_batt_voltage + " mv", indexPath.section, indexPath.row, false);

            case 2://Device Time
                return CreatItemView("Time From Device:", BLEUtils.Convert_TimeSeconed(MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_current_timeSnape) + "", indexPath.section, indexPath.row, false);

            case 3://firmware ID
                return CreatItemView("Firmware Version:", new String(MyApp.Main_Service.getBeacon_All_Data().Firmware_ID).toUpperCase(), indexPath.section, indexPath.row, false);
            case 4://Device ID
                return CreatItemView("Device Identifier:", MyApp.Main_Service.get_ble_gatt().getDevice().getAddress().replace(":", "").toUpperCase(), indexPath.section, indexPath.row, false);
            default:
                break;
        }

        return null;
    }

    private XUICommonListItemView cellForTHSensor(GroupListItemObject indexPath) {

        Object[] keys = history_data.keySet().toArray();
        Arrays.sort(keys);

        switch (indexPath.row) {
            case 0://temp
                if (MyApp.Main_Service.getBeacon_All_Data().m_TH_Data.m_time < 1577808000) {//maybe you forget to sync timesnape to device
                    return CreatItemView("Latest Update Time:", "--", indexPath.section, indexPath.row, false);
                } else {

                    return CreatItemView("Latest Update Time:", BLEUtils.Convert_TimeSeconed(MyApp.Main_Service.getBeacon_All_Data().m_TH_Data.m_time), indexPath.section, indexPath.row, false);
                }

            case 1://temp
                return CreatItemView("Real-Time Temperature Data:", BLEUtils.Convert_Temperature(MyApp.Main_Service.getBeacon_All_Data().m_TH_Data.m_temperture), indexPath.section, indexPath.row, false);

            case 2://humi
                return CreatItemView("Real-Time Humidity Data:", BLEUtils.Convert_Humidity(MyApp.Main_Service.getBeacon_All_Data().m_TH_Data.m_humidity), indexPath.section, indexPath.row, false);

            case 3:
                return CreatItemView("The Count Saved On Phone:", ""+history_data.size(), indexPath.section, indexPath.row, false);
            case 4:
                if (history_data.size() > 0) return CreatItemView("Latest Record Time:", BLEUtils.Convert_TimeSeconed((long)keys[keys.length-1]), indexPath.section, indexPath.row, false);
                else return CreatItemView("Latest Record Time:", "--", indexPath.section, indexPath.row, false);

            case 5:
                if (history_data.size() > 0) return CreatItemView("Oldest Record Time:", BLEUtils.Convert_TimeSeconed((long)keys[0]), indexPath.section, indexPath.row, false);
                else return CreatItemView("Oldest Record Time:", "--", indexPath.section, indexPath.row, false);

            case 6:
                return CreatItemView("Sync to latest data:", ""+MyApp.Main_Service.getBeacon_All_Data().Sensor_Write.current_max_count, indexPath.section, indexPath.row, true);

            default:
                break;
        }

        return null;
    }

    private XUICommonListItemView cellForAdvertisementdata(GroupListItemObject indexPath) {

        switch (indexPath.row) {
            case 0:
                return CreatItemView("1st Advertise Channel:", MyApp.Main_Service.getBeacon_All_Data().dataType(1), indexPath.section, indexPath.row, true);

            case 1:
                return CreatItemView("2nd Advertise Channel:", MyApp.Main_Service.getBeacon_All_Data().dataType(2), indexPath.section, indexPath.row, true);

            case 2:
                return CreatItemView("Custom Defined Adv Data:", "Modify", indexPath.section, indexPath.row, true);

            default:
                break;
        }
        return null;
    }

    private XUICommonListItemView cellForPeripheral(GroupListItemObject indexPath) {
        switch (indexPath.row) {
            case 0:
                return CreatItemView("Find The Beacon:", "", indexPath.section, indexPath.row, true);
            case 2://1st motion
            {
                BluetoothGattDescriptor Descriptor = MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_ACC_SERVICE, MEEBLUE_Defines.MEEBLUE_MOTION_1ST).getDescriptor(MEEBLUE_Defines.NotifyCation_UUID);
                if (Descriptor.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    return CreatItemView("INT1 Motion Detect:", "Enabled", indexPath.section, indexPath.row, true);
                }
                return CreatItemView("INT1 Motion Detect:", "Disabled", indexPath.section, indexPath.row, true);
            }

            case 3: {
                BluetoothGattDescriptor Descriptor = MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_ACC_SERVICE, MEEBLUE_Defines.MEEBLUE_MOTION_2ND).getDescriptor(MEEBLUE_Defines.NotifyCation_UUID);
                if (Descriptor.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    return CreatItemView("INT2 Motion Detect:", "Enabled", indexPath.section, indexPath.row, true);
                }
                return CreatItemView("INT2 Motion Detect:", "Disabled", indexPath.section, indexPath.row, true);
            }

            case 1: {
                BluetoothGattDescriptor Descriptor = MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_PERIPHERAL_SERVICE, MEEBLUE_Defines.MEEBLUE_PERIPHERAL_BUTTON).getDescriptor(MEEBLUE_Defines.NotifyCation_UUID);
                if (Descriptor.getValue() == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) {
                    return CreatItemView("Button Detect:", "Enabled", indexPath.section, indexPath.row, true);
                }
                return CreatItemView("Button Detect:", "Disabled", indexPath.section, indexPath.row, true);
            }

            default:
                break;
        }

        return null;
    }


    private XUICommonListItemView cellForProduct(GroupListItemObject indexPath) {

        switch (indexPath.row) {
            case 0:
                return CreatItemView("Export All Configurations For Production", "", indexPath.section, indexPath.row, true);

            default:
                break;
        }
        return null;
    }

    private XUICommonListItemView cellForRowAtIndexPath(GroupListItemObject indexPath) {
        switch (indexPath.section) {
            case 1:
                return cellForState(indexPath);
            case 0:
                return cellForAdvertisementdata(indexPath);

            case 2:
                return cellForPeripheral(indexPath);

            case 3:
                return cellForTHSensor(indexPath);

            case 4:
                return cellForINformation(indexPath);

            case 5:
                return cellForProduct(indexPath);

            default:
                break;
        }
        return null;
    }


    private void didSelectRowAtIndexPath(GroupListItemObject indexPath) {
        switch (indexPath.section) {
            case 0:
                switch (indexPath.row) {
                    case 0://1st Advertise Channel
                        openNewPage(AdvConfigureFragment.class, "channel", 1);
                        break;
                    case 1://2nd Advertise Channel
                        openNewPage(AdvConfigureFragment.class, "channel", 2);
                        break;
                    case 2:
                        openNewPage(CustomBeaconConfigureFragment.class);
                        break;
                    default:
                        break;
                }
                break;
            case 2:
                switch (indexPath.row) {
                    case 0://Find The Beacon
                    {
                        showLoadingDialog("Sending command...");
                        MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_PERIPHERAL_SERVICE, MEEBLUE_Defines.MEEBLUE_PERIPHERAL_BUZZER, Data.opCode((byte) 1), new BLEMainDataCallback() {
                            @Override
                            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                                super.onOptionState(device, data, state);
                                if (state) {
                                    dismissDialog();
                                } else {
                                    showErrorDialog("Command send failed");
                                }
                            }
                        });
                    }
                    break;
                    case 2://1st Channel Motion Detect
                    {
                        showLoadingDialog("Setting...");
                        MyApp.Main_Service.ChangeNotifications(MEEBLUE_Defines.MEEBLUE_ACC_SERVICE, MEEBLUE_Defines.MEEBLUE_MOTION_1ST, new BLEMainDataCallback() {
                            @Override
                            public void onNotificatioinState(@NonNull BluetoothDevice device, boolean state) {
                                super.onNotificatioinState(device, state);
                                if (state) {
                                    dismissDialog();
                                    UpdateTable_View();
                                } else {
                                    showErrorDialog("Failed to notify INT1");
                                }
                            }

                            @Override
                            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                                super.onDataReceived(device, data);
                                if (data != null) {
                                    showSuccessDialog("INT1 trigger detected");
                                }
                            }
                        });
                    }
                    break;

                    case 3://2st Channel Motion Detect
                    {
                        showLoadingDialog("Setting...");
                        MyApp.Main_Service.ChangeNotifications(MEEBLUE_Defines.MEEBLUE_ACC_SERVICE, MEEBLUE_Defines.MEEBLUE_MOTION_2ND, new BLEMainDataCallback() {
                            @Override
                            public void onNotificatioinState(@NonNull BluetoothDevice device, boolean state) {
                                super.onNotificatioinState(device, state);
                                if (state) {
                                    dismissDialog();
                                    UpdateTable_View();
                                }else {
                                    showErrorDialog("Failed to notify INT2");
                                }
                            }

                            @Override
                            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                                super.onDataReceived(device, data);
                                if (data != null) {
                                    showSuccessDialog("INT2 trigger detected");
                                }
                            }
                        });
                    }
                    break;
                    case 1://Button Detect
                    {
                        showLoadingDialog("Setting...");
                        MyApp.Main_Service.ChangeNotifications(MEEBLUE_Defines.MEEBLUE_PERIPHERAL_SERVICE, MEEBLUE_Defines.MEEBLUE_PERIPHERAL_BUTTON, new BLEMainDataCallback() {
                            @Override
                            public void onNotificatioinState(@NonNull BluetoothDevice device, boolean state) {
                                super.onNotificatioinState(device, state);
                                if (state) {
                                    dismissDialog();
                                    UpdateTable_View();
                                } else {
                                    showErrorDialog("Failed to notify button");
                                }
                            }

                            @Override
                            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                                super.onDataReceived(device, data);
                                if (data != null) {
                                    if (data.getIntValue(Data.FORMAT_UINT8, 0) == 0x02) {
                                        showSuccessDialog("Button long press trigger detected");
                                    } else if (data.getIntValue(Data.FORMAT_UINT8, 0) == 0x01) {
                                        showSuccessDialog("Button press trigger detected");
                                    }
                                }
                            }
                        });
                    }
                    break;
                    default:
                        break;
                }
                break;

            case 3:
                switch (indexPath.row) {
                    case 6://Histry
                    {
                        if (MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_TH_SERVICE, MEEBLUE_Defines.MEEBLUE_TH_SYNC) != null) {
                            if (MyApp.Main_Service.getBeacon_All_Data().Sensor_Write.current_max_count > 0) {
                                //self performSegueWithIdentifier:"HistoryTH" sender:null;
                                openNewPage(HistorySyncFragment.class);
                                //ToastUtils.toast(getResources().getString(R.string.wait_process_update));
                            } else {
                                showErrorDialog("There is no history data saved. Please refresh and try again");
                            }
                        } else {
                            showDialogLoader("The current firmware version does not support obtain and export historical data of the last two months. To use this function, please update your firmware");
                        }
                    }
                    break;
                    default:
                        break;
                }
                break;
            case 4:
                switch (indexPath.row) {
                    case 0://Name
                    {
                        showInputDialog("length from 1 to 15", InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_NORMAL
                                | InputType.TYPE_TEXT_FLAG_CAP_WORDS, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                if (state && input.length() >= 1 && input.length() <= 15) {
                                    Beacon_All_Data_t temp = MyApp.Main_Service.getBeacon_All_Data();
                                    BLEUtils.ByteMemset(temp.mDevice_Name, 0x00, 20);
                                    BLEUtils.ByteMemcpy(input.getBytes(), 0, temp.mDevice_Name, 0, input.length());
                                    Data_ReloadData(true);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                    break;
                    case 1://Battery Voltage
                        break;
                    case 2://Device Temperture Data
                        break;
                    case 3://Device Humidity Data
                        break;
                    default:
                        break;
                }
                break;
            case 1:
                switch (indexPath.row) {
                    case 0://Beacon State
                        //self performSegueWithIdentifier:"BeaconStateConfigure" sender:null);
                        openNewPage(BeaconStateFragment.class);
                        break;
                    case 1://Advertise Interval
                    {
                        showInputDialog("Range from 160 to 16384", InputType.TYPE_CLASS_NUMBER, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                int InputData = 0;
                                if (state) InputData= Integer.parseInt(input);
                                if (state && InputData >= 160 && InputData <= 16384) {
                                    MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Beacon_Broadcast = InputData;
                                    Data_ReloadData(false);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                    break;
                    case 2://Tx Power Code
                    {
                        showInputDialog("Input aviliable int value", InputType.TYPE_CLASS_TEXT, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                int InputData = 0;
                                if (state) InputData= Integer.parseInt(input);
                                if (state && BLEUtils.isByteValue(InputData)) {
                                    MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_txPower = InputData;
                                    Data_ReloadData(false);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                    break;
                    case 3://Keep Connect Max Time
                    {
                        showInputDialog("Range from 0 to 255", InputType.TYPE_CLASS_NUMBER, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                int InputData = 0;
                                if (state) InputData= Integer.parseInt(input);
                                if (state && InputData >= 0 && InputData <= 255) {
                                    MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Connect_State = InputData;
                                    Data_ReloadData(false);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                    break;
                    case 4://Trigger Mode Adv Time
                    {
                        showInputDialog("Range from 2 to 65534", InputType.TYPE_CLASS_NUMBER, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                int InputData = 0;
                                if (state) InputData= Integer.parseInt(input);
                                if (state && InputData >= 2 && InputData <= 65534) {
                                    MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Trigger_Mode_Adv_Time = InputData;
                                    Data_ReloadData(false);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                    break;
                    case 5: {
                        if (MyApp.Main_Service.getBeacon_All_Data().Beacon_State.Low_Power_Enable == 0x01) {
                            MyApp.Main_Service.getBeacon_All_Data().Beacon_State.Low_Power_Enable = 0x00;
                        } else {
                            MyApp.Main_Service.getBeacon_All_Data().Beacon_State.Low_Power_Enable = 0x01;
                        }
                        Data_ReloadData(false);
                    }

                    break;
                    case 6://1st Channel Motion Level
                        if (MyApp.Main_Service.getCharacteristicByUUID(MEEBLUE_Defines.MEEBLUE_ACC_SERVICE, MEEBLUE_Defines.MEEBLUE_MOTION_1ST) == null) {
                            showInputDialog("Range from 1 to 240", InputType.TYPE_CLASS_NUMBER, new StringInputCallback() {
                                @Override
                                public void onStringReceived(String input, boolean state) {
                                    super.onStringReceived(input, state);
                                    int InputData = 0;
                                    if (state) InputData= Integer.parseInt(input);
                                    if (state && InputData >= 1 && InputData <= 240) {
                                        MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Temp_Save_Interval = InputData;
                                        Data_ReloadData(false);
                                    } else {
                                        showErrorDialog("Data entered is not acceptable");
                                    }
                                }
                            });
                            return;
                        } else {
                            showInputDialog("Range from 2 to 127", InputType.TYPE_CLASS_NUMBER, new StringInputCallback() {
                                @Override
                                public void onStringReceived(String input, boolean state) {
                                    super.onStringReceived(input, state);
                                    int InputData = 0;
                                    if (state) InputData= Integer.parseInt(input);
                                    if (state && InputData >= 2 && InputData <= 127) {
                                        MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Motion_Strength_One = InputData;
                                        Data_ReloadData(false);
                                    } else {
                                        showErrorDialog("Data entered is not acceptable");
                                    }
                                }
                            });
                        }
                        break;
                    case 7://2nd Channel Motion Level
                    {
                        showInputDialog("Range from 2 to 127", InputType.TYPE_CLASS_NUMBER, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                int InputData = 0;
                                if (state) InputData= Integer.parseInt(input);
                                if (state && InputData >= 2 && InputData <= 127) {
                                    MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Motion_Strength_Two = InputData;
                                    Data_ReloadData(false);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                    break;
                    case 8://T&H Data Save Interval
                    {
                        showInputDialog("Range from 1 to 240", InputType.TYPE_CLASS_NUMBER, new StringInputCallback() {
                            @Override
                            public void onStringReceived(String input, boolean state) {
                                super.onStringReceived(input, state);
                                int InputData = 0;
                                if (state) InputData= Integer.parseInt(input);
                                if (state && InputData >= 1 && InputData <= 240) {
                                    MyApp.Main_Service.getBeacon_All_Data().Beacon_State.m_Temp_Save_Interval = InputData;
                                    Data_ReloadData(false);
                                } else {
                                    showErrorDialog("Data entered is not acceptable");
                                }
                            }
                        });
                    }
                    break;
                    default:
                        break;
                }
                break;
            case 5:
                if (indexPath.row == 0) {
                    ExportAllConfigure();
                }
                break;
            default:
                break;
        }
    }

    protected void Data_ReloadData(boolean Name) {
        showLoadingDialog("Trying to configure...");

        if (Name) {
            int lenth = 0;
            byte[] mDevice_Name = new byte[20];
            BLEUtils.ByteMemset(mDevice_Name, 0, 20);
            for (lenth = 0; lenth < 20; lenth++) {
                if (MyApp.Main_Service.getBeacon_All_Data().mDevice_Name[lenth] != 0) {
                    mDevice_Name[lenth] = MyApp.Main_Service.getBeacon_All_Data().mDevice_Name[lenth];
                } else {
                    break;
                }
            }

            MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE, MEEBLUE_Defines.MEEBLUE_MAIN_DEVICE_NAME, Data.from(new String(MyApp.Main_Service.getBeacon_All_Data().mDevice_Name)), new BLEMainDataCallback() {
                @Override
                public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                    super.onOptionState(device, data, state);
                    if (state) {
                        dismissDialog();
                    } else {
                        showErrorDialog("Configure failed");
                    }
                }
            });
        } else {
            Beacon_All_Data_t.Beacon_State_Data_t Beacon_state = MyApp.Main_Service.getBeacon_All_Data().Beacon_State;

            MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE, MEEBLUE_Defines.MEEBLUE_MAIN_BEACON_STATE, new Data(Beacon_state.getCombination()), new BLEMainDataCallback() {
                @Override
                public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                    super.onOptionState(device, data, state);
                    if (state) {
                        dismissDialog();
                        UpdateTable_View();
                    } else {
                        showErrorDialog("Configure failed");
                    }
                }
            });
        }
    }

    private void ExportAllConfigure()
    {
        ArrayList<String> export = new ArrayList<String>();
        BLEUtils.DEBUG_PRINTF("Here");
        showLoadingDialog("Trying to generate csv file");

        Beacon_All_Data_t ALL_DATA = MyApp.Main_Service.getBeacon_All_Data();

        String ALL = "ID"+CSVUtil.csv_segmentation_symbol+"Service UUID"+CSVUtil.csv_segmentation_symbol+"Characteristic UUID"+CSVUtil.csv_segmentation_symbol+"Hex/String Value"+CSVUtil.csv_segmentation_symbol+"Length";
        export.add(ALL);


        String TEMPSTR = BLEUtils.byteToStringValue(ALL_DATA.Firmware_ID, false);
        //firmware version
        export.add(1+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.Device_INFO_SERVER.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.Device_INFO_FIRMWARE_ID.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+TEMPSTR+CSVUtil.csv_segmentation_symbol+TEMPSTR.length());

        //Name
        TEMPSTR = new String(ALL_DATA.mDevice_Name);

        export.add(2+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_MAIN_DEVICE_NAME.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+TEMPSTR+CSVUtil.csv_segmentation_symbol+TEMPSTR.length());

        //State
        TEMPSTR = BLEUtils.byteToStringValue(ALL_DATA.Beacon_State.getCombination(), false);
        export.add(3+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_MAIN_SERVICE.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_MAIN_BEACON_STATE.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+TEMPSTR+CSVUtil.csv_segmentation_symbol+TEMPSTR.length());

        //Adv DATA
        TEMPSTR = BLEUtils.byteToStringValue(BLEUtils.subBytes(ALL_DATA.mAdv_1st_Data, 0, 20), false);
        export.add(4+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_ADV_SERVICE.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_ADV_1ST_BEGIN.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+TEMPSTR+CSVUtil.csv_segmentation_symbol+TEMPSTR.length());

        TEMPSTR = BLEUtils.byteToStringValue(BLEUtils.subBytes(ALL_DATA.mAdv_1st_Data, 20, 12), false);
        export.add(5+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_ADV_SERVICE.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_ADV_1ST_END.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+TEMPSTR+CSVUtil.csv_segmentation_symbol+TEMPSTR.length());


        //Adv DATA
        TEMPSTR = BLEUtils.byteToStringValue(BLEUtils.subBytes(ALL_DATA.mAdv_2nd_Data, 0, 20), false);
        export.add(6+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_ADV_SERVICE.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_ADV_2ST_BEGIN.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+TEMPSTR+CSVUtil.csv_segmentation_symbol+TEMPSTR.length());

        TEMPSTR = BLEUtils.byteToStringValue(BLEUtils.subBytes(ALL_DATA.mAdv_2nd_Data, 20, 12), false);
        export.add(7+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_ADV_SERVICE.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_ADV_2ST_END.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+TEMPSTR+CSVUtil.csv_segmentation_symbol+TEMPSTR.length());

        TEMPSTR = BLEUtils.byteToStringValue(ALL_DATA.m_Custom_Channel_Data, false);
        export.add(7+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_ADV_SERVICE.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+MEEBLUE_Defines.MEEBLUE_ADV_CUSTOM.toString().toUpperCase()+CSVUtil.csv_segmentation_symbol+TEMPSTR+CSVUtil.csv_segmentation_symbol+TEMPSTR.length());


        String File = CSVUtil.exportCsv(CSVUtil.get_configuration_file_path_by_mac(requireContext(), MyApp.Main_Service.get_ble_gatt().getDevice().getAddress().replace(":", "")), export);
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

    protected void UpdateTable_View() {
        //(self.m_table_view reloadData);
        initGroupListView();
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateTable_View();
    }
}
