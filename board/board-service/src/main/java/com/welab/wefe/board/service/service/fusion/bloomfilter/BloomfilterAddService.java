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

package com.welab.wefe.board.service.service.fusion.bloomfilter;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.constant.DataSetAddMethod;
import com.welab.wefe.board.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.board.service.database.entity.fusion.bloomfilter.BloomFilterMySqlModel;
import com.welab.wefe.board.service.database.entity.fusion.bloomfilter.BloomFilterTaskMysqlModel;
import com.welab.wefe.board.service.database.repository.fusion.BloomFilterRepository;
import com.welab.wefe.board.service.dto.vo.BloomfilterAddInputModel;
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.fusion.BloomfilterColumnService;
import com.welab.wefe.board.service.service.fusion.BloomfilterService;
import com.welab.wefe.board.service.service.fusion.BloomfilterStorageService;
import com.welab.wefe.board.service.service.fusion.FieldInfoService;
import com.welab.wefe.board.service.util.*;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The service class for add bloomfilter
 *
 * @author jacky.jiang
 */
@Service
public class BloomfilterAddService extends AbstractService {

    @Autowired
    protected BloomFilterRepository repo;
    @Autowired
    protected BloomfilterService bloomfilterService;
    @Autowired
    protected BloomfilterStorageService bloomfilterStorageService;
    @Autowired
    protected BloomfilterColumnService bloomfilterColumnService;
    @Autowired
    protected UnionService unionService;
    @Autowired
    protected BloomfilterTaskService bloomfilterTaskService;
    @Autowired
    protected FieldInfoService fieldInfoService;
    @Autowired
    private Config config;


    /**
     * Asynchronous execution of data writing
     *
     * @param userInfo Since this method is executed in an asynchronous thread,
     *                 the CurrentAccount information cannot be obtained, so it needs to be passed.
     */
    @Async
    public void add(BloomfilterAddInputModel input, BloomFilterTaskMysqlModel bloomfilterTask, CurrentAccount.Info userInfo) {

        BloomFilterMySqlModel model = new ModelMapper().map(input, BloomFilterMySqlModel.class);
        model.setId(bloomfilterTask.getBloomfilterId());
        model.setCreatedBy("test");
        model.setTags(bloomfilterService.standardizeTags(input.getTags()));
        bloomfilterService.handlePublicMemberList(model);
        model.setSourcePath(config.getFileUploadDir()+input.getFilename());
        model.setName(input.getName());
        model.setDataSourceId(input.getDataSourceId());
        model.setUpdatedBy(CurrentAccount.id());
        model.setCreatedBy(CurrentAccount.id());
        model.setDescription(input.getDescription());
        model.setBloomfilterAddMethod(input.getBloomfilterAddMethod());
        model.setColumnNameList(StringUtil.join(input.getRows(), ','));
        fieldInfoService.saveAll(model.getId(), input.getFieldInfoList());

        model.setUsageCount(0);
        model.setUpdatedTime(new Date());
        repo.save(model);

        // Parse and save the original data
        try {
            AbstractBloomfilterReader bloomfilterReader = createBloomfilterReader(input);
            readAllToFilterFile(model, bloomfilterReader, input.isDeduplication());
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            bloomfilterTaskService.onError(bloomfilterTask.getBloomfilterId(), e);
            return;
        }

        // save bloomfilter info to file
//        repo.save(model);

        // save bloomfilter column info to database
        bloomfilterColumnService.update(model.getId(), input.getMetadataList(), userInfo);

        // Mark upload task completed
        bloomfilterTaskService.complete(bloomfilterTask.getBloomfilterId());

        // Delete files uploaded by HttpUpload
        try {
            if (input.getBloomfilterAddMethod().equals(DataSetAddMethod.HttpUpload)) {
                File file = bloomfilterService.getBloomfilterFile(input.getBloomfilterAddMethod(), input.getFilename());
                FileUtils.deleteQuietly(file);
            }
        } catch (StatusCodeWithException e) {
            super.log(e);
        }

        // Synchronize information to union
//        try {
//            unionService.uploadDataSet(model);
//        } catch (StatusCodeWithException e) {
//            super.log(e);
//        }

        // Refresh the bloomfilter tag list
        CacheObjects.refreshDataSetTags();

    }

    /**
     * create AbstractDataSetReader
     */
    private AbstractBloomfilterReader createBloomfilterReader(BloomfilterAddInputModel input) throws StatusCodeWithException {
        switch (input.getBloomfilterAddMethod()) {
            case Database:
                return createSqlBloomfilterReader(input);
            case HttpUpload:
            case LocalFile:
                return createFileBloomfilterReader(input);
            default:
                StatusCode
                        .UNEXPECTED_ENUM_CASE
                        .throwException("暂不支持的过滤器解析方式：" + input.getBloomfilterAddMethod());
        }

        return null;
    }

    /**
     * create CsvBloomfilterReader/ExcelBloomfilterReader
     */
    private AbstractBloomfilterReader createFileBloomfilterReader(BloomfilterAddInputModel input) throws StatusCodeWithException {
        try {
            File file = bloomfilterService.getBloomfilterFile(input.getBloomfilterAddMethod(), input.getFilename());
            boolean isCsv = file.getName().endsWith("csv");
            return isCsv
                    ? new CsvBloomfilterReader(input.getMetadataList(), file)
                    : new ExcelBloomfilterReader(input.getMetadataList(), file);

        } catch (IOException e) {
            StatusCode.FILE_IO_ERROR.throwException(e);
            return null;
        }
    }

    /**
     * create SqlDataSetReader
     */
    private SqlBloomfilterReader createSqlBloomfilterReader(BloomfilterAddInputModel input) throws StatusCodeWithException {
        DataSourceMySqlModel dataSource = bloomfilterService.getDataSourceById(input.getDataSourceId());
        if (dataSource == null) {
            throw new StatusCodeWithException("此dataSourceId在数据库不存在", StatusCode.DATA_NOT_FOUND);
        }
        Connection conn = JdbcManager.getConnection(
                dataSource.getDatabaseType(),
                dataSource.getHost(),
                dataSource.getPort(),
                dataSource.getUserName(),
                dataSource.getPassword(),
                dataSource.getDatabaseName()
        );

        return new SqlBloomfilterReader(input.getMetadataList(), conn, input.getSql());
    }

    /**
     * Parse the bloomfilter file and save it to filter file
     *
     * @param deduplication Do you need to de-duplicate the bloomfilter
     */
    private void readAllToFilterFile(BloomFilterMySqlModel model, AbstractBloomfilterReader bloomfilterReader, boolean deduplication) throws StatusCodeWithException {
        long start = System.currentTimeMillis();
        LOG.info("开始解析过滤器：" + model.getId());

        // update bloomfilter upload task info
        BloomFilterTaskMysqlModel bloomfilterTask = bloomfilterTaskService.findByBloomfilterId(model.getId());
        bloomfilterTaskService.update(bloomfilterTask, x -> x.setTotalRowCount(bloomfilterReader.getTotalDataRowCount()));
        // get bloomfilter headers
        List<String> rawHeaders = bloomfilterReader.getHeader();

        String filterDir = config.getFilterDir();
        String bloomfilterPath = filterDir + model.getName();
        File outFile = new File(filterDir);

        if (!outFile.exists() && !outFile.isDirectory()) {
            outFile.mkdir();
        }

        // data row consumption method
        BloomfilterAddServiceDataRowConsumer dataRowConsumer = new BloomfilterAddServiceDataRowConsumer(model.getId(), deduplication, bloomfilterReader, bloomfilterPath);

        // read all data rows of the raw bloomfilter
        bloomfilterReader.readAll(dataRowConsumer);

        // wait for the consumption queue to finish
        dataRowConsumer.waitForFinishAndClose();

        LOG.info("过滤器解析完毕：" + model.getId() + " spend:" + ((System.currentTimeMillis() - start) / 1000) + "s");


    }

    /**
     * sort headers, move column y to the second column.
     */
    private List<String> sortHeaders(List<String> headers) {
        if (!headers.contains("y")) {
            return headers;
        }

        // A new list must be opened here, and the original column header cannot be modified.
        List<String> list = new ArrayList<>();
        for (String name : headers) {
            if ("y".equals(name)) {
                continue;
            }
            list.add(name);
        }
        list.add(1, "y");
        return list;
    }

}
