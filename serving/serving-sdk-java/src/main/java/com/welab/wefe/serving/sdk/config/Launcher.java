/**
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

package com.welab.wefe.serving.sdk.config;


import java.io.Serializable;

/**
 * @author hunter.zhao
 */
public class Launcher implements Serializable {

    private static boolean inited = false;

    public static synchronized void init(String memberId, String rsaPrivateKey, String rsaPublicKey) {

        if (inited) {
            return;
        }

        Config.MEMBER_ID = memberId;
        Config.RSA_PRIVATE_KEY = rsaPrivateKey;
        Config.RSA_PUBLIC_KEY = rsaPublicKey;

        inited = true;
    }
}
