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
package com.welab.wefe.board.service.api.project.fusion.result;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.database.entity.fusion.FusionTaskMySqlModel;
import com.welab.wefe.board.service.service.fusion.FusionResultStorageService;
import com.welab.wefe.board.service.service.fusion.FusionTaskService;
import com.welab.wefe.common.data.storage.common.Constant;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/result/preview", name = "结果预览", desc = "结果预览")
public class ResultPreviewApi extends AbstractApi<ResultPreviewApi.Input, ResultPreviewApi.Output> {

    @Autowired
    FusionTaskService fusionTaskService;


    @Autowired
    FusionResultStorageService fusionResultStorageService;

    @Override
    protected ApiResult<ResultPreviewApi.Output> handle(Input input) throws Exception {
        FusionTaskMySqlModel model = fusionTaskService.findByBusinessId(input.getBusinessId());
        if (model == null) {
            return success();
        }

        if (model.getFusionCount() == 0) {
            return success();
        }


        DataItemModel headerModel = fusionResultStorageService.getByKey(
                Constant.DBName.WEFE_DATA,
                fusionResultStorageService.createRawDataSetTableName(input.getBusinessId()) + ".meta",
                "header"
        );
        List<String> columns = StringUtil.splitWithoutEmptyItem(headerModel.getV().toString().replace("\"", ""), ",");
        List<List<String>> rows = fusionResultStorageService.previewDataSet(
                fusionResultStorageService.createRawDataSetTableName(input.getBusinessId()),
                10
        );

        List<JSONObject> list = new ArrayList<>();
        for (List<String> row : rows) {

            JSONObject item = new JSONObject();
            for (int i = 0; i < columns.size(); i++) {
                if (row.size() > i) {
                    item.put(columns.get(i), row.get(i));
                }
            }

            list.add(item);
        }

        return success(new ResultPreviewApi.Output(columns, list));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "指定操作的businessId", require = true)
        private String businessId;

        //region


        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        //endregion
    }


    public static class Output {

        private List<String> header;
        private List<JSONObject> list;

        public Output(List<String> header, List<JSONObject> list) {
            this.header = header;
            this.list = list;
        }

        //region getter/setter

        public List<String> getHeader() {
            return header;
        }

        public void setHeader(List<String> header) {
            this.header = header;
        }

        public List<JSONObject> getList() {
            return list;
        }

        public void setList(List<JSONObject> list) {
            this.list = list;
        }


        //endregion
    }
}
