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
package com.welab.wefe.board.service.service.data_resource.add;

import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceUploadTaskMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.board.service.database.repository.ImageDataSetSampleRepository;
import com.welab.wefe.board.service.database.repository.data_resource.ImageDataSetRepository;
import com.welab.wefe.board.service.dto.vo.data_resource.AbstractDataResourceUpdateInputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.ImageDataSetAddInputModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.data_resource.image_data_set.data_set_parser.AbstractImageDataSetParser;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.file.decompression.SuperDecompressor;
import com.welab.wefe.common.file.decompression.dto.DecompressionResult;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.ListUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zane
 * @date 2021/12/2
 */
@Service
public class ImageDataSetAddService extends AbstractDataResourceAddService {

    @Autowired
    private ImageDataSetRepository imageDataSetRepository;
    @Autowired
    private ImageDataSetSampleRepository imageDataSetSampleRepository;

    @Override
    protected void doAdd(AbstractDataResourceUpdateInputModel in, DataResourceUploadTaskMysqlModel task, DataResourceMysqlModel m) throws StatusCodeWithException {

        ImageDataSetAddInputModel input = (ImageDataSetAddInputModel) in;
        ImageDataSetMysqlModel model = (ImageDataSetMysqlModel) m;

        File inputFile = new File(config.getFileUploadDir(), input.getFilename());

        DecompressionResult fileDecompressionResult = null;
        List<ImageDataSetSampleMysqlModel> sampleList = null;
        try {
            fileDecompressionResult = SuperDecompressor.decompression(inputFile, true);
            dataResourceUploadTaskService.updateProgress(model.getId(), fileDecompressionResult.files.size(), 1, 0);

            sampleList = AbstractImageDataSetParser
                    .getParser(input.forJobType)
                    .parseFilesToSamples(model, fileDecompressionResult.files);

            setImageDataSetModel(input, model, sampleList);
            dataResourceUploadTaskService.updateProgress(model.getId(), sampleList.size(), 2, 0);
        } catch (Exception e) {
            super.log(e);
            StatusCode.FILE_IO_ERROR.throwException(e);
        }

        // save models to database
        imageDataSetRepository.save(model);

        AtomicInteger count = new AtomicInteger();
        int totalCount = sampleList.size();
        sampleList
                .parallelStream()
                .forEach(sample -> {
                    imageDataSetSampleRepository.save(sample);
                    count.incrementAndGet();
                    if (count.get() % 20 == 0) {
                        dataResourceUploadTaskService.updateProgress(model.getId(), totalCount, count.get() + 1, 0);
                    }
                });


        // delete source images
        FileUtil.deleteFileOrDir(inputFile);
        fileDecompressionResult.deleteAllDirAndFiles();

        // Refresh the data set tag list
        CacheObjects.refreshDataResourceTags(model.getDataResourceType());
    }

    private void setImageDataSetModel(ImageDataSetAddInputModel input, ImageDataSetMysqlModel dataSet, List<ImageDataSetSampleMysqlModel> sampleList) {
        dataSet.setForJobType(input.forJobType);

        // distinct labels
        TreeSet<String> labelSet = new TreeSet<>();
        sampleList
                .stream()
                .filter(x -> x.isLabeled())
                .forEach(x ->
                        labelSet.addAll(Arrays.asList(x.getLabelList().split(",")))
                );
        dataSet.setLabelList(
                StringUtil.joinByComma(labelSet)
        );

        dataSet.setTotalDataCount(sampleList.size());
        dataSet.setLabeledCount(
                sampleList.stream().filter(x -> x.isLabeled()).count()
        );

        dataSet.setLabelCompleted(
                sampleList.stream().allMatch(x -> x.isLabeled())
        );
        dataSet.setFilesSize(
                ListUtil.sumLong(sampleList, x -> x.getFileSize())
        );

    }

    @Override
    protected Class<? extends DataResourceMysqlModel> getMysqlModelClass() {
        return ImageDataSetMysqlModel.class;
    }

    @Override
    protected DataResourceType getDataResourceType() {
        return DataResourceType.ImageDataSet;
    }
}
