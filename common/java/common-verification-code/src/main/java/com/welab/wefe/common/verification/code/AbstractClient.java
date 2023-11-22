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

package com.welab.wefe.common.verification.code;

import java.util.Map;

/**
 * Send verification code client
 *
 * @author aaron.li
 * @date 2022/1/19 09:51
 **/
public abstract class AbstractClient {
    /**
     * Extend param
     */
    private Map<String, Object> extendParams;

    public AbstractClient(Map<String, Object> extendParams) {
        this.extendParams = extendParams;
    }

    /**
     * Send verification code sms
     *
     * @param mobile           target phone number
     * @param verificationCode verification code
     * @return response message
     * @throws Exception
     */
    public abstract AbstractResponse send(String mobile, String verificationCode) throws Exception;

    public Map<String, Object> getExtendParams() {
        return extendParams;
    }

    public void setExtendParams(Map<String, Object> extendParams) {
        this.extendParams = extendParams;
    }
}
