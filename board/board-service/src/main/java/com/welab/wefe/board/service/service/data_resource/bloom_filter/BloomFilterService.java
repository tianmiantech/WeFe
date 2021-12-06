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

package com.welab.wefe.board.service.service.data_resource.bloom_filter;

import com.welab.wefe.board.service.api.data_resource.bloom_filter.BloomFilterDeleteApi;
import com.welab.wefe.board.service.api.data_resource.bloom_filter.BloomFilterQueryApi;
import com.welab.wefe.board.service.constant.BloomfilterAddMethod;
import com.welab.wefe.board.service.database.entity.DataSourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.BloomFilterMysqlModel;
import com.welab.wefe.board.service.database.repository.DataSourceRepository;
import com.welab.wefe.board.service.database.repository.JobMemberRepository;
import com.welab.wefe.board.service.database.repository.JobRepository;
import com.welab.wefe.board.service.database.repository.ProjectRepository;
import com.welab.wefe.board.service.database.repository.data_resource.BloomFilterRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.fusion.BloomFilterOutputModel;
import com.welab.wefe.board.service.onlinedemo.OnlineDemoBranchStrategy;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.util.JdbcManager;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.DataSetPublicLevel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author jacky.jiang
 */
@Service
public class BloomFilterService extends AbstractService {

    @Autowired
    protected BloomFilterRepository repo;
    @Autowired
    protected BloomFilterStorageService bloomfilterStorageService;
    @Autowired
    protected JobRepository jobRepository;
    @Autowired
    protected JobMemberRepository jobMemberRepository;
    @Autowired
    protected JobRepository featureJobRepository;
    @Autowired
    DataSourceRepository dataSourceRepo;
    @Autowired
    private BloomFilterRepository bloomfilterRepository;
    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Get uploaded file
     */
    public File getBloomfilterFile(BloomfilterAddMethod method, String filename) throws StatusCodeWithException {
        File file = null;
        switch (method) {
            case HttpUpload:
                file = new File(config.getFileUploadDir(), filename);
                break;
            case LocalFile:
                file = new File(filename);
                break;
            case Database:
                break;
            default:
        }

        if (null == file || !file.exists()) {
            throw new StatusCodeWithException("未找到文件：" + filename, StatusCode.PARAMETER_VALUE_INVALID);
        }

        return file;
    }

    /**
     * delete bloom_filter
     */
    public void delete(BloomFilterDeleteApi.Input input) throws StatusCodeWithException {
        BloomFilterMysqlModel model = repo.findById(input.getId()).orElse(null);
        if (model == null) {
            return;
        }

        OnlineDemoBranchStrategy.hackOnDelete(input, model, "只能删除自己添加的数据集。");

        delete(model);
    }

    /**
     * delete bloom_filter
     */
    public void delete(String bloomfilterId) throws StatusCodeWithException {
        BloomFilterMysqlModel model = repo.findById(bloomfilterId).orElse(null);
        if (model == null) {
            return;
        }

        delete(model);
    }

    /**
     * delete bloom_filter
     */
    public void delete(BloomFilterMysqlModel model) throws StatusCodeWithException {

        // delete bloom_filter from database
        repo.deleteById(model.getId());

        // delete bloom_filter from folder
        bloomfilterStorageService.deleteBloomfilter(model.getId());

        // TODO is raw bloom_filter
//        if (model.getSourceType() == null) {
//            // Notify the union to do not public the bloom_filter
//            unionService.dontPublicDataSet(model.getId());
//
//            // Refresh the bloom_filter tag list
//            CacheObjects.refreshDataSetTags();
//        }

    }

    /**
     * Paging query bloom_filter
     */
    public PagingOutput<BloomFilterOutputModel> query(BloomFilterQueryApi.Input input) {

        Specification<BloomFilterMysqlModel> where = Where
                .create()
                .equal("id", input.getId())
                .contains("name", input.getName())
                .containsItem("tags", input.getTag())
                .equal("createdBy", input.getCreator())
                .equal("sourceType", null, false)
                .build(BloomFilterMysqlModel.class);

        return repo.paging(where, input, BloomFilterOutputModel.class);
    }

    /**
     * Process the list of visible members
     * <p>
     * When the scene is visible to the specified members, automatically add itself is also visible.
     */
    public void handlePublicMemberList(BloomFilterMysqlModel model) {

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

    /**
     * get data source by id
     */
    public DataSourceMysqlModel getDataSourceById(String dataSourceId) {
        return dataSourceRepo.findById(dataSourceId).orElse(null);
    }


    public BloomFilterMysqlModel findOne(String bloomfilterId) {
        return repo.findById(bloomfilterId).orElse(null);

    }

    /**
     * Test whether SQL can be queried normally
     */
    public boolean testSqlQuery(String dataSourceId, String sql) throws StatusCodeWithException {
        DataSourceMysqlModel model = getDataSourceById(dataSourceId);
        if (model == null) {
            throw new StatusCodeWithException("dataSourceId在数据库不存在", StatusCode.DATA_NOT_FOUND);
        }

        if (StringUtils.isEmpty(sql)) {
            throw new StatusCodeWithException("请填入sql查询语句", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
        }

        Connection conn = JdbcManager.getConnection(
                model.getDatabaseType(),
                model.getHost(),
                model.getPort(),
                model.getUserName(),
                model.getPassword(),
                model.getDatabaseName()
        );

        return JdbcManager.testQuery(conn, sql, true);
    }

    /**
     * Update the number of Bloomfilter used in the project
     */
    public void updateUsageCountInProject(String bloomfilterId) {
//        bloomfilterRepository.updateUsageCountInProject(bloomfilterId);

        BloomFilterMysqlModel model = repo.findById(bloomfilterId).orElse(null);
        if (model == null) {
            return;
        }

//        try {
//            unionService.uploadDataSet(model);
//        } catch (StatusCodeWithException e) {
//            super.log(e);
//        }
    }

    /**
     * Update the various usage count of the bloom_filter
     */
    private void updateUsageCount(String bloomfilterId, Consumer<BloomFilterMysqlModel> func) throws StatusCodeWithException {
        BloomFilterMysqlModel model = repo.findById(bloomfilterId).orElse(null);
        if (model == null) {
            return;
        }

        func.accept(model);
        repo.save(model);

//        unionService.uploadDataSet(model);
    }
}
