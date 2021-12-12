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

package com.welab.wefe.union.service.api.dataresource.dataset.nomal;

import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetQueryOutput;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataset.ApiDataSetQueryOutput;
import com.welab.wefe.union.service.mapper.DataSetMapper;
import com.welab.wefe.union.service.service.DataSetContractService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jervis
 **/
@Api(path = "data_set/query", name = "data_set_query", rsaVerify = true, login = false)
public class QueryApi extends AbstractApi<QueryApi.Input, PageOutput<ApiDataSetQueryOutput>> {
    @Autowired
    protected DataSetMongoReop dataSetMongoReop;
    @Autowired
    protected DataSetContractService mDataSetContractService;
    protected DataSetMapper mDataSetMapper = Mappers.getMapper(DataSetMapper.class);

    @Override
    protected ApiResult<PageOutput<ApiDataSetQueryOutput>> handle(QueryApi.Input input) {
        PageOutput<DataSetQueryOutput> pageOutput = dataSetMongoReop.findCurMemberCanSee(mDataSetMapper.transferInput(input));

        List<ApiDataSetQueryOutput> list = pageOutput.getList().stream()
                .map(mDataSetMapper::transferOutput)
                .collect(Collectors.toList());

        return success(new PageOutput<>(
                pageOutput.getPageIndex(),
                pageOutput.getTotal(),
                pageOutput.getPageSize(),
                pageOutput.getTotalPage(),
                list
        ));
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
