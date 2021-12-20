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

package com.welab.wefe.board.service.service.data_resource.add;

import com.welab.wefe.board.service.constant.DataSetAddMethod;
import com.welab.wefe.board.service.database.entity.DataSourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.BloomFilterMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceUploadTaskMysqlModel;
import com.welab.wefe.board.service.database.repository.data_resource.BloomFilterRepository;
import com.welab.wefe.board.service.dto.vo.data_resource.AbstractDataResourceUpdateInputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.BloomFilterAddInputModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.data_resource.DataResourceUploadTaskService;
import com.welab.wefe.board.service.service.data_resource.bloom_filter.BloomFilterColumnService;
import com.welab.wefe.board.service.service.data_resource.bloom_filter.BloomFilterService;
import com.welab.wefe.board.service.service.data_resource.bloom_filter.BloomFilterStorageService;
import com.welab.wefe.board.service.service.fusion.FieldInfoService;
import com.welab.wefe.board.service.util.*;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The service class for add bloom_filter
 *
 * @author jacky.jiang
 */
@Service
public class BloomFilterAddService extends AbstractDataResourceAddService {

    @Autowired
    protected BloomFilterRepository bloomFilterRepository;
    @Autowired
    protected BloomFilterService bloomfilterService;
    @Autowired
    protected BloomFilterStorageService bloomfilterStorageService;
    @Autowired
    protected BloomFilterColumnService bloomfilterColumnService;
    @Autowired
    protected DataResourceUploadTaskService dataResourceUploadTaskService;
    @Autowired
    protected FieldInfoService fieldInfoService;


    @Override
    protected void doAdd(AbstractDataResourceUpdateInputModel in, DataResourceUploadTaskMysqlModel task, DataResourceMysqlModel m) throws StatusCodeWithException {
        BloomFilterAddInputModel input = (BloomFilterAddInputModel) in;
        BloomFilterMysqlModel model = (BloomFilterMysqlModel) m;
        model.setSourcePath(config.getFileUploadDir() + input.getFilename());
        model.setDataSourceId(input.getDataSourceId());
        model.setHashFunction(String.valueOf(input.getHashFunction()));
        fieldInfoService.saveAll(model.getId(), input.getFieldInfoList());

        // save bloom_filter info to file
        model.setUpdatedTime(new Date());
        bloomFilterRepository.save(model);

        // Parse and save the original data
        try {
            AbstractBloomFilterReader bloomfilterReader = createBloomfilterReader(input);
            readAllToFilterFile(model, bloomfilterReader, input.isDeduplication());
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            dataResourceUploadTaskService.onError(task.getId(), e);
            return;
        }

        // save bloom_filter column info to database
        bloomfilterColumnService.update(model.getId(), input.getMetadataList());

        // Delete files uploaded by HttpUpload
        try {
            if (input.getBloomfilterAddMethod().equals(DataSetAddMethod.HttpUpload)) {
                File file = bloomfilterService.getBloomfilterFile(input.getBloomfilterAddMethod(), input.getFilename());
                FileUtils.deleteQuietly(file);
            }
        } catch (StatusCodeWithException e) {
            super.log(e);
        }

        // Refresh the bloom_filter tag list
        CacheObjects.refreshDataResourceTags(model.getDataResourceType());
    }

    /**
     * create AbstractDataSetReader
     */
    private AbstractBloomFilterReader createBloomfilterReader(BloomFilterAddInputModel input) throws StatusCodeWithException {
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
     * create CsvBloomFilterReader/ExcelBloomfilterReader
     */
    private AbstractBloomFilterReader createFileBloomfilterReader(BloomFilterAddInputModel input) throws StatusCodeWithException {
        try {
            File file = bloomfilterService.getBloomfilterFile(input.getBloomfilterAddMethod(), input.getFilename());
            boolean isCsv = file.getName().endsWith("csv");
            return isCsv
                    ? new CsvBloomFilterReader(input.getMetadataList(), file)
                    : new ExcelBloomfilterReader(input.getMetadataList(), file);

        } catch (IOException e) {
            StatusCode.FILE_IO_ERROR.throwException(e);
            return null;
        }
    }

    /**
     * create SqlDataSetReader
     */
    private SqlBloomFilterReader createSqlBloomfilterReader(BloomFilterAddInputModel input) throws StatusCodeWithException {
        DataSourceMysqlModel dataSource = bloomfilterService.getDataSourceById(input.getDataSourceId());
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

        return new SqlBloomFilterReader(input.getMetadataList(), conn, input.getSql());
    }

    /**
     * Parse the bloom_filter file and save it to filter file
     *
     * @param deduplication Do you need to de-duplicate the bloom_filter
     */
    private void readAllToFilterFile(BloomFilterMysqlModel model, AbstractBloomFilterReader bloomfilterReader, boolean deduplication) throws StatusCodeWithException {
        long start = System.currentTimeMillis();
        LOG.info("开始解析过滤器：" + model.getId());

        // update data set upload task info
        DataResourceUploadTaskMysqlModel uploadProgress = dataResourceUploadTaskService.findByDataResourceId(model.getId());
        dataResourceUploadTaskService.update(uploadProgress, x -> x.setTotalDataCount(bloomfilterReader.getTotalDataRowCount()));

        // get bloom_filter headers
        List<String> rawHeaders = bloomfilterReader.getHeader();

        // data row consumption method
        BloomFilterAddServiceDataRowConsumer dataRowConsumer = new BloomFilterAddServiceDataRowConsumer(model, deduplication, bloomfilterReader);

        // read all data rows of the raw bloom_filter
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


    @Override
    protected Class<? extends DataResourceMysqlModel> getMysqlModelClass() {
        return BloomFilterMysqlModel.class;
    }

    @Override
    protected DataResourceType getDataResourceType() {
        return DataResourceType.BloomFilter;
    }
}
