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
package com.welab.wefe.serving.service.api.system;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.Caller;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.globalconfig.GlobalConfigService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 * @date 2022/5/7
 */
@Api(path = "system/update_rsa_key_by_board",
        name = "同步board密钥",
        allowAccessWithSign = true,
        domain = Caller.Board
)
public class UpdateRsaKeyByBoardApi extends AbstractNoneOutputApi<UpdateRsaKeyByBoardApi.Input> {

    @Autowired
    GlobalConfigService globalConfigService;

    @Override
    protected ApiResult handler(Input input) throws StatusCodeWithException {
        globalConfigService.updateRsaKeyByBoard(input);
        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "公钥", require = true)
        private String rsaPublicKey;

        @Check(name = "私钥", require = true)
        private String rsaPrivateKey;


        //region getter/setter


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


        //endregion
    }
}
