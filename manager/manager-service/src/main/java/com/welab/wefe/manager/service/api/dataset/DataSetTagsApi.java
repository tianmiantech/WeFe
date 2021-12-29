/*
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

package com.welab.wefe.manager.service.api.dataset;

import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetTagsQueryOutput;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.tag.ApiDataSetTagsQueryOutput;
import com.welab.wefe.manager.service.dto.tag.DataSetTagsQueryInput;
import com.welab.wefe.manager.service.dto.tag.TagsDTO;
import com.welab.wefe.manager.service.service.DataSetContractService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * data set tags query
 *
 * @author yuxin.zhang
 **/
@Api(path = "data_set/tags/query", name = "dataset_tags_query")
public class DataSetTagsApi extends AbstractApi<DataSetTagsQueryInput, ApiDataSetTagsQueryOutput> {
    @Autowired
    protected DataSetMongoReop dataSetMongoReop;
    @Autowired
    protected DataSetContractService mDatasetContractService;

    @Override
    protected ApiResult<ApiDataSetTagsQueryOutput> handle(DataSetTagsQueryInput input) {
        List<DataSetTagsQueryOutput> list = dataSetMongoReop.findByTags(input.getTagName());

        ApiDataSetTagsQueryOutput output = new ApiDataSetTagsQueryOutput();
        output.setTagList(convertTagList(input.getTagName(), list));

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

}
