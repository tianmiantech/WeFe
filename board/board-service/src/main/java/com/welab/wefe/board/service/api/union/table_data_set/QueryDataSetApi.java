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

package com.welab.wefe.board.service.api.union.table_data_set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

/**
 * @author Zane
 */
@Api(path = "union/table_data_set/query", name = "Query data set from union")
public class QueryDataSetApi extends AbstractApi<QueryDataSetApi.Input, JSONObject> {

    @Autowired
    private UnionService unionService;

    @Override
    protected ApiResult<JSONObject> handle(Input input) throws StatusCodeWithException {
        JSONObject result = unionService.queryDataSets(input);

        JSONObject data = result.getJSONObject("data");

        // Rename the fields of the data from the union so that the front end can access it uniformly.
        JSONArray list = data.getJSONArray("list");
        if (list == null) {
            data.put("list", new ArrayList<>());
        }

        return unionApiResultToBoardApiResult(result);
    }

    public static class Input extends PagingInput {

        private String dataSetId;

        @Check(name = "数据集名称")
        private String name;

        @Check(name = "标签名称")
        private String tag;

        private String memberId;

        private Boolean containsY;


        //region getter/setter


        public String getDataSetId() {
            return dataSetId;
        }

        public void setDataSetId(String dataSetId) {
            this.dataSetId = dataSetId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public Boolean getContainsY() {
            return containsY;
        }

        public void setContainsY(Boolean containsY) {
            this.containsY = containsY;
        }


        //endregion
    }
}
