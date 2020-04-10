// Copyright 2015 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.meeblue.checkblue.eddystone.eddystonevalidator;

import com.meeblue.checkblue.fragment.utils.BLELocalInfo;

import java.util.Arrays;


/**
 * Basic validation of an Eddystone-UID frame. <p>
 *
 * @see <a href="https://github.com/google/eddystone/eddystone-uid">UID frame specification</a>
 */
public class UidValidator {

  private static final String TAG = UidValidator.class.getSimpleName();

  private UidValidator() {
  }

  public static boolean validate(String deviceAddress, byte[] serviceData, BLELocalInfo beacon) {

    // Tx power should have reasonable values.
    byte FRAME_TYPE = serviceData[0];
    if (FRAME_TYPE != Constants.UID_FRAME_TYPE) return false;

    int txPower = (int) serviceData[1];

    if (txPower < Constants.MIN_EXPECTED_TX_POWER || txPower > Constants.MAX_EXPECTED_TX_POWER) {
      return false;
    }

    // The namespace and instance bytes should not be all zeroes.
    byte[] uidBytes = Arrays.copyOfRange(serviceData, 2, 18);
    if (Utils.isZeroed(uidBytes)) {
      return false;
    }

    // Last two bytes in frame are RFU and should be zeroed.
    byte[] rfu = Arrays.copyOfRange(serviceData, 18, 20);
    if (rfu[0] != 0x00 || rfu[1] != 0x00) {
      return false;
    }

    return true;
  }
}
