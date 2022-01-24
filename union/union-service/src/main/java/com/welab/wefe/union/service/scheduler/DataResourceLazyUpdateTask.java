package com.welab.wefe.union.service.scheduler;

import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.entity.union.DataResourceLazyUpdateModel;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.repo.DataResourceLazyUpdateModelMongoReop;
import com.welab.wefe.common.data.mongodb.repo.DataResourceMongoReop;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.union.service.service.DataResourceContractService;
import com.welab.wefe.union.service.service.ImageDataSetContractService;
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


    @Scheduled(fixedDelayString = "${data.resource.lazy.update.update.fixed.delay.string}")
    private void startTask() {
        List<DataResourceLazyUpdateModel> list = dataResourceLazyUpdateModelMongoReop.findAll();
        for (DataResourceLazyUpdateModel dataResourceLazyUpdateModel :
                list) {
            try {
                DataResource dataResource = dataResourceMongoReop.findByDataResourceId(dataResourceLazyUpdateModel.getDataResourceId());
                dataResource.setTotalDataCount(String.valueOf(dataResourceLazyUpdateModel.getTotalDataCount()));
                dataResourceContractService.update(dataResource);

                ImageDataSet imageDataSet = imageDataSetMongoReop.findByDataResourceId(dataResourceLazyUpdateModel.getDataResourceId());
                imageDataSet.setDataResourceId(dataResourceLazyUpdateModel.getDataResourceId());
                imageDataSet.setLabeledCount(String.valueOf(dataResourceLazyUpdateModel.getLabeledCount()));
                imageDataSet.setLabelList(dataResourceLazyUpdateModel.getLabelList());
                imageDataSet.setLabelCompleted(String.valueOf(dataResourceLazyUpdateModel.isLabelCompleted() ? 1 : 0));
                imageDataSetContractService.update(imageDataSet);
            } catch (StatusCodeWithException e) {
                LOG.error("DataResourceLazyUpdate error dataResourceId: " + dataResourceLazyUpdateModel.getDataResourceId(), e);
            }
        }
    }
}