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
package com.welab.wefe.board.service.service.data_resource.image_data_set.data_set_parser;

import com.thoughtworks.xstream.io.StreamException;
import com.welab.wefe.board.service.database.entity.data_resource.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.board.service.dto.vo.data_set.image_data_set.Annotation;
import com.welab.wefe.board.service.dto.vo.data_set.image_data_set.LabelInfo;
import com.welab.wefe.board.service.dto.vo.data_set.image_data_set.Size;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.*;
import org.apache.commons.collections4.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zane
 * @date 2021/11/26
 */
public class DetectionImageDataSetParser extends AbstractImageDataSetParser {
    /**
     * e.g:
     * apple
     * banana
     * orange
     */
    private static final String LABEL_LIST_FILE_NAME = "label_list.txt";

    /********** ↓ 导出数据集 ↓ **********/

    /**
     * 目录结构：
     * annotations/
     * **** name1.xml
     * **** name2.xml
     * **** name3.xml
     * <p>
     * images/
     * **** name1.jpg
     * **** name2.jpg
     * **** name3.jpg
     * <p>
     * label_list.txt
     */
    @Override
    protected void emitSamplesToDataSetFileDir(ImageDataSetMysqlModel dataSet, List<ImageDataSetSampleMysqlModel> trainSamples, List<ImageDataSetSampleMysqlModel> testSamples, Path outputDir) throws Exception {
        for (ImageDataSetSampleMysqlModel sample : trainSamples) {
            emitImageFile(outputDir, sample);
            emitAnnotationFile(true, outputDir, sample);
        }
        for (ImageDataSetSampleMysqlModel sample : testSamples) {
            emitImageFile(outputDir, sample);
            emitAnnotationFile(false, outputDir, sample);
        }

        emitLabelListFile(dataSet, outputDir);

    }

    /**
     * 输出 label_list.txt
     */
    private void emitLabelListFile(ImageDataSetMysqlModel dataSet, Path outputDir) throws IOException {
        String labelListStr = StringUtil.join(
                StringUtil.splitWithoutEmptyItem(dataSet.getLabelList(), ","),
                System.lineSeparator()
        );
        Path labelListPath = Paths.get(
                outputDir.toString(),
                LABEL_LIST_FILE_NAME
        );
        FileUtil.writeTextToFile(
                labelListStr,
                labelListPath,
                false
        );
    }

    /**
     * 拷贝图片文件到输出目录
     */
    private void emitImageFile(Path outputDir, ImageDataSetSampleMysqlModel sample) throws IOException {
        FileUtil.copy(
                Paths.get(sample.getFilePath()),
                Paths.get(outputDir.toString(), "images", sample.getFileName())
        );
    }

    /**
     * 输出 xml 文件
     */
    private void emitAnnotationFile(boolean isTrain, Path outputDir, ImageDataSetSampleMysqlModel sample) throws IOException {
        Annotation annotation = XmlUtil.toModel(sample.getXmlAnnotation(), Annotation.class);
        annotation.folder = isTrain ? "train" : "test";
        annotation.objectList = sample.getLabelInfo()
                .toJavaObject(LabelInfo.class)
                .objects
                .stream()
                .map(LabelInfo.Item::toLabelObject)
                .collect(Collectors.toList());
        Path annotationPath = Paths.get(
                outputDir.toString(),
                "annotations",
                FileUtil.getFileNameWithoutSuffix(sample.getFileName()) + ".xml"
        );
        FileUtil.writeTextToFile(
                XmlUtil.toXml(annotation),
                annotationPath,
                false
        );
    }

    /********** ↓ 导入数据集 ↓ **********/

    @Override
    protected List<ImageDataSetSampleMysqlModel> parseFilesToSamples(ImageDataSetMysqlModel dataSet, Map<String, File> imageFiles, Map<String, File> xmlFiles, Map<String, File> txtFiles) throws Exception {

        List<ImageDataSetSampleMysqlModel> result = new ArrayList<>();

        Exception error = ListUtil.parallelEach(
                imageFiles.keySet(),
                key -> {
                    File imageFile = imageFiles.get(key);
                    File xmlFile = xmlFiles.get(key);

                    Annotation annotation = buildAnnotation(imageFile, xmlFile, dataSet);
                    ImageDataSetSampleMysqlModel sample = createSample(dataSet, imageFile, annotation);
                    result.add(sample);
                }
        );

        if (error != null) {
            throw error;
        }

        return result;
    }


    private ImageDataSetSampleMysqlModel createSample(ImageDataSetMysqlModel dataSet, File imageFile, Annotation annotation) throws StatusCodeWithException, IOException {
        ImageDataSetSampleMysqlModel sample = super.createSampleModel(dataSet, imageFile);
        sample.setLabelList(StringUtil.join(annotation.getLabelList(), ","));
        sample.setLabeled(CollectionUtils.isNotEmpty(annotation.getLabelList()));
        sample.setXmlAnnotation(XmlUtil.toXml(annotation));
        sample.setLabelInfo(JObject.create(annotation.toLabelInfo()));
        return sample;
    }


    /**
     * XmlUtil Doc: https://www.bookstack.cn/read/hutool/e41e0b0a699544fb.md
     */
    private Annotation buildAnnotation(File imageFile, File xmlFile, ImageDataSetMysqlModel dataSet) throws StatusCodeWithException {
        Annotation annotation = null;
        if (xmlFile != null) {
            try {
                annotation = XmlUtil.toModel(xmlFile, Annotation.class);
            } catch (StreamException e) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("xml 文件反序列化失败：" + xmlFile.getAbsolutePath());
            } catch (IOException e) {
                StatusCode.FILE_IO_ERROR.throwException(e);
            }
        } else {
            annotation = new Annotation();
        }

        BufferedImage image = null;
        try {
            image = ImageIO.read(new FileInputStream(imageFile));
        } catch (IOException e) {
            StatusCode.FILE_IO_ERROR.throwException(e);
        }
        annotation.size = new Size();
        annotation.size.depth = image.getRaster().getNumDataElements();
        annotation.size.width = image.getWidth();
        annotation.size.height = image.getHeight();

        // folder的取值为：train、test
        annotation.folder = "";

        annotation.filename = imageFile.getName();
        annotation.path = imageFile.getAbsolutePath();

        return annotation;
    }


}
