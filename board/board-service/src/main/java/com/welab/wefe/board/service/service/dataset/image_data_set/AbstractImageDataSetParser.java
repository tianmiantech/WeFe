/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.board.service.service.dataset.image_data_set;

import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.web.CurrentAccount;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public List<ImageDataSetSampleMysqlModel> parseFilesToSamples(ImageDataSetMysqlModel dataSet, final Set<File> allFiles) throws Exception {
        // 过滤掉操作系统临时目录中的文件
        List<File> files = allFiles.stream()
                .filter(x -> !x.getAbsolutePath().contains("/__MACOSX/"))
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

        // move image to dest dir
        for (ImageDataSetSampleMysqlModel sample : samples) {
            File destFile = new File(sample.getFilePath());
            if (destFile.exists()) {
                destFile.delete();
            }

            FileUtils.copyFile(new File(sample.getFilePath()), destFile);
        }

        return samples;
    }

    protected ImageDataSetSampleMysqlModel createSampleModel(ImageDataSetMysqlModel dataSet, File imageFile) throws StatusCodeWithException {
        ImageDataSetSampleMysqlModel sample = new ImageDataSetSampleMysqlModel();
        sample.setDataSetId(dataSet.getId());
        sample.setFileName(imageFile.getName());
        sample.setFilePath(
                Paths.get(dataSet.getNamespace(), imageFile.getName()).toString()
        );
        sample.setFileSize(imageFile.length());
        sample.setCreatedBy(CurrentAccount.id());
        return sample;
    }
}
