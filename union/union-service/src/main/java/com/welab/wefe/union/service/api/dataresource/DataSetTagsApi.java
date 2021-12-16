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

package com.welab.wefe.union.service.api.dataresource;

import com.welab.wefe.common.data.mongodb.repo.DataResourceMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.dataresource.TagsDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * data resoure tags query
 *
 * @author yuxin.zhang
 **/
@Api(path = "data_resoure/tags/query", name = "resoure_tags_query", rsaVerify = true, login = false)
public class DataSetTagsApi extends AbstractApi<DataSetTagsApi.Input, List<TagsDTO>> {
    @Autowired
    protected DataResourceMongoReop dataResourceMongoReop;


    @Override
    protected ApiResult<List<TagsDTO>> handle(Input input) throws StatusCodeWithException, IOException {
        List<String> tagsList = dataResourceMongoReop.findByDataResourceType(input.dataResourceType);

        Map<String, Long> tagGroupMap = tagsList.stream()
                .flatMap(tags -> Arrays.stream(tags.split(",")))
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.groupingBy(String::trim, Collectors.counting()));

        List<TagsDTO> result = tagGroupMap
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(x -> new TagsDTO(x.getKey(), x.getValue()))
                .collect(Collectors.toList());
        return success(result);
    }


    public static class Input extends BaseInput {
        @Check(require = true)
        private String dataResourceType;

        public String getDataResourceType() {
            return dataResourceType;
        }

        public void setDataResourceType(String dataResourceType) {
            this.dataResourceType = dataResourceType;
        }
    }

}
