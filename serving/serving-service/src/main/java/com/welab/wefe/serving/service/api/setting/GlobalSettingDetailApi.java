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

package com.welab.wefe.serving.service.api.setting;

import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.repository.GlobalSettingRepository;
import com.welab.wefe.serving.service.utils.ModelMapper;
import com.welab.wefe.serving.service.utils.ServiceUtil;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "global_setting/detail", name = "get global settings")
public class GlobalSettingDetailApi extends AbstractNoneInputApi<GlobalSettingDetailApi.Output> {

    @Autowired
    GlobalSettingRepository repo;

    @Override
    protected ApiResult<GlobalSettingDetailApi.Output> handle() {
        GlobalSettingDetailApi.Output output = ModelMapper.map(repo.singleton(), Output.class);
        output.setRsaPrivateKey(ServiceUtil.around(output.getRsaPrivateKey(), 10, 10));
        output.setRsaPublicKey(ServiceUtil.around(output.getRsaPublicKey(), 10, 10));
        return success(output);
    }


    public static class Output extends AbstractApiOutput {

        private String memberId;

        private String memberName;

        private String rsaPublicKey;

        private String rsaPrivateKey;

        private String gatewayUri;


        //region getter/setter

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public String getRsaPublicKey() {
            return rsaPublicKey;
        }

        public void setRsaPublicKey(String rsaPublicKey) {
            this.rsaPublicKey = rsaPublicKey;
        }

        public String getRsaPrivateKey() {
            return rsaPrivateKey;
        }

        public void setRsaPrivateKey(String rsaPrivateKey) {
            this.rsaPrivateKey = rsaPrivateKey;
        }

        public String getGatewayUri() {
            return gatewayUri;
        }

        public void setGatewayUri(String gatewayUri) {
            this.gatewayUri = gatewayUri;
        }


        //endregion
    }
}
