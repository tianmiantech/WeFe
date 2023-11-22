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

import com.welab.wefe.mpc.util.EncryptUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author eval
 */
public class RandomPhoneNum {

    final static int LEN_PHONE = 8;
    private static String[] start = {
            "130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
            "145", "147", "149",
            "150", "151", "152", "153", "155", "156", "157", "158", "159",
            "166",
            "170", "171", "172", "173", "175", "176", "177", "178",
            "180", "181", "182", "183", "184", "185", "186", "187", "188", "189",
            "198", "199"};

    public static List<String> getKeys(int n, String k, String encryptType) {
        List<String> keys = new ArrayList<>(n);
        int count = 0;
        while (count < n) {
            String phone = getPhoneNum();
            phone = EncryptUtil.encrypt(phone, encryptType);
            if (phone.equals(k) || keys.contains(phone)) {
                continue;
            }
            count += 1;
            keys.add(phone);
        }
        return keys;
    }

    public static String getPhoneNum() {
        Random r = new Random();
        StringBuilder builder = new StringBuilder();
        //通过Math.random（）*数组长度获得数组下标，从而随机出前三位的号段
        builder.append(start[r.nextInt(start.length)]);
        //循环剩下的位数
        for (int i = 0; i < LEN_PHONE; i++) {
            builder.append(r.nextInt(10));
        }

        return builder.toString();
    }
}
