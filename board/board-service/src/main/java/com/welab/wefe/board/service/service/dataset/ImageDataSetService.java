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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.dataset.image_data_set.ImageDataSetDeleteApi;
import com.welab.wefe.board.service.api.dataset.image_data_set.ImageDataSetQueryApi;
import com.welab.wefe.board.service.database.entity.data_set.AbstractDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.board.service.database.repository.ImageDataSetRepository;
import com.welab.wefe.board.service.database.repository.ImageDataSetSampleRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_set.ImageDataSetOutputModel;
import com.welab.wefe.board.service.dto.vo.data_set.AbstractDataSetUpdateInputModel;
import com.welab.wefe.board.service.dto.vo.data_set.ImageDataSetAddInputModel;
import com.welab.wefe.board.service.dto.vo.data_set.ImageDataSetAddOutputModel;
import com.welab.wefe.board.service.dto.vo.data_set.ImageDataSetUpdateInputModel;
import com.welab.wefe.board.service.dto.vo.data_set.image_data_set.Annotation;
import com.welab.wefe.board.service.dto.vo.data_set.image_data_set.Size;
import com.welab.wefe.board.service.onlinedemo.OnlineDemoBranchStrategy;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.Convert;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.DataSetStorageType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.*;
import com.welab.wefe.common.web.CurrentAccount;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zane
 * @date 2021/11/8
 */
@Service
public class ImageDataSetService extends AbstractDataSetService {

    private static final String IMAGE_DATA_SET_DIR = "image_data_set";
    @Autowired
    private ImageDataSetRepository imageDataSetRepository;
    @Autowired
    private ImageDataSetSampleRepository imageDataSetSampleRepository;

    @Override
    public AbstractDataSetMysqlModel findOneById(String dataSetId) {
        return imageDataSetRepository.findById(dataSetId).orElse(null);
    }

    @Override
    protected void beforeUpdate(AbstractDataSetMysqlModel model, AbstractDataSetUpdateInputModel input) {
        ImageDataSetUpdateInputModel in = (ImageDataSetUpdateInputModel) input;
        ((ImageDataSetMysqlModel) model).setForJobType(in.getForJobType());
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

        FileUtil.deleteFileOrDir(model.getNamespace());

        imageDataSetRepository.deleteById(model.getId());
        imageDataSetSampleRepository.deleteByDataSetId(model.getId());

        unionService.dontPublicDataSet(model.getId());

        CacheObjects.refreshImageDataSetTags();
    }

    /**
     * Paging query data set
     */
    public PagingOutput<ImageDataSetOutputModel> query(ImageDataSetQueryApi.Input input) {

        Specification<ImageDataSetMysqlModel> where = Where
                .create()
                .equal("id", input.getId())
                .contains("name", input.getName())
                .containsItem("tags", input.getTag())
                .equal("createdBy", input.getCreator())
                .build(ImageDataSetMysqlModel.class);

        return imageDataSetRepository.paging(where, input, ImageDataSetOutputModel.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public ImageDataSetAddOutputModel add(ImageDataSetAddInputModel input) throws StatusCodeWithException {

        File zipFile = new File(config.getFileUploadDir(), input.getFilename());

        ImageDataSetMysqlModel dataSet = new ImageDataSetMysqlModel();
        // image data set dir
        dataSet.setNamespace(
                Paths.get(
                                config.getFileUploadDir(),
                                IMAGE_DATA_SET_DIR,
                                dataSet.getId()
                        )
                        .toAbsolutePath()
                        .toString()
        );

        ZipUtil.UnzipFileResult unzipFileResult = null;
        List<ImageDataSetSampleMysqlModel> sampleList = null;
        try {
            unzipFileResult = ZipUtil.unzipFile(zipFile);
            sampleList = parseZipFile(dataSet, unzipFileResult);
            setImageDataSetModel(input, dataSet, sampleList);
        } catch (IOException e) {
            StatusCode.FILE_IO_ERROR.throwException(e);
        }

        // save models to database
        imageDataSetRepository.save(dataSet);
        imageDataSetSampleRepository.saveAll(sampleList);

        // delete source images
        FileUtil.deleteFileOrDir(zipFile);
        FileUtil.deleteFileOrDir(unzipFileResult.baseDir);


        // Synchronize information to union
        try {
            unionService.uploadImageDataSet(dataSet);
        } catch (StatusCodeWithException e) {
            super.log(e);
        }

        // Refresh the data set tag list
        CacheObjects.refreshTableDataSetTags();

        return new ImageDataSetAddOutputModel(dataSet.getId());
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
                StringUtil.join(labelSet, ",")
        );

        dataSet.setSampleCount(sampleList.size());
        dataSet.setLabeledCount(
                Convert.toInt(sampleList.stream().filter(x -> x.isLabeled()).count())
        );

        dataSet.setCompleted(
                sampleList.stream().allMatch(x -> x.isLabeled())
        );
        dataSet.setFilesSize(
                ListUtil.sumLong(sampleList, x -> x.getFileSize())
        );
        dataSet.setName(input.getName());
        dataSet.setTags(StringUtil.join(input.getTags(), ","));
        dataSet.setDescription(input.getDescription());
        dataSet.setStorageType(DataSetStorageType.LocalFileSystem);
        dataSet.setPublicLevel(input.getPublicLevel());
        dataSet.setPublicMemberList(input.getPublicMemberList());
        dataSet.setCreatedBy(input);
        super.handlePublicMemberList(dataSet);

    }

    /**
     * 解析 zip 文件，获取样本信息。
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
                .filter(x -> "xml".equalsIgnoreCase(FileUtil.getFileSuffix(x)))
                .collect(Collectors.toMap(
                        x -> FileUtil.getFileNameWithoutSuffix(x),
                        x -> x
                ));

        List<ImageDataSetSampleMysqlModel> result = new ArrayList<>();
        imageFiles.keySet()
                .parallelStream()
                .forEach(key -> {
                    File imageFile = imageFiles.get(key);
                    File xmlFile = xmlFiles.get(key);

                    try {
                        Annotation annotation = buildAnnotation(imageFile, xmlFile, dataSet);
                        ImageDataSetSampleMysqlModel sample = buildSample(dataSet, imageFile, annotation);
                        result.add(sample);
                    } catch (IOException e) {
                        super.log(e);
                    }
                });

        return result;
    }

    private ImageDataSetSampleMysqlModel buildSample(ImageDataSetMysqlModel dataSet, File imageFile, Annotation annotation) throws IOException {
        ImageDataSetSampleMysqlModel sample = new ImageDataSetSampleMysqlModel();
        sample.setDataSetId(dataSet.getId());
        sample.setFileName(imageFile.getName());
        sample.setFilePath(
                Paths.get(dataSet.getNamespace(), imageFile.getName()).toString()
        );
        sample.setFileSize(imageFile.length());
        sample.setCreatedBy(CurrentAccount.id());

        sample.setLabelList(StringUtil.join(annotation.getLabelList(), ","));
        sample.setLabeled(StringUtil.isNotEmpty(sample.getLabelList()));

        sample.setXmlAnnotation(XmlUtil.toXml(annotation));
        sample.setLabelInfo(new JSONObject());

        // move image to dest dir
        File destFile = new File(sample.getFilePath());
        if (destFile.exists()) {
            destFile.delete();
        }
        FileUtils.copyFile(imageFile, destFile);

        return sample;
    }

    /**
     * XmlUtil Doc: https://www.bookstack.cn/read/hutool/e41e0b0a699544fb.md
     */
    private Annotation buildAnnotation(File imageFile, File xmlFile, ImageDataSetMysqlModel dataSet) throws IOException {
        Annotation annotation;
        if (xmlFile != null) {
            annotation = XmlUtil.toModel(xmlFile, Annotation.class);
        } else {
            annotation = new Annotation();
        }

        BufferedImage image = ImageIO.read(new FileInputStream(imageFile));
        annotation.size = new Size();
        annotation.size.depth = image.getRaster().getNumDataElements();
        annotation.size.width = image.getWidth();
        annotation.size.height = image.getHeight();

        annotation.folder = dataSet.getNamespace();

        annotation.filename = imageFile.getName();
        annotation.path = Paths.get(annotation.folder, annotation.filename).toString();

        return annotation;
    }

}