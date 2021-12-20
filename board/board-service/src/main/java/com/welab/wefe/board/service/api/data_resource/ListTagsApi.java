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

package com.welab.wefe.board.service.api.data_resource;


import com.welab.wefe.board.service.database.repository.data_resource.TableDataSetRepository;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Zane
 */
@Api(path = "data_resource/tags", name = "all of the table data set tags")
public class ListTagsApi extends AbstractApi<ListTagsApi.Input, TreeMap<String, Long>> {

    @Autowired
    TableDataSetRepository repo;

    @Override
    protected ApiResult<TreeMap<String, Long>> handle(Input input) throws StatusCodeWithException {

        Map<String, Long> result = CacheObjects
                .getDataResourceTags(input.dataResourceType)
                .entrySet()
                .stream()
                .filter(x -> {
                    return StringUtil.isNotEmpty(input.tag) ||
                            x.getKey().toLowerCase().contains(x.getKey().toLowerCase());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return success(new TreeMap<>(result));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "tag关键字，用于模糊搜索（联想输入）")
        public String tag;

        @Check(name = "资源类型")
        public DataResourceType dataResourceType;

    }
}
