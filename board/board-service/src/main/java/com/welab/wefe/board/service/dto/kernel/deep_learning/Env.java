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
package com.welab.wefe.board.service.dto.kernel.deep_learning;

import com.alibaba.fastjson.JSON;
import com.welab.wefe.board.service.component.deep_learning.ImageDataIOComponent;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.Convert;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 * @author zane
 * @date 2021/11/22
 */
public class Env {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    /**
     * 本方 worker 个数
     * <p>
     * 计算逻辑：
     * 以所有样本集中最小样本数为基数
     * 各成员的 worker 数为自己样本数除以基数，四舍五入取整。
     * 为避免极端情况，最大不超过5。
     */
    public int localWorkerNum;
    /**
     * worker 总个数，即各方 worker 数之和。
     */
    public int workerNum;
    /**
     * 本方 worker 索引，多方之间不能重复。
     * e.g: [0,1]
     */
    public int[] localTrainerIndexs;
    /**
     * 设备 cpu/gpu
     */
    public String device = "cpu";
    /**
     * 是否使用 visualdl 可视化
     */
    public boolean useVdl = true;
    /**
     * 是否基于上次执行一半的任务继续执行
     */
    public boolean resume = false;

    public Env() {
    }

    public Env(ImageDataIOComponent.Params imageDataIoParam) throws StatusCodeWithException {
        imageDataIoParam.fillDataSetDetail();
        if (CollectionUtils.isEmpty(imageDataIoParam.dataSetList)) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("未选择数据集");
        }
        LOG.info("dataSetList:" + JSON.toJSONString(imageDataIoParam.dataSetList));
        Object[] labeledCountArray = imageDataIoParam.dataSetList
                .stream()
                .map(x -> x.dataSet.getLabeledCount())
                .toArray();
        LOG.info("labeledCountArray:" + JSON.toJSONString(labeledCountArray));

        // 以所有样本集中最小样本数为基数，用于计算各成员需要的 worker 数。
        long min = imageDataIoParam.dataSetList
                .stream()
                .mapToLong(x -> x.dataSet.getLabeledCount())
                .min()
                .orElse(0);


        /**
         * 1. 前端应该不允许使用标注量为0的样本
         * 2. ImageDataIO 中阻止了标注量为 0 的样本
         *
         * 所以正常情况下不应会让这里出现0
         */
        if (min == 0) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("有成员的数据集已标注样本量为 0");
        }

        // 对成员按 member_id 排序，使各成员生成的 worker 顺序一致。
        imageDataIoParam.dataSetList.sort(Comparator.comparing(x -> x.getMemberId()));

        // 计算各方的 worker 数
        LinkedHashMap<String, Integer> workerCountMap = new LinkedHashMap<>();
        for (ImageDataIOComponent.DataSetItem dataSetItem : imageDataIoParam.dataSetList) {
            int workerCount = Convert.toInt(
                    Math.round(
                            new Double(dataSetItem.dataSet.getLabeledCount()) / min
                    )
            );

            // 限制上限
            if (workerCount > 10) {
                workerCount = 10;
            }

            // is me
            if (CacheObjects.getMemberId().equals(dataSetItem.getMemberId())) {
                this.localWorkerNum = workerCount;
                int startIndex = workerCountMap.values().stream().mapToInt(x -> x).sum();
                int endIndex = startIndex + this.localWorkerNum - 1;
                if (startIndex == endIndex) {
                    this.localTrainerIndexs = new int[]{startIndex};
                } else {
                    this.localTrainerIndexs = new int[]{startIndex, endIndex};
                }

            }

            workerCountMap.put(dataSetItem.getMemberId(), workerCount);
        }

        this.workerNum = workerCountMap.values().stream().mapToInt(x -> x).sum();
    }

}
