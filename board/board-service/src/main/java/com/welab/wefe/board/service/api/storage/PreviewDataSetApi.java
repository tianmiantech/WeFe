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

package com.welab.wefe.board.service.api.storage;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.database.repository.data_resource.TableDataSetRepository;
import com.welab.wefe.board.service.service.DataSetStorageService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zane
 */
@Api(path = "storage/table_data_set/preview", name = "View data sets in storage")
public class PreviewDataSetApi extends AbstractApi<PreviewDataSetApi.Input, PreviewDataSetApi.Output> {

    @Autowired
    DataSetStorageService dataSetStorageService;
    @Autowired
    TableDataSetRepository dataSetRepository;
    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        TableDataSetMysqlModel model = dataSetRepository.findById(input.getId()).orElse(null);
        if (model == null) {
            return success();
        }
        List<String> columns = StringUtil.splitWithoutEmptyItem(model.getColumnNameList(), ",");
        List<List<String>> rows;
        if (!model.isDerivedResource()) {
            rows = dataSetStorageService.previewDataSet(model.getStorageNamespace(), model.getStorageResourceName(), 100);
        } else {
            rows = getRowsFromFlow(model);
        }

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

        return success(new Output(columns, list));
    }

    /**
     * View the data of the derived data set from flow service
     */
    private List<List<String>> getRowsFromFlow(TableDataSetMysqlModel model) throws StatusCodeWithException {
        String url = globalConfigService.getFlowConfig().intranetBaseUri
                + String.format(
                "/data_set/view?table_name=%s&table_namespace=%s",
                model.getStorageResourceName(),
                model.getStorageNamespace()
        );

        HttpResponse response = HttpRequest
                .create(url)
                .get();

        if (!response.success()) {
            StatusCode.RPC_ERROR.throwException(response.getError());
        }

        return response
                .getBodyAsJson()
                .getJSONObject("data")
                .toJavaObject(FlowOutput.class)
                .list;
    }


    static class FlowOutput {
        public List<List<String>> list;
    }


    public static class Input extends AbstractApiInput {
        @Check(require = true, name = "数据集 Id")
        private String id;

        //region getter/setter

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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
