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

public class Constants {

    public static final String RESULT = "mpc_origin_result";
    public static final String ENCRYPT_RESULT = "mpc_encrypt_result";
    public static final String UUID_FIRST_TIME = "mpc_first_time";

    public static class PIR {
        public static final String KEYS = "pir_keys";
        public static final String UUID = "pir_uuid";

        public static final String UUID_FIRST_TIME = "pir_time";
        public static final String RANDOM = "pir_random";
        public static final String RANDOM_LEGAL = "pir_random_legal";
        public static final String ATTEMPT_COUNT = "pir_attempt_count";
        public static final String R = "pir_r";

        public static final String AES_IV = "pir_iv";

        public static final String NAORPINKAS_P = "naorpinkas_p";
        public static final String NAORPINKAS_G = "naorpinkas_g";
        public static final String NAORPINKAS_A = "naorpinkas_a";
        public static final String NAORPINKAS_RANDOM = "naorpinkas_random";
        public static final String NAORPINKAS_CONDITION = "naorpinkas_condition";

        public static final String NAORPINKAS_OT = "naorpinkas_ot";
        public static final String HUACK_OT = "huack_ot";
    }

    public static class SA {
        public static final String SA_KEY = "sa_key";
        public static final String SA_MOD = "sa_mod";
    }

}
