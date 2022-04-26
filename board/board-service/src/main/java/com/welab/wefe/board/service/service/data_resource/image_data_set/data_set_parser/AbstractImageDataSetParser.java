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
package com.welab.wefe.board.service.service.data_resource.image_data_set.data_set_parser;


import com.welab.wefe.board.service.database.entity.data_resource.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.Convert;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.file.compression.impl.Zip;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.wefe.enums.DeepLearningJobType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zane
 * @date 2021/11/26
 */
public abstract class AbstractImageDataSetParser extends AbstractService {
    /**
     * 解析文件列表，获取样本信息。
     */
    protected abstract List<ImageDataSetSampleMysqlModel> parseFilesToSamples(ImageDataSetMysqlModel dataSet, Map<String, File> imageFiles, Map<String, File> xmlFiles, Map<String, File> txtFiles) throws Exception;

    /**
     * 将数据集样本输出到数据集文件输出目录，为打包为数据集文件做好准备。
     */
    protected abstract void emitSamplesToDataSetFileDir(ImageDataSetMysqlModel dataSet, final List<ImageDataSetSampleMysqlModel> trainSamples, final List<ImageDataSetSampleMysqlModel> testSamples, Path outputDir) throws Exception;

    public static AbstractImageDataSetParser getParser(DeepLearningJobType type) {
        switch (type) {
            case classify:
                return new ClassifyImageDataSetParser();
            case detection:
                return new DetectionImageDataSetParser();
            default:
                return null;
        }
    }

    public static String getDataSetFileName(String jobId, String version) {
        return jobId + "_" + version + ".zip";
    }

    public static File getDataSetFile(ImageDataSetMysqlModel dataSet, String jobId, String version) {
        return Paths.get(
                dataSet.getStorageNamespace(),
                "output",
                getDataSetFileName(jobId, version)
        ).toFile();
    }

    /**
     * 将数据集样本打包为数据集文件
     * <p>
     * 为减小磁盘开销，在数据集和参数没有变化时，将不会重复生成数据集文件。
     * 以下为影响是否重新生成数据集文件的因素，当这些因素有变化时，需要重新生成数据集：
     * 1. 切割比例
     * 2. 样本数量
     * 3. 样本最后标注时间
     */
    public String parseSamplesToDataSetFile(String jobId, ImageDataSetMysqlModel dataSet, final List<ImageDataSetSampleMysqlModel> samples, int trainTestSplitRatio) throws Exception {
        // 构建数据集文件的版本号
        long samplesLastUpdateTime = samples.stream().mapToLong(x ->
                x.getUpdatedTime() == null
                        ? x.getCreatedTime().getTime()
                        : x.getUpdatedTime().getTime()
        ).max().orElse(0);
        String version = trainTestSplitRatio + "-" + samples.size() + "-" + samplesLastUpdateTime;

        File file = getDataSetFile(dataSet, jobId, version);
        // 如果数据集文件已经存在，不重复生成，节省磁盘。
        if (file.exists()) {
            return version;
        }

        // 根据切割比例计算训练集和测试集样本的数量
        int trainCount = Convert.toInt(trainTestSplitRatio / 100D * samples.size());
        if (trainCount < 1) {
            trainCount = 1;
        }
        int testCount = samples.size() - trainCount;

        // 将全部样本切割为训练集和测试集
        Random rand = new Random();
        List<ImageDataSetSampleMysqlModel> trainList = new ArrayList<>();
        List<ImageDataSetSampleMysqlModel> testList = new ArrayList<>();
        for (ImageDataSetSampleMysqlModel sample : samples) {
            // 该样本是否判定为 train
            boolean isTrainSample = false;
            // train 数量还没凑够
            if (trainList.size() < trainCount) {
                // test 已经凑够了，或者命运选择这条样本为 train
                if (testList.size() >= testCount || rand.nextBoolean()) {
                    isTrainSample = true;
                }
            }

            if (isTrainSample) {
                trainList.add(sample);
            } else {
                testList.add(sample);
            }
        }

        // 生成打包路径
        Path outputDir = Paths.get(
                dataSet.getStorageNamespace(),
                "output",
                jobId
        );

        // 删除已存在的文件
        FileUtil.deleteFileOrDir(outputDir.toString());
        // 将样本内容输出到打包目录
        emitSamplesToDataSetFileDir(dataSet, trainList, testList, outputDir);
        new Zip().compression(
                outputDir.toString(),
                getDataSetFile(dataSet, jobId, version).getAbsolutePath()
        );
        return version;
    }

    public List<ImageDataSetSampleMysqlModel> parseFilesToSamples(ImageDataSetMysqlModel dataSet, final Set<File> allFiles) throws Exception {
        // 过滤掉隐藏文件和操作系统临时目录中的文件
        List<File> files = allFiles.stream()
                .filter(x -> !x.getAbsolutePath().contains("/__MACOSX/"))
                .filter(x -> !x.isHidden())
                .collect(Collectors.toList());

        Set<String> fileNameSet = new HashSet<>();
        for (File file : files) {
            String fileName = file.getName();
            if (fileNameSet.contains(fileName)) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("检测到多个文件名为：" + fileName + "，请删除或修改文件名后重试。");
            }
            fileNameSet.add(fileName);
        }

        Map<String, File> imageFiles = files
                .stream()
                .filter(FileUtil::isImage)
                .collect(Collectors.toMap(
                        FileUtil::getFileNameWithoutSuffix,
                        x -> x
                ));

        Map<String, File> xmlFiles = files
                .stream()
                .filter(x -> "xml".equalsIgnoreCase(FileUtil.getFileSuffix(x)))
                .collect(Collectors.toMap(
                        FileUtil::getFileNameWithoutSuffix,
                        x -> x
                ));

        Map<String, File> txtFiles = files
                .stream()
                .filter(x -> "txt".equalsIgnoreCase(FileUtil.getFileSuffix(x)))
                .collect(Collectors.toMap(
                        FileUtil::getFileNameWithoutSuffix,
                        x -> x
                ));

        List<ImageDataSetSampleMysqlModel> samples = parseFilesToSamples(dataSet, imageFiles, xmlFiles, txtFiles);

        return samples;
    }

    protected ImageDataSetSampleMysqlModel createSampleModel(ImageDataSetMysqlModel dataSet, File imageFile) throws StatusCodeWithException, IOException {
        ImageDataSetSampleMysqlModel sample = new ImageDataSetSampleMysqlModel();
        sample.setDataSetId(dataSet.getId());
        sample.setFileName(imageFile.getName());
        sample.setFilePath(
                Paths.get(dataSet.getStorageNamespace(), imageFile.getName()).toString()
        );
        sample.setFileSize(imageFile.length());
        sample.setCreatedBy(CurrentAccount.id());

        // move image to dest dir
        File destFile = new File(sample.getFilePath());
        if (destFile.exists()) {
            destFile.delete();
        }
        FileUtils.copyFile(imageFile, destFile);

        return sample;
    }
}
