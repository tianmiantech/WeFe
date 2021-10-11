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

package com.welab.wefe.data.fusion.service.api.partner;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.service.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jacky.jiang
 */
@Api(path = "partner/update", name = "添加合作伙伴", desc = "添加合作伙伴", login = false)
public class UpdateApi extends AbstractNoneOutputApi<UpdateApi.Input> {

    @Autowired
    PartnerService partnerService;

    @Override
    protected ApiResult handler(Input input) throws StatusCodeWithException {
        partnerService.update(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "id", require = true)
        String id;

        @Check(name = "合作伙伴Id", require = true)
        String partnerId;

        @Check(name = "合作伙伴名称", require = true)
        String name;

        @Check(name = "合作伙伴名称", require = true)
        String rsaPublicKey;

        @Check(name = "请求路径", require = true)
        String baseUrl;

        @Check(name = "对方ip地址", require = true)
        String socketIp;

        @Check(name = "对方端口地址", require = true)
        int socketPort;

        @Check(name = "我方开放地址", require = true)
        int openSocketPort;


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPartnerId() {
            return partnerId;
        }

        public void setPartnerId(String partnerId) {
            this.partnerId = partnerId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRsaPublicKey() {
            return rsaPublicKey;
        }

        public void setRsaPublicKey(String rsaPublicKey) {
            this.rsaPublicKey = rsaPublicKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
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

        public int getOpenSocketPort() {
            return openSocketPort;
        }

        public void setOpenSocketPort(int openSocketPort) {
            this.openSocketPort = openSocketPort;
        }
    }
}