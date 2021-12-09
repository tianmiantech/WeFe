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

import com.welab.wefe.board.service.database.entity.data_resource.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.dto.base.PagingInput;
import com.welab.wefe.board.service.service.data_resource.image_data_set.ImageDataSetSampleService;
import com.welab.wefe.board.service.service.data_resource.image_data_set.ImageDataSetService;
import com.welab.wefe.board.service.service.data_resource.image_data_set.data_set_parser.AbstractImageDataSetParser;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * @author Zane
 */
@Api(path = "data_resource/query", name = "query all kinds of data resource", login = false)
public class DataResourceQueryApi extends AbstractApi<DataResourceQueryApi.Input, String> {

    @Autowired
    private ImageDataSetService imageDataSetService;
    @Autowired
    private ImageDataSetSampleService imageDataSetSampleService;

    @Override
    protected ApiResult<String> handle(Input input) throws StatusCodeWithException {
        ImageDataSetMysqlModel dataSet = imageDataSetService.findOneById(input.dataSetId);
        // 生成数据集文件
        File file = null;
        try {
            file = AbstractImageDataSetParser
                    .getParser(dataSet.getForJobType())
                    .parseSamplesToDataSetFile(
                            "a_zane_test_job",
                            dataSet,
                            imageDataSetSampleService.allLabeled(input.dataSetId),
                            50
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
//        return success(imageDataSetService.query(input));
        return success(file.getAbsolutePath());
    }

    public static class Input extends PagingInput {
        public String dataSetId;
    }
}
