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
import com.welab.wefe.board.service.onlinedemo.OnlineDemoBranchStrategy;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.dataset.image_data_set.AbstractImageDataSetParser;
import com.welab.wefe.board.service.service.dataset.image_data_set.ClassifyImageDataSetParser;
import com.welab.wefe.board.service.service.dataset.image_data_set.DetectionImageDataSetParser;
import com.welab.wefe.common.Convert;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.DataSetStorageType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.ListUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.ZipUtil;
import com.welab.wefe.common.util.dto.FileDecompressionResult;
import com.welab.wefe.common.web.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

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
            return unionService.getImageDataSetDetail(dataSetId);
        }
    }

    public synchronized void updateLabelInfo(String dataSetId) {
        ImageDataSetMysqlModel dataSet = findOneById(dataSetId);
        TreeSet<String> labelSet = new TreeSet<>();
        imageDataSetSampleRepository.getAllDistinctLabelList(dataSetId)
                .stream()
                .filter(x -> StringUtil.isNotEmpty(x))
                .forEach(x ->
                        labelSet.addAll(Arrays.asList(x.split(",")))
                );

        dataSet.setLabelList(StringUtil.joinByComma(labelSet));
        dataSet.setSampleCount(imageDataSetSampleRepository.getSampleCount(dataSetId));
        dataSet.setLabeledCount(imageDataSetSampleRepository.getLabelCount(dataSetId));

        dataSet.setLabelCompleted(dataSet.getSampleCount() == dataSet.getLabeledCount());

        imageDataSetRepository.save(dataSet);

        unionService.updateImageDataSetLabelInfo(dataSet);

    }

    @Override
    public ImageDataSetMysqlModel findOneById(String dataSetId) {
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

        imageDataSetRepository.deleteById(model.getId());
        imageDataSetSampleRepository.deleteByDataSetId(model.getId());

        FileUtil.deleteFileOrDir(model.getNamespace());
        CacheObjects.refreshImageDataSetTags();

        unionService.dontPublicDataSet(model);
    }

    /**
     * Paging query data set
     */
    public PagingOutput<ImageDataSetOutputModel> query(ImageDataSetQueryApi.Input input) {

        Specification<ImageDataSetMysqlModel> where = Where
                .create()
                .equal("id", input.getId())
                .equal("forJobType", input.getForJobType())
                .contains("name", input.getName())
                .containsItem("tags", input.getTag())
                .equal("createdBy", input.getCreator())
                .build(ImageDataSetMysqlModel.class);

        return imageDataSetRepository.paging(where, input, ImageDataSetOutputModel.class);
    }

    @Autowired
    private DetectionImageDataSetParser detectionImageDataSetParser;
    @Autowired
    private ClassifyImageDataSetParser classifyImageDataSetParser;

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

        FileDecompressionResult fileDecompressionResult = null;
        List<ImageDataSetSampleMysqlModel> sampleList = null;
        try {
            fileDecompressionResult = ZipUtil.unzipFile(zipFile);

            AbstractImageDataSetParser dataSetParser = null;
            switch (input.forJobType) {
                case classify:
                    dataSetParser = classifyImageDataSetParser;
                    break;
                case detection:
                    dataSetParser = detectionImageDataSetParser;
                    break;
                default:
                    StatusCode.UNEXPECTED_ENUM_CASE.throwException();
            }
            sampleList = dataSetParser.parseFilesToSamples(dataSet, fileDecompressionResult.files);
            setImageDataSetModel(input, dataSet, sampleList);
        } catch (Exception e) {
            super.log(e);
            StatusCode.FILE_IO_ERROR.throwException(e);
        }

        // save models to database
        imageDataSetRepository.save(dataSet);
        imageDataSetSampleRepository.saveAll(sampleList);

        // delete source images
        FileUtil.deleteFileOrDir(zipFile);
        fileDecompressionResult.deleteAllDirAndFiles();


        // Synchronize information to union
        try {
            unionService.uploadImageDataSet(dataSet);
        } catch (StatusCodeWithException e) {
            super.log(e);
        }

        // Refresh the data set tag list
        CacheObjects.refreshImageDataSetTags();

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
                StringUtil.joinByComma(labelSet)
        );

        dataSet.setSampleCount(sampleList.size());
        dataSet.setLabeledCount(
                Convert.toInt(sampleList.stream().filter(x -> x.isLabeled()).count())
        );

        dataSet.setLabelCompleted(
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

    public File download(String id) {
        ImageDataSetMysqlModel dataSet = findOneById(id);


        return null;
    }
}
