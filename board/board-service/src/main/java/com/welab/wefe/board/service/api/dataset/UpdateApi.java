/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

import com.welab.wefe.board.service.dto.vo.DataSetBaseInputModel;
import com.welab.wefe.board.service.service.DataSetService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Zane
 */
@Api(path = "data_set/update", name = "update data set info")
public class UpdateApi extends AbstractNoneOutputApi<UpdateApi.Input> {

    @Autowired
    private DataSetService dataSetService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        dataSetService.update(input);

        return success();
    }

    public static class Input extends DataSetBaseInputModel {

        @Check(name = "数据集Id", require = true)
        private String id;

        @Check(name = "数据集名称", require = true, regex = "^.{4,50}$")
        private String name;

        @Check(name = "标签", require = true)
        private List<String> tags;

        @Check(name = "描述")
        private String description;

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

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        //endregion

    }
}
