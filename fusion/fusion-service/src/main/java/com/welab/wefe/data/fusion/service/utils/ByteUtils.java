/*
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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
package com.welab.wefe.data.fusion.service.utils;

/**
 * @author hunter.zhao
 * @date 2022/2/25
 */
public class ByteUtils {
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    public static int byteArrayToInt(byte[] b){
        int res = 0;
        for(int i=0;i<b.length;i++){
            res += (b[i] & 0xff) << ((3-i)*8);
        }
        return res;
    }

    public static void main(String[] args) {

        for (int i=0; i<10000; i++){
            int r  = byteArrayToInt(intToByteArray(i));
            System.out.println("i: " + i + " byteArrayToInt : " + r);
        }
    }
}
