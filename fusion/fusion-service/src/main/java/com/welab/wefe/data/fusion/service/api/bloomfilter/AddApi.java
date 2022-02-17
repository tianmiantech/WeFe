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

package com.welab.wefe.data.fusion.service.api.bloomfilter;


import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.enums.DataResourceSource;
import com.welab.wefe.data.fusion.service.service.bloomfilter.BloomFilterAddService;
import com.welab.wefe.data.fusion.service.utils.primarykey.FieldInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * @author jacky.jiang
 */
@Api(path = "filter/add", name = "添加过滤器", desc = "添加过滤器", login = true)
public class AddApi extends AbstractApi<AddApi.Input, AddApi.BloomfilterAddOutput> {

    @Autowired
    private BloomFilterAddService filterAddService;

    @Override
    protected ApiResult<BloomfilterAddOutput> handle(Input input) throws Exception {
        return success(filterAddService.addFilter(input));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "数据集名称", require = true, regex = "^.{4,30}$", messageOnInvalid = "数据集名称长度不能少于4，不能大于30")
        private String name;

        @Check(name = "描述", regex = "^.{0,3072}$", messageOnInvalid = "你写的描述太多了~")
        private String description;

        @Check(messageOnEmpty = "请指定数据集文件")
        private String filename;

        @Check(require = true)
        private DataResourceSource dataResourceSource;

        @Check(require = true, name = "是否需要去重")
        private boolean deduplication;

        @Check(name = "数据源id")
        private String dataSourceId;

        @Check(name = "sql脚本")
        private String sql;

        @Check(name = "选择的id特征列")
        private List<String> rows;

        @Check(name = "主键处理")
        private List<FieldInfo> fieldInfoList;

        @Check(name = "id")
        private String id ;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

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

        public boolean isDeduplication() {
            return deduplication;
        }

        public void setDeduplication(boolean deduplication) {
            this.deduplication = deduplication;
        }

        public String getDataSourceId() {
            return dataSourceId;
        }

        public void setDataSourceId(String dataSourceId) {
            this.dataSourceId = dataSourceId;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public List<String> getRows() {
            return rows;
        }

        public void setRows(List<String> rows) {
            this.rows = rows;
        }

        public List<FieldInfo> getFieldInfoList() {
            return fieldInfoList;
        }

        public void setFieldInfoList(List<FieldInfo> fieldInfoList) {
            this.fieldInfoList = fieldInfoList;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class BloomfilterAddOutput extends AbstractApiOutput {
        private String dataSourceId;

        public BloomfilterAddOutput() {

        }

        public String getDataSourceId() {
            return dataSourceId;
        }

        public void setDataSourceId(String dataSourceId) {
            this.dataSourceId = dataSourceId;
        }
    }
}

