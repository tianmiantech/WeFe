/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.constant;

import com.welab.wefe.common.util.StringUtil;

/**
 * Secret key type
 *
 * @author aaron.li
 * @date 2022/1/14 13:46
 **/
public enum SecretKeyType {
    /**
     * RSA secret Key type
     */
    rsa,
    /**
     * GM sm2 secret Key type
     */
    sm2;

    public static SecretKeyType get(String name) {
        if (StringUtil.isEmpty(name)) {
            return rsa;
        }
        SecretKeyType[] secretKeyTypes = SecretKeyType.values();
        for (SecretKeyType secretKeyType : secretKeyTypes) {
            if (secretKeyType.name().equals(name)) {
                return secretKeyType;
            }
        }
        return rsa;
    }
}
