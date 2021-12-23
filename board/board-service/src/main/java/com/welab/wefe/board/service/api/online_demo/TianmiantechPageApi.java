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

package com.welab.wefe.board.service.api.online_demo;

import com.welab.wefe.board.service.base.OnlineDemoApi;
import com.welab.wefe.board.service.onlinedemo.TianmiantechService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.UrlUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Map;

/**
 * @author zane
 */
@OnlineDemoApi
@Api(path = "tianmiantech/page_url", name = "create tianmiantech website page url")
public class TianmiantechPageApi extends AbstractApi<TianmiantechPageApi.Input, TianmiantechPageApi.Output> {
    @Autowired
    private TianmiantechService tianmiantechService;
    @Value("${tianmiantech.website.base-url:}")
    private String TIANMIANTECH_WEBSITE_BASE_URL;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {

        input.params.put("timestamp", System.currentTimeMillis());
        input.params.put("sign", tianmiantechService.sign(input.params));
        String url = UrlUtil.appendQueryParameters(TIANMIANTECH_WEBSITE_BASE_URL + input.page, input.params);
        return success(new Output(url));
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String page;
        public Map<String, Object> params;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            if (!page.startsWith("/")) {
                page = "/" + page;
            }
        }
    }

    public static class Output {
        public String url;

        public Output(String url) {
            this.url = url;
        }
    }
}
