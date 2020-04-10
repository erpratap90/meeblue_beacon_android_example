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
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.meeblue.checkblue.MyApp;
import com.meeblue.checkblue.R;
import com.meeblue.checkblue.utils.LocalBroadcastManager;
import com.meeblue.checkblue.ble.callback.BLEMainDataCallback;
import com.meeblue.checkblue.ble.profile.MEEBLUE_Defines;
import com.meeblue.checkblue.core.BaseFragment;
import com.meeblue.checkblue.eddystone.eddystonevalidator.Constants;
import com.meeblue.checkblue.eddystone.eddystonevalidator.UriBeacon;
import com.meeblue.checkblue.fragment.utils.BLEUtils;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.widget.edittext.materialedittext.MaterialEditText;
import com.xuexiang.xui.widget.edittext.materialedittext.validation.RegexpValidator;
import com.xuexiang.xui.widget.tabbar.TabControlView;

import java.net.URISyntaxException;
import java.util.UUID;

import butterknife.BindView;
import no.nordicsemi.android.ble.data.Data;

/**
 * author alvin
 * since 2020-03-17
 */
@Page(name = "Adv Data")
public class AdvConfigureFragment extends BaseFragment {

    @BindView(R.id.configure_type)
    TabControlView configure_type;

    @BindView(R.id.configure_flag)
    TabControlView configure_flag;

    @BindView(R.id.title_show)
    TextView title_show;

    private int configure_type_ID = 0;
    private int head_start_type_ID = 0;


    @Override
    protected int getLayoutId() {
        return R.layout.configure_settings;
    }

    @Override
    protected void initViews() {
        addActionRightAction("Save");
        if (getArguments().getInt("channel") == 1) {
            SetTitle("1st Channel");
        } else {
            SetTitle("2nd Channel");
        }
        MaterialEditText text = findViewById(R.id.eddystone_url_str);
        text.setText("https://www.");
        initValidationEt();
        initTabControlView();
    }

    @Override
    protected void onRightActionClick() {
        super.onRightActionClick();

        switch (configure_type_ID) {
            case 0:
                ConfigureAsiBeacon();
                break;
            case 1:
                ConfigureEddyStone(false);
                break;
            case 2:
                ConfigureEddyStone(true);
                break;
            case 3:
                ConfigureCustom();
                break;
        }
    }

    @Override
    protected void initListeners() {

    }

    private void showViewsByID(int ID) {
        configure_type_ID = ID;
        switch (ID) {
            case 0:
                title_show.setText("iBeacon Parameters:");
                findViewById(R.id.ibeacon_configure).setVisibility(View.VISIBLE);
                findViewById(R.id.eddystone_uid).setVisibility(View.GONE);
                findViewById(R.id.eddystone_url).setVisibility(View.GONE);
                findViewById(R.id.custom_configure).setVisibility(View.GONE);
                break;
            case 1:
                title_show.setText("Eddystone-UID Parameters:");
                findViewById(R.id.ibeacon_configure).setVisibility(View.GONE);
                findViewById(R.id.eddystone_uid).setVisibility(View.VISIBLE);
                findViewById(R.id.eddystone_url).setVisibility(View.GONE);
                findViewById(R.id.custom_configure).setVisibility(View.GONE);
                break;
            case 2:
                title_show.setText("Eddystone-URI Parameters:");
                findViewById(R.id.ibeacon_configure).setVisibility(View.GONE);
                findViewById(R.id.eddystone_uid).setVisibility(View.GONE);
                findViewById(R.id.eddystone_url).setVisibility(View.VISIBLE);
                findViewById(R.id.custom_configure).setVisibility(View.GONE);
                break;
            case 3:
                title_show.setText("Custom Beacon Parameters:");
                findViewById(R.id.ibeacon_configure).setVisibility(View.GONE);
                findViewById(R.id.eddystone_uid).setVisibility(View.GONE);
                findViewById(R.id.eddystone_url).setVisibility(View.GONE);
                findViewById(R.id.custom_configure).setVisibility(View.VISIBLE);
                break;
        }
    }

    private void initTabControlView() {
        try {
            configure_type.setItems(ResUtils.getStringArray(R.array.configure_param_option), ResUtils.getStringArray(R.array.configure_param_value));
            configure_type.setDefaultSelection(0);

            configure_flag.setItems(ResUtils.getStringArray(R.array.data_param_option), ResUtils.getStringArray(R.array.data_param_value));
            configure_flag.setDefaultSelection(0);
            showViewsByID(0);
            title_show.setText("iBeacon Parameters:");
        } catch (Exception e) {
            e.printStackTrace();
        }
        configure_type.setOnTabSelectionChangedListener(new TabControlView.OnTabSelectionChangedListener() {
            @Override
            public void newSelection(String title, String value) {
                select_show_type = value;
                showViewsByID(Integer.parseInt(value));

            }
        });

        configure_flag.setOnTabSelectionChangedListener(new TabControlView.OnTabSelectionChangedListener() {
            @Override
            public void newSelection(String title, String value) {
                head_start_type_ID = Integer.parseInt(value);
            }
        });

        int length = decode_string();

    }

    private void initValidationEt() {
        MaterialEditText temp = findViewById(R.id.ibeacon_uuid);
        temp.addValidator(new RegexpValidator("UUID Format", "[0-9A-Fa-f]{8}(-[0-9A-Fa-f]{4}){3}-[0-9A-Fa-f]{12}"));
        temp = findViewById(R.id.major);
        temp.addValidator(new RegexpValidator("Only numbers", "\\d+"));
        temp = findViewById(R.id.minor);
        temp.addValidator(new RegexpValidator("Only numbers", "\\d+"));
        temp = findViewById(R.id.measured_power);
        temp.addValidator(new RegexpValidator("Only numbers", "\\d+"));

        temp = findViewById(R.id.namespace);
        temp.addValidator(new RegexpValidator("10 Bytes Hex Value", "[0-9A-Fa-f]{20}"));
        temp = findViewById(R.id.instance);
        temp.addValidator(new RegexpValidator("6 Bytes Hex Value", "[0-9A-Fa-f]{12}"));

        temp = findViewById(R.id.custom_first);
        temp.addValidator(new RegexpValidator("Hex Only (0-9 A-F a-f)", "[0-9A-Fa-f]*"));
        temp = findViewById(R.id.custom_end);
        temp.addValidator(new RegexpValidator("Hex Only (0-9 A-F a-f)", "[0-9A-Fa-f]*"));
    }


    private void change_type_auto(int index) {
        select_show_type = "" + index;
        showViewsByID(index);
        configure_type.setSelection(select_show_type);
    }

    private int decode_string() {
        byte[] Adv_data;
        if (getArguments().getInt("channel") == 1) {
            Adv_data = MyApp.Main_Service.getBeacon_All_Data().mAdv_1st_Data;
        } else {
            Adv_data = MyApp.Main_Service.getBeacon_All_Data().mAdv_2nd_Data;
        }

        if (BLEUtils.unsignedByteToInt(Adv_data[0]) > 31) {
            return 31;
        }
        BLEUtils.DEBUG_PRINTF(BLEUtils.byteToStringValue(Adv_data, false));
        if (BLEUtils.unsignedByteToInt(Adv_data[0]) == 0x1E
                && BLEUtils.unsignedByteToInt(Adv_data[5]) == 0xFF
                && BLEUtils.unsignedByteToInt(Adv_data[6]) == 0x4C
                && BLEUtils.unsignedByteToInt(Adv_data[7]) == 0x00
                && BLEUtils.unsignedByteToInt(Adv_data[8]) == 0x02
                && BLEUtils.unsignedByteToInt(Adv_data[9]) == 0x15)//iBeacon
        {
            BLEUtils.DEBUG_PRINTF("is i'beacon");
            String UUID = BLEUtils.byteToStringValue(BLEUtils.subBytes(Adv_data, 10, 16), false);
            UUID = UUID.toUpperCase();
            MaterialEditText text = findViewById(R.id.ibeacon_uuid);
            text.setText(UUID.substring(0, 8) + "-" + UUID.substring(8, 12) + "-" + UUID.substring(12, 16) + "-" + UUID.substring(16, 20) + "-" + UUID.substring(20, 32));
            text = findViewById(R.id.major);
            text.setText("" + (BLEUtils.unsignedByteToInt(Adv_data[26]) * 256 + BLEUtils.unsignedByteToInt(Adv_data[27])));
            text = findViewById(R.id.minor);
            text.setText("" + (BLEUtils.unsignedByteToInt(Adv_data[28]) * 256 + BLEUtils.unsignedByteToInt(Adv_data[29])));
            text = findViewById(R.id.measured_power);
            text.setText("" + BLEUtils.unsignedByteToInt(Adv_data[30]));
            change_type_auto(0);
            return BLEUtils.unsignedByteToInt(Adv_data[0]);
        } else if (BLEUtils.unsignedByteToInt(Adv_data[0]) == 0x1D) {
            byte[] TEMPDATA = BLEUtils.subBytes(Adv_data, 12, BLEUtils.unsignedByteToInt(Adv_data[0]) - 11);

            if (TEMPDATA.length == 18 && TEMPDATA[0] == Constants.UID_FRAME_TYPE)//UID
            {

                MaterialEditText text = findViewById(R.id.eddystone_power);
                text.setText("" + TEMPDATA[1]);

                String UIDString = BLEUtils.byteToStringValue(BLEUtils.subBytes(TEMPDATA, 2, 16), false);
                text = findViewById(R.id.namespace);
                text.setText(UIDString.substring(0, 20));

                text = findViewById(R.id.instance);
                text.setText(UIDString.substring(20, 32));

                change_type_auto(1);
                return BLEUtils.unsignedByteToInt(Adv_data[0]);
            }
        } else if (Adv_data.length > 14 && BLEUtils.unsignedByteToInt(Adv_data[0]) > 14 && BLEUtils.unsignedByteToInt(Adv_data[0]) <= 31)//URI
        {
            BLEUtils.DEBUG_PRINTF(BLEUtils.byteToStringValue(BLEUtils.subBytes(Adv_data, 1, BLEUtils.unsignedByteToInt(Adv_data[0])), false));
            UriBeacon url = UriBeacon.parseFromBytes(BLEUtils.subBytes(Adv_data, 1, BLEUtils.unsignedByteToInt(Adv_data[0])));
            BLEUtils.DEBUG_PRINTF(url.toString());
            if (url != null) {
                url.getUriString();
                MaterialEditText text = findViewById(R.id.eddystone_url_str);
                text.setText(url.getUriString());
                text = findViewById(R.id.eddystone_url_power);
                text.setText("" + url.getTxPowerLevel());
                change_type_auto(2);
                return BLEUtils.unsignedByteToInt(Adv_data[0]);
            }
        }

        configure_flag.setSelection("0");
        head_start_type_ID = 0;
        if (BLEUtils.unsignedByteToInt(Adv_data[3]) == 0x06) {
            configure_flag.setSelection("1");
            head_start_type_ID = 1;
        }

        if (BLEUtils.unsignedByteToInt(Adv_data[0]) > 14 + 3 && BLEUtils.unsignedByteToInt(Adv_data[0]) <= 31) {
            String All = BLEUtils.byteToStringValue(Adv_data, false);
            MaterialEditText text = findViewById(R.id.custom_first);
            text.setText(All.substring(8, 36));

            text = findViewById(R.id.custom_end);
            text.setText(All.substring(36, Adv_data[0] * 2 + 2));
        } else if (BLEUtils.unsignedByteToInt(Adv_data[0]) <= 31 && BLEUtils.unsignedByteToInt(Adv_data[0]) >= 4) {
            String All = BLEUtils.byteToStringValue(Adv_data, false);
            MaterialEditText text = findViewById(R.id.custom_first);
            text.setText(All.substring(8, Adv_data[0] * 2 - 6));
            text = findViewById(R.id.custom_end);
            text.setText("");
        } else {
            MaterialEditText text = findViewById(R.id.custom_first);
            text.setText("");
            text = findViewById(R.id.custom_end);
            text.setText("");
        }
        change_type_auto(3);

        return BLEUtils.unsignedByteToInt(Adv_data[0]);
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
        String UUIDStr = ((MaterialEditText) findViewById(R.id.ibeacon_uuid)).getEditValue().replace("-", "");
        String Major = ((MaterialEditText) findViewById(R.id.major)).getEditValue();
        String Minor = ((MaterialEditText) findViewById(R.id.minor)).getEditValue();
        String Messured = ((MaterialEditText) findViewById(R.id.measured_power)).getEditValue();

        if (!((MaterialEditText) findViewById(R.id.ibeacon_uuid)).validate()) {
            showErrorDialog("Invalid iBeacon Proximity UUID");
            return;
        }
        if (!(Major.length()>0 && Integer.parseInt(Major) <= 65535)){
            showErrorDialog("Major unacceptable");
            return;
        }  else if (!(Minor.length()>0 && Integer.parseInt(Minor) <= 65535)) {
            showErrorDialog("Minor unacceptable");
            return;
        } else if (!(Messured.length()>0 && Integer.parseInt(Messured) <= 255 && Integer.parseInt(Messured) >= 128)) {
            showErrorDialog("Measured Power Value unacceptable");
            return;
        }

        //start configure

        String ALLString = "1E0201061AFF4C000215"+UUIDStr
                + BLEUtils.byteToStringValue( (Integer.parseInt(Major)>>8) & 0xFF, false) + BLEUtils.byteToStringValue( (Integer.parseInt(Major)&0xFF), false)//Major
                + BLEUtils.byteToStringValue( (Integer.parseInt(Minor)>>8) & 0xFF, false) + BLEUtils.byteToStringValue( (Integer.parseInt(Minor)&0xFF), false)//Minor
                + BLEUtils.byteToStringValue( Integer.parseInt(Messured), false);//Power

        WriteData(BLEUtils.HexString2Bytes(ALLString));
    }

    public void ConfigureEddyStone(boolean URL) {

        if (URL)//URI
        {
            if (!CheckEddystoneURIFormat()) {
                return;
            }

            //UriBeacon temp = new UriBeacon((byte) 0x10, (byte) Integer.parseInt(((MaterialEditText) findViewById(R.id.eddystone_url_power)).getEditValue()), ((MaterialEditText) findViewById(R.id.eddystone_url_str)).getEditValue());
            UriBeacon temp = null;
            try {
                temp = new UriBeacon.Builder().uriString(((MaterialEditText) findViewById(R.id.eddystone_url_str)).getEditValue())
                        .txPowerLevel((byte) Integer.parseInt(((MaterialEditText) findViewById(R.id.eddystone_url_power)).getEditValue()))
                        .flags((byte) 0x10).build();

            }catch (URISyntaxException e)
            {
                BLEUtils.DEBUG_PRINTF("wrong uri");
            }

            if (temp == null)
            {
                showErrorDialog("Could not decode URI");
                return;
            }


            String URI_STR = BLEUtils.byteToStringValue(temp.toByteArray(), false);
            String Header = "020106";
            String ALL = BLEUtils.byteToStringValue((Header.length()+URI_STR.length())/2, false) + Header + URI_STR;


            if (ALL.length() > 64 || ALL.length() <= 14) {
                showErrorDialog("Data length unacceptable");
                return;
            }

            WriteData(BLEUtils.HexString2Bytes(ALL));
            return;
        } else if (CheckEddystoneUIDFormat()) {
            String Start = "0201060303AAFE1516AAFE00";
            String dataMiddle = BLEUtils.byteToStringValue(Integer.parseInt(((MaterialEditText) findViewById(R.id.eddystone_power)).getEditValue()), false);
            String dataEnd = ((MaterialEditText) findViewById(R.id.namespace)).getEditValue();
            String data_end = ((MaterialEditText) findViewById(R.id.instance)).getEditValue();

            String ALL = Start + dataMiddle + dataEnd + data_end;
            int length = ALL.length()/2;

            ALL = BLEUtils.byteToStringValue(length, false) + ALL;


            if (ALL.length() != 60) {
                showErrorDialog("Data length unacceptable");
                return;
            }

            WriteData(BLEUtils.HexString2Bytes(ALL));
            return;
        }
    }

    private boolean ConfigureCustom() {

        if (((MaterialEditText) findViewById(R.id.custom_first)).getEditValue().length() > 28 || ((MaterialEditText) findViewById(R.id.custom_first)).getEditValue().length() == 0) {
            showErrorDialog("Data input unacceptable");
            return false;
        }

        String Text = ((MaterialEditText) findViewById(R.id.custom_first)).getEditValue() + ((MaterialEditText) findViewById(R.id.custom_end)).getEditValue();

        if (Text.length() == 0 || Text.length() > 56) {
            showErrorDialog("Data input unacceptable");
            return false;
        }

        String Start = "020106";
        if (head_start_type_ID == 0) {
            Start = "020104";
        }

        String All = Start + Text;
        int length = All.length()/2;

        All = BLEUtils.byteToStringValue(length, false) + All;


        if (All.length() > 64) {
            showErrorDialog("Data Input unacceptable");
            return false;
        }
        WriteData(BLEUtils.HexString2Bytes(All));
        return true;
    }


    private boolean CheckEddystoneURIFormat() {

        if (((MaterialEditText) findViewById(R.id.eddystone_url_str)).getEditValue().length() == 0) {
            showErrorDialog("Uri is null");
            return false;
        }

        if (((MaterialEditText) findViewById(R.id.eddystone_url_power)).getEditValue() == null || ((MaterialEditText) findViewById(R.id.eddystone_url_power)).getEditValue().length() == 0) {
            showErrorDialog("Power value must be input");
            return false;
        }
        if (Integer.parseInt(((MaterialEditText) findViewById(R.id.eddystone_url_power)).getEditValue()) < -255 || Integer.parseInt(((MaterialEditText) findViewById(R.id.eddystone_url_power)).getEditValue()) > 255) {
            showErrorDialog("Power value out of range");
            return false;
        }

        return true;
    }

    private boolean CheckEddystoneUIDFormat() {

        if (!((MaterialEditText) findViewById(R.id.namespace)).validate()) {
            showErrorDialog("Namespace ID format is incorrect");
            return false;
        }
        if (!((MaterialEditText) findViewById(R.id.instance)).validate()) {
            showErrorDialog("Instance ID format is incorrect");
            return false;
        }
        if (((MaterialEditText) findViewById(R.id.eddystone_power)).getEditValue() == null || ((MaterialEditText) findViewById(R.id.eddystone_power)).getEditValue().length() == 0) {
            showErrorDialog("Power value must be input");
            return false;
        }
        if (Integer.parseInt(((MaterialEditText) findViewById(R.id.eddystone_power)).getEditValue()) < -255 || Integer.parseInt(((MaterialEditText) findViewById(R.id.eddystone_power)).getEditValue()) > 255) {
            showErrorDialog("Power value out of range");
            return false;
        }

        return true;
    }


    private void WriteData(byte[] value)
    {
        BLEUtils.DEBUG_PRINTF(BLEUtils.byteToStringValue(value, true));
        showLoadingDialog("Trying to configure...");
        UUID Char_S = MEEBLUE_Defines.MEEBLUE_ADV_1ST_BEGIN;

        if (getArguments().getInt("channel") == 2) {
            Char_S = MEEBLUE_Defines.MEEBLUE_ADV_2ST_BEGIN;
        }

        if (value.length <= 20)
        {
            MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_ADV_SERVICE, Char_S, new Data(value), new BLEMainDataCallback() {
                @Override
                public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                    super.onOptionState(device, data, state);
                    if (state) {
                        if (getArguments().getInt("channel") == 1) {
                            BLEUtils.ByteMemset(MyApp.Main_Service.getBeacon_All_Data().mAdv_1st_Data, 0, 32);
                            BLEUtils.ByteMemcpy(value, 0, MyApp.Main_Service.getBeacon_All_Data().mAdv_1st_Data, 0, value.length);
                        }
                        else{
                            BLEUtils.ByteMemset(MyApp.Main_Service.getBeacon_All_Data().mAdv_2nd_Data, 0, 32);
                            BLEUtils.ByteMemcpy(value, 0, MyApp.Main_Service.getBeacon_All_Data().mAdv_2nd_Data, 0, value.length);
                        }
                        showSuccessDialog("Configure Finished");
                        popToBack();
                    } else {
                        showErrorDialog("Configure failed");
                    }
                }
            });
        }
        else{
            MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_ADV_SERVICE, Char_S, new Data(BLEUtils.subBytes(value, 0, 20)), new BLEMainDataCallback() {
                @Override
                public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                    super.onOptionState(device, data, state);
                    if (state) {
                        UUID Char_E = MEEBLUE_Defines.MEEBLUE_ADV_1ST_END;
                        if (getArguments().getInt("channel") == 2) {
                            Char_E = MEEBLUE_Defines.MEEBLUE_ADV_2ST_END;
                        }
                        MyApp.Main_Service.writeCharacteristic(MEEBLUE_Defines.MEEBLUE_ADV_SERVICE, Char_E, new Data(BLEUtils.subBytes(value, 20, value.length-20)), new BLEMainDataCallback() {
                            @Override
                            public void onOptionState(@NonNull BluetoothDevice device, Data data, boolean state) {
                                super.onOptionState(device, data, state);
                                if (state) {
                                    if (getArguments().getInt("channel") == 1) {
                                        BLEUtils.ByteMemset(MyApp.Main_Service.getBeacon_All_Data().mAdv_1st_Data, 0, 32);
                                        BLEUtils.ByteMemcpy(value, 0, MyApp.Main_Service.getBeacon_All_Data().mAdv_1st_Data, 0, value.length);
                                    }
                                    else{
                                        BLEUtils.ByteMemset(MyApp.Main_Service.getBeacon_All_Data().mAdv_2nd_Data, 0, 32);
                                        BLEUtils.ByteMemcpy(value, 0, MyApp.Main_Service.getBeacon_All_Data().mAdv_2nd_Data, 0, value.length);
                                    }
                                    showSuccessDialog("Configure Finished");
                                    popToBack();
                                } else {
                                    showErrorDialog("Configure failed");
                                }
                            }
                        });
                    } else {
                        showErrorDialog("Configure failed");
                    }
                }
            });
        }
    }
}
