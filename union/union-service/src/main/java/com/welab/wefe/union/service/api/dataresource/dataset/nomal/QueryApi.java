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

package com.welab.wefe.union.service.api.dataresource.dataset.nomal;

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataresource.dataset.table.ApiDataSetQueryOutput;
import com.welab.wefe.union.service.service.DataSetService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Jervis
 **/
@Api(path = "data_set/query", name = "data_set_query", allowAccessWithSign = true)
public class QueryApi extends AbstractApi<QueryApi.Input, PageOutput<ApiDataSetQueryOutput>> {
    @Autowired
    private DataSetService dataSetService;

    @Override
    protected ApiResult<PageOutput<ApiDataSetQueryOutput>> handle(QueryApi.Input input) {
        return success(dataSetService.query(input));
    }

    public static class Input extends BaseInput {
        private String id;
        private String memberId;
        private String memberName;
        private String name;
        private Boolean containsY;
        private String tag;
        private Integer pageIndex = 0;
        private Integer pageSize = 10;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getContainsY() {
            return containsY;
        }

        public void setContainsY(Boolean containsY) {
            this.containsY = containsY;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public Integer getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(Integer pageIndex) {
            this.pageIndex = pageIndex;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }
    }

}
