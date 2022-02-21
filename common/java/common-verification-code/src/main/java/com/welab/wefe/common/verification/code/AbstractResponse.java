/**
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

package com.welab.wefe.common.verification.code;

import java.util.UUID;

/**
 * Response message after sending verification code
 *
 * @author aaron.li
 * @date 2022/1/19 09:52
 **/
public abstract class AbstractResponse<T> {
    /**
     * Response entity data
     */
    public T data;

    public AbstractResponse(T data) {
        this.data = data;
    }

    /**
     * Get request Id
     */
    public String getReqId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Is send success
     *
     * @return true or false
     */
    public abstract boolean success();

    /**
     * Get response content
     *
     * @return Get response body
     */
    public abstract String getRespBody();

    /**
     * Get response message
     */
    public String getMessage() {
        return null;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
