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

package com.welab.wefe.board.service.api.dataset;

import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_set.DataSetOutputModel;
import com.welab.wefe.board.service.service.DataSetService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "data_set/query", name = "query data set")
public class QueryApi extends AbstractApi<QueryApi.Input, PagingOutput<DataSetOutputModel>> {

    @Autowired
    private DataSetService dataSetService;

    @Override
    protected ApiResult<PagingOutput<DataSetOutputModel>> handle(Input input) throws StatusCodeWithException {
        return success(dataSetService.query(input));
    }


    public static class Input extends PagingInput {

        private String id;

        @Check(name = "数据集名称")
        private String name;

        @Check(name = "标签")
        private String tag;

        @Check(name = "是否包含 Y 值")
        private Boolean containsY;

        @Check(name = "上传者")
        private String creator;

        //region getter/setter


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public Boolean getContainsY() {
            return containsY;
        }

        public void setContainsY(Boolean containsY) {
            this.containsY = containsY;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        //endregion
    }
}
