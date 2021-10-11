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

package com.welab.wefe.common.web.api.dev;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.HostUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiInput;
import com.welab.wefe.common.web.util.HttpServletRequestUtil;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zane
 */
@Api(path = "http_bin", name = "查看 http 请求对象", login = false)
public class HttpBinApi extends AbstractApi<NoneApiInput, HttpBinApi.Output> {

    @Override
    protected ApiResult<Output> handle(NoneApiInput input) throws StatusCodeWithException {
        if (input.request == null) {
            return success();
        }

        Output output = new Output();

        Enumeration<String> headerNames = input.request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            String value = input.request.getHeader(header);
            output.headers.put(header, value);
        }

        output.clientIp = HttpServletRequestUtil.getClientIp(input.request);

        output.url = input.request.getRequestURL().toString();

        output.serverIp = HostUtil.getLocalIp();

        return success(output);
    }

    public static class Output {
        public Map<String, String> headers = new HashMap<>();
        public String clientIp;
        public String serverIp;
        public String url;
    }
}
