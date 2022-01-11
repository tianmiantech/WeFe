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
package com.welab.wefe.board.service.service.data_resource;

import com.welab.wefe.board.service.api.data_resource.DataResourceQueryApi;
import com.welab.wefe.board.service.database.entity.data_resource.BloomFilterMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.ImageDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectDataSetMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.repository.ProjectDataSetRepository;
import com.welab.wefe.board.service.database.repository.ProjectRepository;
import com.welab.wefe.board.service.database.repository.base.BaseRepository;
import com.welab.wefe.board.service.database.repository.base.RepositoryManager;
import com.welab.wefe.board.service.database.repository.data_resource.BloomFilterRepository;
import com.welab.wefe.board.service.database.repository.data_resource.DataResourceRepository;
import com.welab.wefe.board.service.database.repository.data_resource.ImageDataSetRepository;
import com.welab.wefe.board.service.database.repository.data_resource.TableDataSetRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_resource.output.BloomFilterOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.DataResourceOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.ImageDataSetOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.TableDataSetOutputModel;
import com.welab.wefe.board.service.dto.entity.project.ProjectUsageDetailOutputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.AbstractDataResourceUpdateInputModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.data_resource.bloom_filter.BloomFilterService;
import com.welab.wefe.board.service.service.data_resource.image_data_set.ImageDataSetService;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.DataSetPublicLevel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author zane
 * @date 2021/12/1
 */
@Service
public class DataResourceService extends AbstractDataResourceService {
    @Autowired
    private ProjectDataSetRepository projectDataSetRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private TableDataSetService tableDataSetService;
    @Autowired
    private ImageDataSetService imageDataSetService;
    @Autowired
    private BloomFilterService bloomFilterSetService;
    @Autowired
    private TableDataSetRepository tableDataSetRepository;
    @Autowired
    private ImageDataSetRepository imageDataSetRepository;
    @Autowired
    private BloomFilterRepository bloomFilterRepository;

    /**
     * Update the number of data sets used in the project
     */
    public void updateUsageCountInProject(String dataSetId) {
        dataResourceRepository.updateUsageCountInProject(dataSetId);

        DataResourceMysqlModel model = (DataResourceMysqlModel) dataResourceRepository.findById(dataSetId).orElse(null);
        if (model == null) {
            return;
        }

        try {
            unionService.lazyUpdateDataResource(model);
        } catch (StatusCodeWithException e) {
            super.log(e);
        }
    }

    /**
     * The number of data sets used in the flow ++
     */
    public <T extends DataResourceMysqlModel> void usageCountInFlowIncrement(String dataSetId, Class<T> clazz) throws StatusCodeWithException {
        updateUsageCount(dataSetId, clazz, x -> x.setUsageCountInProject(x.getUsageCountInProject() + 1));
    }

    /**
     * The number of data sets used in the flow --
     */
    public <T extends DataResourceMysqlModel> void usageCountInFlowDecrement(String dataSetId, Class<T> clazz) throws StatusCodeWithException {
        updateUsageCount(dataSetId, clazz, x -> x.setUsageCountInFlow(x.getUsageCountInFlow() - 1));
    }

    /**
     * The number of data sets used in the job ++
     */
    public void usageCountInJobIncrement(String dataSetId) throws StatusCodeWithException {
        DataResourceMysqlModel one = (DataResourceMysqlModel) dataResourceRepository.findById(dataSetId).orElse(null);
        if (one == null) {
            return;
        }
        Class<? extends DataResourceMysqlModel> clazz = null;
        switch (one.getDataResourceType()) {
            case ImageDataSet:
                clazz = ImageDataSetMysqlModel.class;
                break;
            case TableDataSet:
                clazz = TableDataSetMysqlModel.class;
                break;
            case BloomFilter:
                clazz = BloomFilterMysqlModel.class;
                break;
            default:
        }
        updateUsageCount(dataSetId, clazz, x -> x.setUsageCountInJob(x.getUsageCountInJob() + 1));
    }

    /**
     * Update the various usage count of the data set
     */
    private <T extends DataResourceMysqlModel> void updateUsageCount(String dataSetId, Class<T> clazz, Consumer<T> func) throws StatusCodeWithException {
        BaseRepository repo = RepositoryManager.get(clazz);
        T model = (T) repo.findById(dataSetId).orElse(null);
        if (model == null) {
            return;
        }

        func.accept(model);
        repo.save(model);

        unionService.lazyUpdateDataResource(model);
    }


    /**
     * Query the project information used by the dataset in the project
     */
    public List<ProjectUsageDetailOutputModel> queryUsageInProject(String dataResourceId) {
        List<ProjectUsageDetailOutputModel> result = new ArrayList<>();

        // 查询资源的引用记录
        List<ProjectDataSetMySqlModel> usageInProjectList = projectDataSetRepository.queryUsageInProject(dataResourceId);
        if (usageInProjectList == null || usageInProjectList.isEmpty()) {
            return result;
        }

        // 查询引用资源的项目详情
        for (ProjectDataSetMySqlModel usageInProject : usageInProjectList) {
            ProjectMySqlModel project = projectRepository.findOneById(usageInProject.getProjectId());
            result.add(ModelMapper.map(project, ProjectUsageDetailOutputModel.class));
        }

        return result;
    }

    /**
     * update data set info
     */
    public void update(AbstractDataResourceUpdateInputModel input) throws StatusCodeWithException {
        DataResourceMysqlModel model = findOneById(input.getId());
        if (model == null) {
            return;
        }

        model.setUpdatedBy(input);
        model.setName(input.getName());
        model.setDescription(input.getDescription());
        model.setPublicMemberList(input.getPublicMemberList());
        model.setPublicLevel(input.getPublicLevel());
        model.setTags(standardizeTags(input.getTags()));
        handlePublicMemberList(model);

        beforeUpdate(model, input);
        RepositoryManager.get(model.getClass()).save(model);


        unionService.upsertDataResource(model);
        CacheObjects.refreshDataResourceTags(model.getDataResourceType());
    }

    /**
     * Process the list of visible members
     * <p>
     * When the scene is visible to the specified members, automatically add itself is also visible.
     */
    public void handlePublicMemberList(DataResourceMysqlModel model) {

        // When the PublicLevel is PublicWithMemberList, if list contains yourself,
        // you will be removed, and union will handle the data that you must be visible.
        if (model.getPublicLevel() == DataSetPublicLevel.PublicWithMemberList) {
            String memberId = CacheObjects.getMemberId();


            if (model.getPublicMemberList().contains(memberId)) {
                String list = model.getPublicMemberList()
                        .replace(memberId, "")
                        .replace(",,", ",");

                model.setPublicMemberList(list);
            }
        }

    }

    /**
     * Standardize the tag list
     */
    public String standardizeTags(List<String> tags) {
        if (tags == null) {
            return "";
        }

        tags = tags.stream()
                // Remove comma(,，)
                .map(x -> x.replace(",", "").replace("，", ""))
                // Remove empty elements
                .filter(x -> !StringUtil.isEmpty(x))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Concatenate into a string, add a comma before and after it to facilitate like query.
        return "," + StringUtil.join(tags, ',') + ",";

    }

    @Override
    public DataResourceMysqlModel findOneById(String dataSetId) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void beforeUpdate(DataResourceMysqlModel m, AbstractDataResourceUpdateInputModel in) {
        throw new UnsupportedOperationException();
    }

    /**
     * 从本地或 union 中获取一个 DataResource 的详细信息
     *
     * @param memberId       成员Id
     * @param dataResourceId 资源Id
     * @param mysqlClass     资源对应的 MysqlModel.class
     * @param outputClass    输出类型的 class
     */
    public <T extends DataResourceMysqlModel, O extends DataResourceOutputModel> O
    findDataResourceFromLocalOrUnion(String memberId, String dataResourceId, Class<T> mysqlClass, Class<O> outputClass) throws StatusCodeWithException {

        BaseRepository repository = RepositoryManager.get(mysqlClass);

        if (memberId.equals(CacheObjects.getMemberId())) {
            Object obj = repository.findById(dataResourceId).orElse(null);
            if (obj == null) {
                return null;
            }
            return ModelMapper.map(obj, outputClass);
        } else {
            return unionService.getDataResourceDetail(dataResourceId, outputClass);
        }
    }

    public DataResourceOutputModel findDataResourceFromLocalOrUnion(ProjectDataSetMySqlModel projectDataSet) throws StatusCodeWithException {

        if (CacheObjects.getMemberId().equals(projectDataSet.getMemberId())) {
            Object obj = dataResourceRepository.findById(projectDataSet).orElse(null);
            if (obj == null) {
                return null;
            }
            return ModelMapper.map(obj, DataResourceOutputModel.class);
        } else {
            return unionService.getDataResourceDetail(
                    projectDataSet.getDataSetId(),
                    projectDataSet.getDataResourceType(),
                    DataResourceOutputModel.class
            );
        }
    }


    public void delete(String dataResourceId, DataResourceType dataResourceType) throws StatusCodeWithException {
        switch (dataResourceType) {
            case ImageDataSet:
                imageDataSetService.delete(dataResourceId);
                break;
            case TableDataSet:
                tableDataSetService.delete(dataResourceId);
                break;
            case BloomFilter:
                bloomFilterSetService.delete(dataResourceId);
            default:
        }
    }


    public PagingOutput<? extends DataResourceOutputModel> query(DataResourceQueryApi.Input input) {
        Where where = Where
                .create()
                .equal("id", input.getId())
                .in("dataResourceType", input.getDataResourceType())
                .contains("name", input.getName())
                .containsItem("tags", input.getTag())
                .equal("createdBy", input.getCreator())
                .orderBy("createdTime", OrderBy.asc);

        // 查所有资源
        if (CollectionUtils.isEmpty(input.getDataResourceType()) || input.getDataResourceType().size() > 1) {
            PagingOutput<?> page = dataResourceRepository.paging(
                    where.build(DataResourceMysqlModel.class),
                    input
            );

            // 将查到的数据按类型转换为 output 类型
            List<DataResourceOutputModel> list = new ArrayList<>();
            for (Object item : page.getList()) {

                Class<? extends DataResourceOutputModel> targetClass = null;
                if (item instanceof TableDataSetMysqlModel) {
                    targetClass = TableDataSetOutputModel.class;
                } else if (item instanceof ImageDataSetMysqlModel) {
                    targetClass = ImageDataSetOutputModel.class;
                } else if (item instanceof BloomFilterMysqlModel) {
                    targetClass = BloomFilterOutputModel.class;
                }

                list.add(ModelMapper.map(item, targetClass));
            }

            return PagingOutput.of(page.getTotal(), list);
        }

        // 查所指定类型的资源
        switch (input.getDataResourceType().get(0)) {
            case TableDataSet:
                return tableDataSetRepository.paging(
                        where
                                .equal("containsY", input.getContainsY())
                                .equal("derivedResource", false)
                                .build(TableDataSetMysqlModel.class),
                        input,
                        TableDataSetOutputModel.class
                );
            case ImageDataSet:
                return imageDataSetRepository.paging(
                        where
                                .equal("forJobType", input.getForJobType())
                                .build(ImageDataSetMysqlModel.class),
                        input,
                        ImageDataSetOutputModel.class
                );
            case BloomFilter:
                return bloomFilterRepository.paging(
                        where.build(BloomFilterMysqlModel.class),
                        input,
                        BloomFilterOutputModel.class
                );
            default:
                return null;
        }

    }
}
