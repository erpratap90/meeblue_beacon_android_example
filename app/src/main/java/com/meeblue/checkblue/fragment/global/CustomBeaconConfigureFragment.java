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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.meeblue.checkblue.MyApp;
import com.meeblue.checkblue.R;
import com.meeblue.checkblue.utils.LocalBroadcastManager;
import com.meeblue.checkblue.ble.callback.BLEMainDataCallback;
import com.meeblue.checkblue.ble.profile.MEEBLUE_Defines;
import com.meeblue.checkblue.core.BaseFragment;
import com.meeblue.checkblue.fragment.utils.BLEUtils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.edittext.materialedittext.MaterialEditText;
import com.xuexiang.xui.widget.edittext.materialedittext.validation.RegexpValidator;

import no.nordicsemi.android.ble.data.Data;

/**
 * author alvin
 * since 2020-03-17
 */
@Page(name = "Custom Beacon")
public class CustomBeaconConfigureFragment extends BaseFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.custom_configure_settings;
    }

    @Override
    protected void initViews() {
        addActionRightAction("Save");
        SetTitle("Custom");
        initValidationEt();
        decode_string();
    }

    @Override
    protected void onRightActionClick() {
        super.onRightActionClick();

        ConfigureAsiBeacon();
    }

    @Override
    protected void initListeners() {

    }

    private void initValidationEt() {
        MaterialEditText temp = findViewById(R.id.ibeacon_uuid);
        temp.addValidator(new RegexpValidator("UUID Format", "[0-9A-Fa-f]{12}"));
        temp = findViewById(R.id.major);
        temp.addValidator(new RegexpValidator("Only numbers", "\\d+"));
        temp = findViewById(R.id.minor);
        temp.addValidator(new RegexpValidator("Only numbers", "\\d+"));
    }

    private int decode_string() {
        byte[] Adv_data = MyApp.Main_Service.getBeacon_All_Data().m_Custom_Channel_Data;

        String UUID = BLEUtils.byteToStringValue(BLEUtils.subBytes(Adv_data, 0, 6), false);
        MaterialEditText text = findViewById(R.id.ibeacon_uuid);
        text.setText(UUID.toUpperCase());

        text = findViewById(R.id.major);
        text.setText("" + (BLEUtils.unsignedByteToInt(Adv_data[6]) * 256 + BLEUtils.unsignedByteToInt(Adv_data[7])));
        text = findViewById(R.id.minor);
        text.setText("" + (BLEUtils.unsignedByteToInt(Adv_data[8]) * 256 + BLEUtils.unsignedByteToInt(Adv_data[9])));

        return 10;
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

    public void ConfigureAsiBeacon() {
        String UUIDStr = ((MaterialEditText) findViewById(R.id.ibeacon_uuid)).getEditValue();
        String Major = ((MaterialEditText) findViewById(R.id.major)).getEditValue();
        String Minor = ((MaterialEditText) findViewById(R.id.minor)).getEditValue();

        if (!((MaterialEditText) findViewById(R.id.ibeacon_uuid)).validate()) {
            showErrorDialog("Invalid Mini UUID");
            return;
        }
        if (!(Major.length() > 0 && Integer.parseInt(Major) <= 65535)) {
            showErrorDialog("Major unacceptable");
            return;
        } else if (!(Minor.length() > 0 && Integer.parseInt(Minor) <= 65535)) {
            showErrorDialog("Minor unacceptable");
            return;
        }

        //start configure
        String ALLString = UUIDStr
                + BLEUtils.byteToStringValue((Integer.parseInt(Major) >> 8) & 0xFF, false) + BLEUtils.byteToStringValue((Integer.parseInt(Major) & 0xFF), false)//Major
                + BLEUtils.byteToStringValue((Integer.parseInt(Minor) >> 8) & 0xFF, false) + BLEUtils.byteToStringValue((Integer.parseInt(Minor) & 0xFF), false);//Minor

        if (ALLString.length() != 20)
        {
            showErrorDialog("The value input are unacceptable");
            return;
        }

        WriteData(BLEUtils.HexString2Bytes(ALLString));
    }

    private void WriteData(byte[] value) {
        BLEUtils.DEBUG_PRINTF(BLEUtils.byteToStringValue(value, true));
        showLoadingDialog("Trying to configure...");
        if (value.length == 10) {
            MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_ADV_SERVICE, MEEBLUE_Defines.MEEBLUE_ADV_CUSTOM, new Data(value), new BLEMainDataCallback() {
                @Override
                public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                    super.onOptionState(device, data, state);
                    if (state) {
                        BLEUtils.ByteMemset(MyApp.Main_Service.getBeacon_All_Data().m_Custom_Channel_Data, 0, 10);
                        BLEUtils.ByteMemcpy(value, 0, MyApp.Main_Service.getBeacon_All_Data().m_Custom_Channel_Data, 0, value.length);
                        showSuccessDialog("Configure Finished");
                        popToBack();
                    } else {
                        showErrorDialog("Configure failed");
                    }
                }
            });
        } else {
            showErrorDialog("The value are unacceptable");
        }
    }
}
