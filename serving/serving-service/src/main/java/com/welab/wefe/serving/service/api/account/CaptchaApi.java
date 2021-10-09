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

package com.welab.wefe.serving.service.api.account;

import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.Captcha;
import com.welab.wefe.common.web.service.CaptchaService;

/**
 * @author hunter.zhao
 */
@Api(path = "account/captcha", name = "Get verification code", login = false)
public class CaptchaApi extends AbstractNoneInputApi<CaptchaApi.Output> {

    @Override
    protected ApiResult<Output> handle() {
        Captcha captcha = CaptchaService.get();

        Output output = new Output();
        output.setKey(captcha.getKey());
        output.setImage(captcha.getImage());
        return success(output);
    }

    public static class Output extends AbstractApiOutput {

        private String image;

        private String key;


        //region getter/setter

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }


        //endregion
    }
}
