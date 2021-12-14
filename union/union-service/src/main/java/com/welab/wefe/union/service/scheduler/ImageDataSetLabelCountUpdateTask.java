package com.welab.wefe.union.service.scheduler;

import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSetLabeledCount;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetLabeledCountMongoReop;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.union.service.service.ImageDataSetContractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/25
 */
@Configuration
public class ImageDataSetLabelCountUpdateTask {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ImageDataSetContractService imageDataSetContractService;

    @Autowired
    private ImageDataSetMongoReop imageDataSetMongoReop;

    @Autowired
    private ImageDataSetLabeledCountMongoReop imageDataSetLabeledCountMongoReop;
    @Value("${image.dataset.label.count.update.rate}")
    private String rateString;

    @Scheduled(fixedRateString = "${image.dataset.label.count.update.rate}")
    private void startTask() {
        List<ImageDataSetLabeledCount> list = imageDataSetLabeledCountMongoReop.findAll();
        for (ImageDataSetLabeledCount imageDataSetLabeledCount :
                list) {
            if (imageDataSetLabeledCount.getUpdateTime() - System.currentTimeMillis() >= Long.parseLong(rateString)) {
                boolean isLabelCompleted = false;
                if (imageDataSetLabeledCount.getLabeledCount() >= imageDataSetLabeledCount.getTotalDataCount()) {
                    isLabelCompleted = true;
                }
                try {
                    ImageDataSet imageDataSet = imageDataSetMongoReop.findDataResourceId(imageDataSetLabeledCount.getDataResouceId());
                    imageDataSet.setDataResourceId(imageDataSetLabeledCount.getDataResouceId());
                    imageDataSet.setLabeledCount(String.valueOf(imageDataSetLabeledCount.getLabeledCount()));
                    imageDataSet.setLabelList(imageDataSetLabeledCount.getLabelList());
                    imageDataSet.setLabelCompleted(isLabelCompleted ? "1" : "0");
                    imageDataSetContractService.update(imageDataSet);
                } catch (StatusCodeWithException e) {
                    LOG.error("update ImageDataSetLabeledCount error dataSetId: " + imageDataSetLabeledCount.getDataResouceId(), e);
                }
            }
        }
    }
}