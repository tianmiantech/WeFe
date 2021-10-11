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

package com.welab.wefe.data.fusion.service.api.dataset;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.enums.DataResourceSource;
import com.welab.wefe.data.fusion.service.service.GetHeadersService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author jacky.jiang
 */
@Api(path = "data_set/get_headers", name = "添加数据集", desc = "获取特征字段", login = false)
public class GetHeadersApi extends AbstractApi<GetHeadersApi.Input, GetHeadersApi.Output> {

    @Autowired
    private GetHeadersService getHeadersService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        List<String> headers = getHeadersService.getHeaders(input);
        return success(new Output(headers));
    }


    public static class Input extends AbstractApiInput {

        @Check(messageOnEmpty = "请指定数据集文件")
        private String filename;

        @Check(require = true)
        private DataResourceSource dataResourceSource;

        @Check(name = "sql脚本")
        private String sql;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public DataResourceSource getDataResourceSource() {
            return dataResourceSource;
        }

        public void setDataResourceSource(DataResourceSource dataResourceSource) {
            this.dataResourceSource = dataResourceSource;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }
    }

    public static class Output {
        private List<String> headers;

        public Output(List<String> headers) {
            this.headers = headers;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public void setHeaders(List<String> headers) {
            this.headers = headers;
        }
    }
}
