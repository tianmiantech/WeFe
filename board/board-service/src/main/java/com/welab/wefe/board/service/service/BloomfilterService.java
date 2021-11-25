/**
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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.api.bloomfilter.DeleteApi;
import com.welab.wefe.board.service.api.bloomfilter.QueryApi;
import com.welab.wefe.board.service.constant.BloomfilterAddMethod;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.board.service.database.entity.bloomfilter.BloomfilterMySqlModel;
import com.welab.wefe.board.service.database.repository.*;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.bloomfilter.BloomfilterOutputModel;
import com.welab.wefe.board.service.onlinedemo.OnlineDemoBranchStrategy;
import com.welab.wefe.board.service.sdk.UnionService;
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
public class BloomfilterService extends AbstractService {

    @Autowired
    protected BloomfilterRepository repo;
    @Autowired
    protected UnionService unionService;
    @Autowired
    protected BloomfilterStorageService bloomfilterStorageService;
    @Autowired
    protected JobRepository jobRepository;
    @Autowired
    protected JobMemberRepository jobMemberRepository;
    @Autowired
    protected JobRepository featureJobRepository;
    @Autowired
    DataSourceRepository dataSourceRepo;
    @Autowired
    private BloomfilterRepository bloomfilterRepository;
    @Autowired
    private Config config;
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
     * delete bloomfilter
     */
    public void delete(DeleteApi.Input input) throws StatusCodeWithException {
        BloomfilterMySqlModel model = repo.findById(input.getId()).orElse(null);
        if (model == null) {
            return;
        }

        OnlineDemoBranchStrategy.hackOnDelete(input, model, "只能删除自己添加的数据集。");

        delete(model);
    }

    /**
     * delete bloomfilter
     */
    public void delete(String bloomfilterId) throws StatusCodeWithException {
        BloomfilterMySqlModel model = repo.findById(bloomfilterId).orElse(null);
        if (model == null) {
            return;
        }

        delete(model);
    }

    /**
     * delete bloomfilter
     */
    public void delete(BloomfilterMySqlModel model) throws StatusCodeWithException {

        // delete bloomfilter from database
        repo.deleteById(model.getId());

        // delete bloomfilter from folder
        bloomfilterStorageService.deleteBloomfilter(model.getId());

        // TODO is raw bloomfilter
//        if (model.getSourceType() == null) {
//            // Notify the union to do not public the bloomfilter
//            unionService.dontPublicDataSet(model.getId());
//
//            // Refresh the bloomfilter tag list
//            CacheObjects.refreshDataSetTags();
//        }

    }

    /**
     * Paging query bloomfilter
     */
    public PagingOutput<BloomfilterOutputModel> query(QueryApi.Input input) {

        Specification<BloomfilterMySqlModel> where = Where
                .create()
                .equal("id", input.getId())
                .contains("name", input.getName())
                .containsItem("tags", input.getTag())
                .equal("containsY", input.getContainsY())
                .equal("createdBy", input.getCreator())
                .equal("sourceType", null, false)
                .build(BloomfilterMySqlModel.class);

        return repo.paging(where, input, BloomfilterOutputModel.class);
    }

    /**
     * Process the list of visible members
     * <p>
     * When the scene is visible to the specified members, automatically add itself is also visible.
     */
    public void handlePublicMemberList(BloomfilterMySqlModel model) {

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
    public DataSourceMySqlModel getDataSourceById(String dataSourceId) {
        return dataSourceRepo.findById(dataSourceId).orElse(null);
    }


    public BloomfilterMySqlModel findOne(String bloomfilterId) {
        return repo.findById(bloomfilterId).orElse(null);

    }

    /**
     * Test whether SQL can be queried normally
     */
    public boolean testSqlQuery(String dataSourceId, String sql) throws StatusCodeWithException {
        DataSourceMySqlModel model = getDataSourceById(dataSourceId);
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

        BloomfilterMySqlModel model = repo.findById(bloomfilterId).orElse(null);
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
     * Update the various usage count of the bloomfilter
     */
    private void updateUsageCount(String bloomfilterId, Consumer<BloomfilterMySqlModel> func) throws StatusCodeWithException {
        BloomfilterMySqlModel model = repo.findById(bloomfilterId).orElse(null);
        if (model == null) {
            return;
        }

        func.accept(model);
        repo.save(model);

//        unionService.uploadDataSet(model);
    }
}
