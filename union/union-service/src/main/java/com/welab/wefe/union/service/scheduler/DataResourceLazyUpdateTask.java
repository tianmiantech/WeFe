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

package com.welab.wefe.union.service.scheduler;

import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.entity.union.DataResourceLazyUpdateModel;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.repo.DataResourceLazyUpdateModelMongoReop;
import com.welab.wefe.common.data.mongodb.repo.DataResourceMongoReop;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetMongoReop;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.union.service.service.DataResourceContractService;
import com.welab.wefe.union.service.service.ImageDataSetContractService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/25
 */
@Configuration
public class DataResourceLazyUpdateTask {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ImageDataSetContractService imageDataSetContractService;
    @Autowired
    private DataResourceContractService dataResourceContractService;
    @Autowired
    private DataResourceMongoReop dataResourceMongoReop;

    @Autowired
    private ImageDataSetMongoReop imageDataSetMongoReop;

    @Autowired
    private DataResourceLazyUpdateModelMongoReop dataResourceLazyUpdateModelMongoReop;


    @Scheduled(initialDelay = 10000, fixedDelayString = "${data.resource.lazy.update.fixed.delay:30000}")
    private void startTask() {
        LOG.info("DataResourceLazyUpdate begin");
        List<DataResourceLazyUpdateModel> list = null;
        try {
            list = dataResourceLazyUpdateModelMongoReop.findAll();
        } catch (Exception e) {
            LOG.error("DataResourceLazyUpdate error:", e);
            return;
        }
        if (CollectionUtils.isEmpty(list)) {
            LOG.info("DataResourceLazyUpdate end");
        }

        for (DataResourceLazyUpdateModel dataResourceLazyUpdateModel : list) {
            try {
                LOG.info("DataResourceLazyUpdate start data resource id: {}", dataResourceLazyUpdateModel.getDataResourceId());
                DataResource dataResource = dataResourceMongoReop.findByDataResourceId(dataResourceLazyUpdateModel.getDataResourceId());
                if (null == dataResource) {
                    LOG.info("DataResourceLazyUpdate, Not exist data resource id: {} info", dataResourceLazyUpdateModel.getDataResourceId());
                    continue;
                }
                dataResource.setTotalDataCount(String.valueOf(dataResourceLazyUpdateModel.getTotalDataCount()));
                dataResource.setUsageCountInJob(String.valueOf(dataResourceLazyUpdateModel.getUsageCountInJob()));
                dataResource.setUsageCountInFlow(String.valueOf(dataResourceLazyUpdateModel.getUsageCountInFlow()));
                dataResource.setUsageCountInProject(String.valueOf(dataResourceLazyUpdateModel.getUsageCountInProject()));
                dataResource.setUsageCountInMember(String.valueOf(dataResourceLazyUpdateModel.getUsageCountInMember()));
                dataResourceContractService.update(dataResource);
                if (!DataResourceType.ImageDataSet.equals(dataResourceLazyUpdateModel.getDataResourceType())) {
                    continue;
                }
                ImageDataSet imageDataSet = imageDataSetMongoReop.findByDataResourceId(dataResourceLazyUpdateModel.getDataResourceId());
                if (null == imageDataSet) {
                    LOG.info("DataResourceLazyUpdate, Not exist data resource id: {} image info", dataResourceLazyUpdateModel.getDataResourceId());
                    continue;
                }
                imageDataSet.setLabeledCount(String.valueOf(dataResourceLazyUpdateModel.getLabeledCount()));
                imageDataSet.setLabelList(dataResourceLazyUpdateModel.getLabelList());
                imageDataSet.setLabelCompleted(String.valueOf(dataResourceLazyUpdateModel.isLabelCompleted() ? 1 : 0));
                imageDataSetContractService.update(imageDataSet);

                LOG.info("DataResourceLazyUpdate end data resource id:{}", dataResourceLazyUpdateModel.getDataResourceId());
            } catch (Exception e) {
                LOG.error("DataResourceLazyUpdate error dataResourceId: " + dataResourceLazyUpdateModel.getDataResourceId(), e);
            }
        }
        LOG.info("DataResourceLazyUpdate end");
    }
}