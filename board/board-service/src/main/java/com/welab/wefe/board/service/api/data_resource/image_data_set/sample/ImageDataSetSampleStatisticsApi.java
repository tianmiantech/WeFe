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
package com.welab.wefe.board.service.api.data_resource.image_data_set.sample;


import com.welab.wefe.board.service.service.data_resource.image_data_set.ImageDataSetSampleService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zane
 * @date 2021/11/15
 */
@Api(path = "image_data_set_sample/statistics", name = "statistics the data set labels distribute", login = false)
public class ImageDataSetSampleStatisticsApi extends AbstractApi<ImageDataSetSampleStatisticsApi.Input, ImageDataSetSampleStatisticsApi.Output> {
    @Autowired
    private ImageDataSetSampleService imageDataSetSampleService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
        Output output = imageDataSetSampleService.statistics(input.dataSetId);
        return success(output);
    }

    public static class Output {

        @Check(name = "按 label 统计 label 数量", desc = "例：一个样本中有三个 apple，apple 计数三次。")
        public List<Item> countByLabel;
        @Check(name = "按样本统计 label 数量", desc = "例：一个样本中有三个 apple，apple 计数一次。")
        public List<Item> countBySample;

        public Output() {
        }

        public Output(Map<String, Integer> countByLabel, Map<String, Integer> countBySample) {
            this.countByLabel = mapToList(countByLabel);
            this.countBySample = mapToList(countBySample);
        }

        private List<Item> mapToList(Map<String, Integer> map) {
            return map
                    .entrySet()
                    .stream()
                    .map(x -> new Item(x.getKey(), x.getValue()))
                    .collect(Collectors.toList());
        }

        public static class Item {
            public String label;
            public int count;

            public Item() {
            }

            public Item(String label, int count) {
                this.label = label;
                this.count = count;
            }
        }
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "数据集Id")
        public String dataSetId;
    }
}
