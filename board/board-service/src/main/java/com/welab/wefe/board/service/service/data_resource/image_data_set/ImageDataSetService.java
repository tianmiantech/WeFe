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
package com.welab.wefe.board.service.service.data_resource.image_data_set;

import com.welab.wefe.board.service.api.data_source.image_data_set.ImageDataSetDeleteApi;
import com.welab.wefe.board.service.api.data_source.image_data_set.ImageDataSetQueryApi;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.ImageDataSetSampleMysqlModel;
import com.welab.wefe.board.service.database.repository.ImageDataSetSampleRepository;
import com.welab.wefe.board.service.database.repository.data_resource.ImageDataSetRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_resource.output.ImageDataSetOutputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.AbstractDataResourceUpdateInputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.ImageDataSetAddInputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.ImageDataSetAddOutputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.ImageDataSetUpdateInputModel;
import com.welab.wefe.board.service.onlinedemo.OnlineDemoBranchStrategy;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.data_resource.DataResourceService;
import com.welab.wefe.board.service.service.data_resource.image_data_set.data_set_parser.AbstractImageDataSetParser;
import com.welab.wefe.board.service.service.data_resource.image_data_set.data_set_parser.ClassifyImageDataSetParser;
import com.welab.wefe.board.service.service.data_resource.image_data_set.data_set_parser.DetectionImageDataSetParser;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.decompression.SuperDecompressor;
import com.welab.wefe.common.decompression.dto.DecompressionResult;
import com.welab.wefe.common.enums.DataSetStorageType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.ListUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
        dataSet.setTotalDataCount(imageDataSetSampleRepository.getSampleCount(dataSetId));
        dataSet.setLabeledCount(imageDataSetSampleRepository.getLabeledCount(dataSetId));

        dataSet.setLabelCompleted(dataSet.getTotalDataCount().equals(dataSet.getLabeledCount()));

        imageDataSetRepository.save(dataSet);

        unionService.updateImageDataSetLabelInfo(dataSet);

    }

    @Override
    public ImageDataSetMysqlModel findOneById(String dataSetId) {
        return imageDataSetRepository.findById(dataSetId).orElse(null);
    }

    @Override
    protected void beforeUpdate(DataResourceMysqlModel m, AbstractDataResourceUpdateInputModel in) {
        ImageDataSetMysqlModel model = (ImageDataSetMysqlModel) m;
        ImageDataSetUpdateInputModel input = (ImageDataSetUpdateInputModel) in;
        model.setForJobType(input.getForJobType());
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

        FileUtil.deleteFileOrDir(model.getStorageNamespace());
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

    public ImageDataSetAddOutputModel add(ImageDataSetAddInputModel input) throws StatusCodeWithException {

        File dataSetFile = new File(config.getFileUploadDir(), input.getFilename());

        ImageDataSetMysqlModel dataSet = new ImageDataSetMysqlModel();
        // image data set dir
        dataSet.setStorageNamespace(
                Paths.get(
                                config.getFileUploadDir(),
                                StringUtil.stringToUnderLineLowerCase(dataSet.getResourceType().name()),
                                dataSet.getId()
                        )
                        .toAbsolutePath()
                        .toString()
        );

        DecompressionResult fileDecompressionResult = null;
        List<ImageDataSetSampleMysqlModel> sampleList = null;
        try {
            fileDecompressionResult = SuperDecompressor.decompression(dataSetFile, true);

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

        // Synchronize information to union
        try {
            unionService.uploadDataResource(dataSet);
        } catch (StatusCodeWithException e) {
            super.log(e);
        }

        // delete source images
        FileUtil.deleteFileOrDir(dataSetFile);
        fileDecompressionResult.deleteAllDirAndFiles();

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
