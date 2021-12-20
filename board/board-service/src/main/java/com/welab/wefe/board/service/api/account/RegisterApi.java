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

package com.welab.wefe.board.service.api.account;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.dto.vo.AccountInputModel;
import com.welab.wefe.board.service.service.account.AccountService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.service.CaptchaService;
import com.welab.wefe.common.wefe.enums.BoardUserSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "account/register", name = "register", login = false)
public class RegisterApi extends AbstractNoneOutputApi<RegisterApi.Input> {
    @Autowired
    private AccountService accountService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        accountService.register(input, BoardUserSource.board_register);
        return success();
    }


    public static class Input extends AccountInputModel {

        @Check(require = true, desc = "验证码标识")
        private String key;

        @Check(require = true, desc = "验证码")
        private String code;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            if (Launcher.getBean(Config.class).getEnvName().isProductionEnv()) {
                // Verification code verification
                if (!CaptchaService.verify(key, code)) {
                    throw new StatusCodeWithException("验证码错误！", StatusCode.PARAMETER_VALUE_INVALID);
                }
            }


        }

        //region getter/setter

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        //endregion
    }
}
