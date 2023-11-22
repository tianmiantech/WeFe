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
package com.welab.wefe.board.service.service.data_resource.add;

import com.welab.wefe.board.service.base.file_system.WeFeFileSystem;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceUploadTaskMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.board.service.database.repository.ImageDataSetSampleRepository;
import com.welab.wefe.board.service.database.repository.data_resource.ImageDataSetRepository;
import com.welab.wefe.board.service.dto.vo.data_resource.AbstractDataResourceUpdateInputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.ImageDataSetAddInputModel;
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

        LOG.info("{} 开始解析图片数据集文件...", m.getId());

        ImageDataSetAddInputModel input = (ImageDataSetAddInputModel) in;
        ImageDataSetMysqlModel model = (ImageDataSetMysqlModel) m;

        File inputFile = WeFeFileSystem.getFilePath(DataResourceType.ImageDataSet, input.getFilename()).toFile();
        LOG.info("{} 获取到图片数据集文件：{}", m.getId(), inputFile.getAbsolutePath());

        DecompressionResult fileDecompressionResult = null;
        List<ImageDataSetSampleMysqlModel> sampleList = null;
        try {
            dataResourceUploadTaskService.updateMessageBeforeStart(model.getId(), "解压中...");
            fileDecompressionResult = SuperDecompressor.decompression(inputFile, true);
            dataResourceUploadTaskService.updateMessageBeforeStart(model.getId(), "解压完成，正在解析样本...");
            LOG.info("{} 完成解压，包含文件 {} 个", m.getId(), fileDecompressionResult.files.size());

            sampleList = AbstractImageDataSetParser
                    .getParser(input.forJobType)
                    .parseFilesToSamples(model, fileDecompressionResult.files);
            LOG.info("{} 完成样本解析，包含样本 {} 个", m.getId(), sampleList.size());
            dataResourceUploadTaskService.updateProgress(model.getId(), sampleList.size(), 1, 0, "已完成样本解析");

            setImageDataSetModel(input, model, sampleList);
            dataResourceUploadTaskService.updateProgress(model.getId(), sampleList.size(), 2, 0);
        } catch (Exception e) {
            super.log(e);
            StatusCode.FILE_IO_ERROR.throwException(e);
        }

        // save models to database
        imageDataSetRepository.save(model);
        LOG.info("{} 数据集信息已入库，开始保存 {} 个样本信息。", m.getId(), sampleList.size());

        AtomicInteger count = new AtomicInteger();
        int totalCount = sampleList.size();

        ListUtil.parallelEach(
                sampleList,
                sample -> {
                    try {
                        imageDataSetSampleRepository.save(sample);
                        count.incrementAndGet();
                        if (count.get() % 50 == 0) {
                            dataResourceUploadTaskService.updateProgress(model.getId(), totalCount, count.get(), 0, "正在保存样本信息...");
                            LOG.info("{} 样本信息保存中，当前进度 {}/{}", m.getId(), count.get(), totalCount);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
        );

        LOG.info("{} 样本保存完毕 {}/{}", m.getId(), count.get(), totalCount);

        // delete source images
        FileUtil.deleteFileOrDir(inputFile);
        LOG.info("{} 原始数据集文件已删除：{}", m.getId(), inputFile.getAbsolutePath());

        fileDecompressionResult.deleteAllDirAndFiles();
        LOG.info("{} 原始数据集解压后的文件夹已删除：{}", m.getId(), fileDecompressionResult.baseDir);
    }

    private void setImageDataSetModel(ImageDataSetAddInputModel input, ImageDataSetMysqlModel dataSet, List<ImageDataSetSampleMysqlModel> sampleList) {
        dataSet.setForJobType(input.forJobType);

        // distinct labels
        TreeSet<String> labelSet = new TreeSet<>();
        sampleList
                .stream()
                .filter(x -> x.isLabeled())
                .forEach(x ->
                        labelSet.addAll(x.getLabelSet())
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
