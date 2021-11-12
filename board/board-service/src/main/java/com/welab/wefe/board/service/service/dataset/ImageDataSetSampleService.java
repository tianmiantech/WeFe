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
package com.welab.wefe.board.service.service.dataset;

import com.welab.wefe.board.service.api.dataset.image_data_set.sample.ImageDataSetSampleUpdateApi;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.board.service.database.repository.ImageDataSetSampleRepository;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane
 * @date 2021/11/10
 */
@Service
public class ImageDataSetSampleService extends AbstractService {
    @Autowired
    private ImageDataSetSampleRepository imageDataSetSampleRepository;
    @Autowired
    private ImageDataSetService imageDataSetService;

    public void update(ImageDataSetSampleUpdateApi.Input input) throws StatusCodeWithException {
        ImageDataSetSampleMysqlModel sample = imageDataSetSampleRepository.findById(input.id).orElse(null);
        if (sample == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("id 对应的样本不存在：" + input.id);
        }
        sample.setLabeled(input.labelInfo.isLabeled());
        sample.setLabelInfo(JObject.create(input.labelInfo));
        sample.setLabelList(StringUtil.joinByComma(input.labelInfo.labelList()));
        sample.setUpdatedBy(input);

        imageDataSetSampleRepository.save(sample);

        imageDataSetService.updateLabelInfo(sample.getDataSetId());
    }
}
