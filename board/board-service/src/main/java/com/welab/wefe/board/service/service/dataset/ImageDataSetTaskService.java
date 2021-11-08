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
package com.welab.wefe.board.service.service.dataset;

import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.board.service.dto.vo.data_set.ImageDataSetAddInputModel;
import com.welab.wefe.board.service.dto.vo.data_set.ImageDataSetAddOutputModel;
import com.welab.wefe.board.service.dto.vo.data_set.image_data_set.Annotation;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.ZipUtil;
import com.welab.wefe.common.web.CurrentAccount;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zane
 * @date 2021/11/8
 */
@Service
public class ImageDataSetTaskService extends AbstractService {

    public ImageDataSetAddOutputModel add(ImageDataSetAddInputModel input) throws StatusCodeWithException {


        File zipFile = new File(config.getFileUploadDir(), input.getFilename());

        ImageDataSetMysqlModel dataSet = new ImageDataSetMysqlModel();
        dataSet.setFilesSize(zipFile.length());

        ZipUtil.UnzipFileResult unzipFileResult = null;
        try {
            unzipFileResult = ZipUtil.unzipFile(zipFile);
            List<ImageDataSetSampleMysqlModel> sampleList = parseZipFile(dataSet, unzipFileResult);


        } catch (IOException e) {
            StatusCode.FILE_IO_ERROR.throwException(e);
        }


        return null;
    }

    /**
     * 解析 zip 文件，获取样本信息。
     * <p>
     * XmlUtil Doc: https://www.bookstack.cn/read/hutool/e41e0b0a699544fb.md
     */
    private List<ImageDataSetSampleMysqlModel> parseZipFile(ImageDataSetMysqlModel dataSet, ZipUtil.UnzipFileResult unzipFileResult) throws IOException {

        Map<String, File> imageFiles = unzipFileResult.files
                .stream()
                .filter(x -> FileUtil.isImage(x))
                .collect(Collectors.toMap(
                        x -> FileUtil.getFileNameWithoutSuffix(x),
                        x -> x
                ));

        Map<String, File> xmlFiles = unzipFileResult.files
                .stream()
                .filter(x -> FileUtil.isImage(x))
                .collect(Collectors.toMap(
                        x -> FileUtil.getFileNameWithoutSuffix(x),
                        x -> x
                ));

        List<ImageDataSetSampleMysqlModel> result = new ArrayList<>();
        for (String key : imageFiles.keySet()) {
            File imageFile = imageFiles.get(key);
            ImageDataSetSampleMysqlModel sample = new ImageDataSetSampleMysqlModel();
            sample.setDataSetId(dataSet.getId());
            sample.setFileName(imageFile.getName());
            sample.setFilePath(imageFile.getAbsolutePath());
            sample.setFileSize(imageFile.length());
            sample.setCreatedBy(CurrentAccount.id());


            File xmlFile = xmlFiles.get(key);
            if (xmlFile != null) {
                Annotation annotation = XmlUtil.readObjectFromXml(xmlFile);
                sample.setLabel();
                sample.setLabeled(true);
                sample.setXmlAnnotation("");
                sample.setLabelInfo(new JSONObject());
            }

            result.add();
        }


    }


}
