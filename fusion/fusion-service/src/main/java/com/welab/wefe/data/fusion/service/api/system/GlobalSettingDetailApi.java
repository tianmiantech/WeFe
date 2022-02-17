/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.data.fusion.service.api.system;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.dto.entity.globalconfig.MemberInfoModel;
import com.welab.wefe.data.fusion.service.service.globalconfig.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "system/global_setting/detail", name = "获取 全局设置 详情")
public class GlobalSettingDetailApi extends AbstractNoneInputApi<MemberInfoModel> {

    @Autowired
    GlobalConfigService globalConfigService;

    @Override
    protected ApiResult<MemberInfoModel> handle() throws StatusCodeWithException {
        return success(globalConfigService.getMemberInfo());
    }


    public static class Output extends AbstractApiOutput {

        String partnerId;

        String partnerName;

        String rsaPrivateKey;

        String rsaPublicKey;

        Integer openSocketPort;

        //region getter/setter

        public String getPartnerId() {
            return partnerId;
        }

        public void setPartnerId(String partnerId) {
            this.partnerId = partnerId;
        }

        public String getPartnerName() {
            return partnerName;
        }

        public void setPartnerName(String partnerName) {
            this.partnerName = partnerName;
        }

        public String getRsaPrivateKey() {
            return rsaPrivateKey;
        }

        public void setRsaPrivateKey(String rsaPrivateKey) {
            this.rsaPrivateKey = rsaPrivateKey;
        }

        public String getRsaPublicKey() {
            return rsaPublicKey;
        }

        public void setRsaPublicKey(String rsaPublicKey) {
            this.rsaPublicKey = rsaPublicKey;
        }

        public Integer getOpenSocketPort() {
            return openSocketPort;
        }

        public void setOpenSocketPort(Integer openSocketPort) {
            this.openSocketPort = openSocketPort;
        }

        //endregion
    }
}
