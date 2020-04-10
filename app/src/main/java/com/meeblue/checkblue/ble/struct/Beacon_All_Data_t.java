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
package com.meeblue.checkblue.ble.struct;

import com.meeblue.checkblue.fragment.utils.BLEUtils;

import no.nordicsemi.android.ble.data.Data;

public class Beacon_All_Data_t extends Object {


    public Beacon_TH_Data_t m_TH_Data = new Beacon_TH_Data_t();
    public Beacon_State_Data_t Beacon_State = new Beacon_State_Data_t();
    public Sensor_Configure_Data_t Sensor_Write = new Sensor_Configure_Data_t();

    public byte[] mDevice_Name = new byte[20];
    public byte[] mAdv_1st_Data = new byte[32];
    public byte[] mAdv_2nd_Data = new byte[32];
    public byte[] m_Custom_Channel_Data = new byte[10];

    public int m_batt_voltage;
    public byte[] Systemp_ID = new byte[6];
    public byte[] Firmware_ID = new byte[20];

    public byte[] getm_batt_voltage() {
        byte[] temp = new byte[2];
        System.arraycopy(BLEUtils.UnsignedInt16ToBytes(m_batt_voltage, false), 0, temp, 0, 2);
        return temp;
    }

    public void setm_batt_voltage(byte[] value) {
        m_batt_voltage = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 0, 1), false);
    }



    public String dataType(int Channel)
    {
        String temp =  "";
        if (Channel == 1) {
            temp = BLEUtils.byteToStringValue(mAdv_1st_Data, false);
        }
        else if (Channel == 2)
        {
            temp = BLEUtils.byteToStringValue(mAdv_2nd_Data, false);
        }

        temp = temp.toUpperCase();
        if (temp.contains("1AFF4C000215") || temp.contains("1BFF4C000215")) {
            return "iBeacon";
        }
        else if ((temp.contains("0303AAFE") && temp.contains("16AAFE")) || (temp.contains("0303D8FE") && temp.contains("16D8FE")))
        {
            return "Eddystone";
        }
        else{
            return "Custom";
        }
    }


    public class Beacon_State_Data_t extends Object {
        public int m_Beacon_Broadcast;
        public int m_Trigger_Mode_Adv_Time;//2-65535
        public int m_Beacon_State;
        public int m_txPower;
        public int m_Connect_State;//uint:minutes, 0:no limit
        public int m_Motion_Strength_One;
        public int m_Motion_Strength_Two;
        public int m_Temp_Save_Interval;
        public int Low_Power_Enable;
        public byte[] Reseved = new byte[5];
        public long m_current_timeSnape;

        public byte[] getCombination() {
            byte[] temp = new byte[20];

            int length = 0;
            System.arraycopy(BLEUtils.UnsignedInt16ToBytes(m_Beacon_Broadcast, false), 0, temp, length, 2);
            length += 2;
            System.arraycopy(BLEUtils.UnsignedInt16ToBytes(m_Trigger_Mode_Adv_Time, false), 0, temp, length, 2);
            length += 2;
            System.arraycopy(BLEUtils.UnsignedInt8ToBytes(m_Beacon_State), 0, temp, length, 1);
            length += 1;
            System.arraycopy(BLEUtils.UnsignedInt8ToBytes(m_txPower), 0, temp, length, 1);
            length += 1;
            System.arraycopy(BLEUtils.UnsignedInt8ToBytes(m_Connect_State), 0, temp, length, 1);
            length += 1;
            System.arraycopy(BLEUtils.UnsignedInt8ToBytes(m_Motion_Strength_One), 0, temp, length, 1);
            length += 1;
            System.arraycopy(BLEUtils.UnsignedInt8ToBytes(m_Motion_Strength_Two), 0, temp, length, 1);
            length += 1;
            System.arraycopy(BLEUtils.UnsignedInt8ToBytes(m_Temp_Save_Interval), 0, temp, length, 1);
            length += 1;
            System.arraycopy(BLEUtils.UnsignedInt8ToBytes(Low_Power_Enable), 0, temp, length, 1);
            length += 1;
            System.arraycopy(Reseved, 0, temp, length, 5);
            length += 5;
            System.arraycopy(BLEUtils.UnsignedInt32ToBytes(m_current_timeSnape, false), 0, temp, length, 4);
            return temp;
        }

        public boolean setCombination(byte[] value) {
            if (value.length != 20) {
                BLEUtils.DEBUG_PRINTF("Value length unaviliable");
                return false;
            }
            m_Beacon_Broadcast = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 0, 2), false);
            m_Trigger_Mode_Adv_Time = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 2, 2), false);
            m_Beacon_State = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 4, 1), false);
            //m_txPower =  BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 5, 1), false);
            m_txPower = new Data(BLEUtils.subBytes(value, 5, 1)).getIntValue(Data.FORMAT_SINT8, 0);
            m_Connect_State = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 6, 1), false);
            m_Motion_Strength_One = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 7, 1), false);
            m_Motion_Strength_Two = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 8, 1), false);
            m_Temp_Save_Interval = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 9, 1), false);
            Low_Power_Enable = BLEUtils.getUnsignedInt8(BLEUtils.subBytes(value, 10, 1), false);
            Reseved = BLEUtils.subBytes(value, 11, 5);
            m_current_timeSnape = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 16, 4), false);

            return true;
        }
    }

    public class TH_Data_t extends Object  {
        public  int m_temperture;
        public int m_humidity;//5-65535

        public byte[] getCombination() {
            byte[] temp = new byte[4];
            System.arraycopy(BLEUtils.UnsignedInt16ToBytes(m_temperture, false), 0, temp, 0, 2);
            System.arraycopy(BLEUtils.UnsignedInt16ToBytes(m_humidity, false), 0, temp, 2, 2);
            return temp;
        }

        public void setCombination(byte[] value) {
            m_temperture = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 0, 2), false);
            m_humidity = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 2, 2), false);
        }
    }

    public class Beacon_TH_Data_t extends Object  {
        public long m_time;
        public int m_temperture;
        public  int m_humidity;

        public byte[] getCombination() {
            byte[] temp = new byte[8];
            System.arraycopy(BLEUtils.UnsignedInt32ToBytes(m_time, false), 0, temp, 0, 4);
            System.arraycopy(BLEUtils.UnsignedInt16ToBytes(m_temperture, false), 0, temp, 4, 2);
            System.arraycopy(BLEUtils.UnsignedInt16ToBytes(m_humidity, false), 0, temp, 6, 2);
            return temp;
        }

        public void setCombination(byte[] value) {
            m_time = BLEUtils.getUnsignedInt32(BLEUtils.subBytes(value, 0, 4), false);
            m_temperture = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 4, 2), false);
            m_humidity = BLEUtils.getUnsignedInt16(BLEUtils.subBytes(value, 6, 2), false);
        }

    }





}
