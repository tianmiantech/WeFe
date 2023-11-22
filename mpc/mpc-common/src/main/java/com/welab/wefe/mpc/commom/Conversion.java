/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.mpc.commom;


import com.welab.wefe.mpc.pir.protocol.nt.group.GroupElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author eval
 * @Date 2020-11-18
 **/
public class Conversion {

    public static List<Integer> intToBinaryRepresentation(BigInteger integer) {
        String binStr = integer.toString(2);
        int binLen = binStr.length();
        List<Integer> exponent = new ArrayList<>();
        for (int i = 0; i < binLen; i++) {
            if (binStr.charAt(i) == '1') {
                exponent.add(binLen - i - 1);
            }
        }
        return exponent;
    }

    public static byte[] intToBytes(BigInteger integer) {
        return hexStringToBytes(integer.toString(16));
    }

    public static BigInteger bytesToInt(byte[] bytes) {
        return new BigInteger(bytesToHexString(bytes), 16);
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }

        return buf.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.trim().equals("")) {
            return new byte[0];
        }
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length() / 2; i++) {
            String subStr = hexString.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

    public static String groupElementToString(GroupElement element) {
        return element.x.val.toString() + "," + element.y.val.toString();
    }

}
