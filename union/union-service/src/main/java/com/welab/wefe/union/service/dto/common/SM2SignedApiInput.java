/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.union.service.dto.common;

import com.welab.wefe.common.web.dto.AbstractApiInput;

/**
 * @author yuxin.zhang
 */
public class SM2SignedApiInput extends AbstractApiInput {

    private String currentBlockchainNodeId;
    private String sign;
    private String data;

    public String getCurrentBlockchainNodeId() {
        return currentBlockchainNodeId;
    }

    public void setCurrentBlockchainNodeId(String currentBlockchainNodeId) {
        this.currentBlockchainNodeId = currentBlockchainNodeId;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
