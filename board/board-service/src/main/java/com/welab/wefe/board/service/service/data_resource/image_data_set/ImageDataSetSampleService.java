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
package com.welab.wefe.board.service.service.data_resource.image_data_set;

import com.welab.wefe.board.service.api.data_resource.image_data_set.sample.ImageDataSetSampleQueryApi;
import com.welab.wefe.board.service.api.data_resource.image_data_set.sample.ImageDataSetSampleStatisticsApi;
import com.welab.wefe.board.service.api.data_resource.image_data_set.sample.ImageDataSetSampleUpdateApi;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.board.service.database.repository.ImageDataSetSampleRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_set.ImageDataSetSampleOutputModel;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.MapUtil;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    /**
     * 获取所有已标注的样本
     */
    public List<ImageDataSetSampleMysqlModel> allLabeled(String dataSetId) {
        Specification<ImageDataSetSampleMysqlModel> where = Where
                .create()
                .equal("dataSetId", dataSetId)
                .equal("labeled", true)
                .build(ImageDataSetSampleMysqlModel.class);

        List<ImageDataSetSampleMysqlModel> all = imageDataSetSampleRepository.findAll(where);
        return all;
    }

    public PagingOutput<ImageDataSetSampleOutputModel> query(ImageDataSetSampleQueryApi.Input input) {

        Where where = Where
                .create()
                .equal("dataSetId", input.getDataSetId())
                .equal("labeled", input.getLabeled());

        if (StringUtil.isNotEmpty(input.getLabel())) {
            if (input.labelMatchWithContains) {
                where.contains("labelList", input.getLabel());
            } else {
                // 前后拼接逗号，用于精确匹配单个 label。
                where.contains("labelList", "," + input.getLabel() + ",");
            }
        }


        return imageDataSetSampleRepository.paging(
                where.build(ImageDataSetSampleMysqlModel.class),
                input,
                ImageDataSetSampleOutputModel.class
        );
    }

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

    public void delete(String id) throws StatusCodeWithException {
        ImageDataSetSampleMysqlModel sample = imageDataSetSampleRepository.findById(id).orElse(null);
        if (sample == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("id 对应的样本不存在：" + id);
        }

        imageDataSetSampleRepository.delete(sample);
        imageDataSetService.updateLabelInfo(sample.getDataSetId());

        FileUtil.deleteFileOrDir(sample.getFilePath());
    }

    /**
     * 统计样本分布情况
     */
    public ImageDataSetSampleStatisticsApi.Output statistics(String dataSetId) {
        Map<String, Integer> countByLabel = new TreeMap<>();
        Map<String, Integer> countBySample = new TreeMap<>();

        imageDataSetSampleRepository.getAllLabelList(dataSetId)
                .stream()
                .filter(x -> StringUtil.isNotEmpty(x))
                .forEach(x -> {
                    List<String> labelList = StringUtil.splitWithoutEmptyItem(x, ",");
                    labelList.forEach(label -> MapUtil.increment(countByLabel, label));

                    List<String> distinctLabelList = labelList.stream().distinct().collect(Collectors.toList());
                    distinctLabelList.forEach(label -> MapUtil.increment(countBySample, label));
                });

        return new ImageDataSetSampleStatisticsApi.Output(countByLabel, countBySample);
    }
}
