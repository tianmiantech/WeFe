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

package com.welab.wefe.common.web.api.dev;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.ApiListJsonFormatter;
import com.welab.wefe.common.web.util.ApiListMarkdownFormatter;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

/**
 * @author Zane
 */
@Api(path = "apis", name = "获取 api 列表", login = false)
public class Apis extends AbstractApi<Apis.Input, ResponseEntity<?>> {


    @Override
    protected ApiResult<ResponseEntity<?>> handle(Input input) throws StatusCodeWithException, IOException {


        switch (input.format) {
            case "json":
                List<JObject> apiList = ApiListJsonFormatter.format(list);
                ResponseEntity<ApiResult<Object>> response1 = ResponseEntity
                        .ok()
                        .header("content-type", "application/json; charset=utf-8")
                        .body(ApiResult.ofSuccess(Output.of(apiList.size(), apiList)));

                return success(response1);

            case "markdown":
                String markdown = ApiListMarkdownFormatter.format(list);
                ResponseEntity<String> response2 = ResponseEntity
                        .ok()
                        .header("content-type", "text/markdown; charset=utf-8")
                        .body(markdown);
            default:
                String markdown = ApiListMarkdownFormatter.format(list);
                ResponseEntity<String> response2 = ResponseEntity
                        .ok()
                        .header("content-type", "text/markdown; charset=utf-8")
                        .body(markdown);

                return success(response2);
        }
    }


    public static class Output extends AbstractApiOutput {
        public int size;
        public List<JObject> list;

        public static Output of(int size, List<JObject> list) {
            Output output = new Output();
            output.size = size;
            output.list = list;
            return output;
        }
    }

    public static class Input extends AbstractApiInput {
        public String format = "json";
    }
}
