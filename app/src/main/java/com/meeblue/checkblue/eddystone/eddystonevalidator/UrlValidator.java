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

import android.util.Log;

import com.meeblue.checkblue.fragment.utils.BLELocalInfo;
import java.util.Arrays;


/**
 * Basic validation of an Eddystone-URL frame. <p>
 *
 * @see <a href="https://github.com/google/eddystone/eddystone-url">URL frame specification</a>
 */
public class UrlValidator {

  private static final String TAG = UrlValidator.class.getSimpleName();

  private UrlValidator() {
  }

  public static boolean validate(String deviceAddress, byte[] serviceData, BLELocalInfo beacon) {


    // Tx power should have reasonable values.
    int txPower = (int) serviceData[1];
    if (txPower < Constants.MIN_EXPECTED_TX_POWER || txPower > Constants.MAX_EXPECTED_TX_POWER) {
      return false;
    }

    // The URL bytes should not be all zeroes.
    byte[] urlBytes = Arrays.copyOfRange(serviceData, 2, 20);
    if (Utils.isZeroed(urlBytes)) {
      return false;
    }

    String urlValue = UrlUtils.decodeUrl(serviceData);

    if (urlValue == null || urlValue.length() == 0)
    {
      return false;
    }

    return true;
  }

}
