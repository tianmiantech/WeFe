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

package com.welab.wefe.data.fusion.service.api.thirdparty;

import com.welab.wefe.data.fusion.service.enums.CallbackType;
import com.welab.wefe.data.fusion.service.service.CallbackService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "thirdparty/callback", name = "接收消息接口",rsaVerify = true)
public class CallbackApi extends AbstractNoneOutputApi<CallbackApi.Input> {
    @Autowired
    CallbackService callbackService;

    @Override
    protected ApiResult handler(CallbackApi.Input input) throws StatusCodeWithException {
        callbackService.callback(input);
        return success();
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "指定操作的businessId", require = true)
        private String businessId;

        @Check(name = "消息类型", require = true)
        private CallbackType type;

        @Check(name = "对齐数据量")
        private Integer dataCount;

        @Check(name = "对齐server的IP")
        private String socketIp;

        @Check(name = "对齐server的端口")
        private int socketPort;

        public CallbackType getType() {
            return type;
        }

        public void setType(CallbackType type) {
            this.type = type;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public Integer getDataCount() {
            return dataCount;
        }

        public void setDataCount(Integer dataCount) {
            this.dataCount = dataCount;
        }

        public String getSocketIp() {
            return socketIp;
        }

        public void setSocketIp(String socketIp) {
            this.socketIp = socketIp;
        }

        public int getSocketPort() {
            return socketPort;
        }

        public void setSocketPort(int socketPort) {
            this.socketPort = socketPort;
        }
    }
}
