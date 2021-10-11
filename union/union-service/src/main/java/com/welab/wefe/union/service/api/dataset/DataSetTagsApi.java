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

package com.welab.wefe.union.service.api.dataset;

import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetTagsQueryOutput;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataset.ApiTagsQueryOutput;
import com.welab.wefe.union.service.dto.dataset.TagsDTO;
import com.welab.wefe.union.service.service.DatasetContractService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * data set tags query
 *
 * @author yuxin.zhang
 **/
@Api(path = "data_set/tags/query", name = "dataset_tags_query", rsaVerify = true, login = false)
public class DataSetTagsApi extends AbstractApi<DataSetTagsApi.Input, ApiTagsQueryOutput> {
    @Autowired
    protected DataSetMongoReop dataSetMongoReop;
    @Autowired
    protected DatasetContractService mDatasetContractService;

    @Override
    protected ApiResult<ApiTagsQueryOutput> handle(DataSetTagsApi.Input input) {
        List<DataSetTagsQueryOutput> list = dataSetMongoReop.findByTags(input.getTagName());

        ApiTagsQueryOutput output = new ApiTagsQueryOutput();
        output.setTagList(convertTagList(input.tagName, list));

        return success(output);
    }

    private List<TagsDTO> convertTagList(String tagName, List<DataSetTagsQueryOutput> tagList) {

        Map<String, Long> map = new HashMap<>();
        // Split the tags field of the database record, remove duplicates, and sort
        tagList
                .stream()
                .map(DataSetTagsQueryOutput::getTags)
                .flatMap(tag -> Arrays.stream(tag.split(",")))
                .filter(StringUtil::isNotEmpty)
                .filter(word -> {
                    if (StringUtil.isEmpty(tagName)) {
                        return true;
                    } else {
                        return word.contains(tagName);
                    }
                })
                .collect(Collectors.toList())
                .forEach(word -> map.put(word, map.getOrDefault(word, 0L) + 1));


        List<TagsDTO> list = new ArrayList<>();
        map.forEach((word, count) -> list.add(new TagsDTO(word, count)));

        return list;

    }


    public static class Input extends BaseInput {
        private String memberId;
        private String tagName;
        private int pageIndex;
        private int pageSize;

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
    }
}
