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

package com.welab.wefe.union.service.service;

import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.union.service.api.dataresource.dataset.image.PutApi;
import com.welab.wefe.union.service.service.contract.ImageDataSetContractService;
import com.welab.wefe.union.service.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageDataSetService extends AbstractDataResource{
    @Autowired
    protected ImageDataSetContractService imageDataSetContractService;
    @Autowired
    protected ImageDataSetMongoReop imageDataSetMongoReop;

    public void add(PutApi.Input input) throws StatusCodeWithException {
        ImageDataSet imageDataSet = imageDataSetMongoReop.findByDataResourceId(input.getDataResourceId());
        DataResource dataResource = dataResourceMongoReop.find(input.getDataResourceId(), input.curMemberId);
        if (dataResource == null) {
            if (imageDataSet == null) {
                imageDataSetContractService.add(MapperUtil.transferPutInputToImageDataSet(input));
                dataResourceContractService.add(MapperUtil.transferPutInputToDataResource(input));
            } else {
                dataResourceContractService.add(MapperUtil.transferPutInputToDataResource(input));
            }
        } else {
            imageDataSet.setLabelCompleted(input.isLabelCompleted() ? "1" : "0");
            imageDataSet.setLabelList(input.getLabelList());
            imageDataSet.setLabeledCount(String.valueOf(input.getLabeledCount()));
            imageDataSet.setForJobType(input.getForJobType());
            imageDataSet.setFileSize(input.getFilesSize());
            imageDataSetContractService.update(imageDataSet);

            updateDataResource(dataResource, input);
        }
    }


}
