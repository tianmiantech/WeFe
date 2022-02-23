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

package com.welab.wefe.data.fusion.service.api.dataset;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.dto.entity.dataset.DataSetPreviewOutputModel;
import com.welab.wefe.data.fusion.service.enums.DataResourceSource;
import com.welab.wefe.data.fusion.service.service.DataSourceService;
import com.welab.wefe.data.fusion.service.service.dataset.DataSetService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

/**
 * @author Jakcy.jiang
 */
@Api(path = "data_set/preview", name = "Preview the dataset file")
public class PreviewApi extends AbstractApi<PreviewApi.Input, DataSetPreviewOutputModel> {

    @Autowired
    DataSourceService dataSourceService;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    protected DataSource dataSource;

    @Override
    protected ApiResult<DataSetPreviewOutputModel> handle(Input input) throws Exception {
        return success(dataSetService.preview(input));
    }


    //region dto

    public static class Input extends AbstractApiInput {

        @Check(name = "数据id")
        private String id;

        private String filename;

        private DataResourceSource dataResourceSource;

        private String sql;

        private String rows;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public String getRows() {
            return rows;
        }

        public void setRows(String rows) {
            this.rows = rows;
        }
    }

    //endregion
}
