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

import com.welab.wefe.board.service.api.data_resource.image_data_set.ImageDataSetDeleteApi;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.repository.ImageDataSetSampleRepository;
import com.welab.wefe.board.service.database.repository.data_resource.ImageDataSetRepository;
import com.welab.wefe.board.service.dto.entity.data_resource.output.ImageDataSetOutputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.AbstractDataResourceUpdateInputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.ImageDataSetUpdateInputModel;
import com.welab.wefe.board.service.onlinedemo.OnlineDemoBranchStrategy;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.data_resource.DataResourceService;
import com.welab.wefe.board.service.service.data_resource.image_data_set.data_set_parser.AbstractImageDataSetParser;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.TreeSet;

/**
 * @author zane
 * @date 2021/11/8
 */
@Service
public class ImageDataSetService extends DataResourceService {

    @Autowired
    private ImageDataSetRepository imageDataSetRepository;
    @Autowired
    private ImageDataSetSampleRepository imageDataSetSampleRepository;

    /**
     * get data sets info from local or union
     */
    public ImageDataSetOutputModel findDataSetFromLocalOrUnion(String memberId, String dataSetId) throws StatusCodeWithException {

        if (memberId.equals(CacheObjects.getMemberId())) {
            ImageDataSetMysqlModel dataSet = imageDataSetRepository.findById(dataSetId).orElse(null);
            if (dataSet == null) {
                return null;
            }
            return ModelMapper.map(dataSet, ImageDataSetOutputModel.class);
        } else {
            return unionService.getDataResourceDetail(dataSetId, ImageDataSetOutputModel.class);
        }
    }

    public synchronized void updateLabelInfo(String dataSetId) throws StatusCodeWithException {
        ImageDataSetMysqlModel dataSet = findOneById(dataSetId);
        TreeSet<String> labelSet = new TreeSet<>();
        imageDataSetSampleRepository.getAllDistinctLabelList(dataSetId)
                .stream()
                .filter(x -> StringUtil.isNotEmpty(x))
                .forEach(x ->
                        labelSet.addAll(StringUtil.splitWithoutEmptyItem(x, ","))
                );

        dataSet.setLabelList(StringUtil.joinByComma(labelSet));
        dataSet.setTotalDataCount(imageDataSetSampleRepository.getSampleCount(dataSetId));
        dataSet.setLabeledCount(imageDataSetSampleRepository.getLabeledCount(dataSetId));

        dataSet.setLabelCompleted(dataSet.getTotalDataCount().equals(dataSet.getLabeledCount()));

        imageDataSetRepository.save(dataSet);

        unionService.lazyUpdateDataResource(dataSet);

    }

    @Override
    public ImageDataSetMysqlModel findOneById(String dataSetId) {
        return imageDataSetRepository.findById(dataSetId).orElse(null);
    }

    @Override
    protected void beforeUpdate(DataResourceMysqlModel m, AbstractDataResourceUpdateInputModel in) {
        ImageDataSetMysqlModel model = (ImageDataSetMysqlModel) m;
        ImageDataSetUpdateInputModel input = (ImageDataSetUpdateInputModel) in;
    }

    /**
     * delete image data set
     */
    public void delete(ImageDataSetDeleteApi.Input input) throws StatusCodeWithException {
        ImageDataSetMysqlModel model = imageDataSetRepository.findById(input.getId()).orElse(null);
        if (model == null) {
            return;
        }

        OnlineDemoBranchStrategy.hackOnDelete(input, model, "只能删除自己添加的数据集。");

        delete(model);
    }

    public void delete(String dataSetId) throws StatusCodeWithException {
        ImageDataSetMysqlModel model = imageDataSetRepository.findById(dataSetId).orElse(null);
        if (model == null) {
            return;
        }

        delete(model);
    }

    public void delete(ImageDataSetMysqlModel model) throws StatusCodeWithException {
        imageDataSetRepository.deleteById(model.getId());
        imageDataSetSampleRepository.deleteByDataSetId(model.getId());

        FileUtil.deleteFileOrDir(model.getStorageNamespace());
        CacheObjects.refreshDataResourceTags(model.getDataResourceType());

        unionService.deleteDataResource(model);
    }


    public File download(String dataSetId, String jobId) throws StatusCodeWithException {
        ImageDataSetMysqlModel dataSet = findOneById(dataSetId);

        File file = AbstractImageDataSetParser.getDataSetFile(dataSet, jobId);
        if (!file.exists()) {
            StatusCode
                    .PARAMETER_VALUE_INVALID
                    .throwException("该 job 尚未生成数据集文件：" + jobId);
        }
        return file;

    }
}
