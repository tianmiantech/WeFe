/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.data.fusion.service.service.bloomfilter;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.data.fusion.service.api.bloomfilter.DeleteApi;
import com.welab.wefe.data.fusion.service.api.bloomfilter.PreviewApi;
import com.welab.wefe.data.fusion.service.api.bloomfilter.QueryApi;
import com.welab.wefe.data.fusion.service.database.entity.BloomFilterMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.BloomFilterRepository;
import com.welab.wefe.data.fusion.service.dto.base.PagingOutput;
import com.welab.wefe.data.fusion.service.dto.entity.bloomfilter.BloomfilterOutputModel;
import com.welab.wefe.data.fusion.service.dto.entity.dataset.DataSetDetailOutputModel;
import com.welab.wefe.data.fusion.service.dto.entity.dataset.DataSetPreviewOutputModel;
import com.welab.wefe.data.fusion.service.enums.DataResourceSource;
import com.welab.wefe.data.fusion.service.service.AbstractService;
import com.welab.wefe.data.fusion.service.service.DataSourceService;
import com.welab.wefe.data.fusion.service.service.FieldInfoService;
import com.welab.wefe.data.fusion.service.utils.dataresouce.DataResouceHelper;
import com.welab.wefe.data.fusion.service.utils.primarykey.FieldInfo;
import com.welab.wefe.data.fusion.service.utils.primarykey.PrimaryKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author hunter.zhao
 */
@Service
public class BloomFilterService extends AbstractService {

    @Autowired
    private BloomFilterRepository bloomFilterRepository;


    @Autowired
    private FieldInfoService fieldInfoService;

    @Autowired
    DataSourceService dataSourceService;


    /**
     * @param bloomFilterId
     */
    public void increment(String bloomFilterId) {
        BloomFilterMySqlModel model = bloomFilterRepository.findOne("id", bloomFilterId, BloomFilterMySqlModel.class);
        model.setUsedCount(model.getUsedCount() + 1);
        model.setUpdatedTime(new Date());
        bloomFilterRepository.save(model);
    }


    public BloomFilterMySqlModel findById(String bloomFilterId) {
        return bloomFilterRepository.findOne("id", bloomFilterId, BloomFilterMySqlModel.class);
    }


    /**
     * Filter list
     */
    public List<BloomFilterMySqlModel> list() {
        return bloomFilterRepository.findAll();
    }

    /**
     * Paging query data sets
     */
    public PagingOutput<BloomfilterOutputModel> query(QueryApi.Input input) {
        Specification<BloomFilterMySqlModel> where = Where
                .create()
                .equal("id", input.getId())
                .contains("name", input.getName())
                .build(BloomFilterMySqlModel.class);

        return bloomFilterRepository.paging(where, input, BloomfilterOutputModel.class);
    }

    /**
     * Delete filter
     *
     * @param input
     */
    public void delete(DeleteApi.Input input) {
        BloomFilterMySqlModel model = bloomFilterRepository.findById(input.getId()).orElse(null);
        if (model == null) {
            return;
        }

        bloomFilterRepository.deleteById(input.getId());
        String src = model.getSrc();
        if (StringUtil.isEmpty(src)) {
            return;
        }

        File file = new File(src);
        if (file.exists()) {
            file.delete();
            System.out.println("删除成功");
        }
    }


    /**
     *  Filter Detail
     *
     * @param id
     */
    public BloomfilterOutputModel detail(String id) throws StatusCodeWithException {
        BloomFilterMySqlModel model = bloomFilterRepository.findById(id).orElse(null);
        if (model == null) {
            throw new StatusCodeWithException("数据不存在！", StatusCode.DATA_NOT_FOUND);
        }


        BloomfilterOutputModel outputModel = ModelMapper.map(model, BloomfilterOutputModel.class);

        List<FieldInfo> fieldInfoList = fieldInfoService.fieldInfoList(id);
        outputModel.setHashFusion(PrimaryKeyUtils.hashFunction(fieldInfoList));

        return outputModel;
    }

    public DataSetPreviewOutputModel preview(PreviewApi.Input input) throws Exception{
        DataResourceSource dataResourceSource = input.getDataResourceSource();
        DataSetPreviewOutputModel output = new DataSetPreviewOutputModel();

        // Preview by reading data from the database
        if (dataResourceSource == null) {
            BloomFilterMySqlModel bloomFilterMySqlModel = findById(input.getId());
            if (bloomFilterMySqlModel == null) {
                throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "Filter not found");
            }

            String rows = input.getRows();
            List<String> rowsList = Arrays.asList(rows.split(","));

            if (bloomFilterMySqlModel.getDataResourceSource().equals(DataResourceSource.Sql)) {
                String sql = bloomFilterMySqlModel.getStatement();
                // Test whether SQL can be queried normally
                boolean result = dataSourceService.testSqlQuery(bloomFilterMySqlModel.getDataSourceId(), sql);
                if (result) {
                    output = DataResouceHelper.readFromDB(bloomFilterMySqlModel.getDataSourceId(), sql, rowsList);
                }
            }else if (bloomFilterMySqlModel.getDataResourceSource().equals(DataResourceSource.UploadFile) || bloomFilterMySqlModel.getDataResourceSource().equals(DataResourceSource.LocalFile)){
                File file = dataSourceService.getDataSetFile(bloomFilterMySqlModel.getDataResourceSource(), bloomFilterMySqlModel.getSourcePath());
                try {
                    output = DataResouceHelper.readFile(file, rowsList);
                } catch (IOException e) {
                    LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                    throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "文件读取失败");
                }
            }
        } else if (DataResourceSource.Sql.equals(dataResourceSource)) {
            output = DataResouceHelper.readFromSourceDB(input.getId(), input.getSql());
        } else if (dataResourceSource.equals(DataResourceSource.UploadFile) || dataResourceSource.equals(DataResourceSource.LocalFile)) {
            File file = dataSourceService.getDataSetFile(input.getDataResourceSource(), input.getFilename());
            try {
                output = DataResouceHelper.readFile(file);
            } catch (IOException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "文件读取失败");
            }
        }

        return output;
    }


    public DataSetDetailOutputModel detailAndPreview(String id) throws Exception {
        BloomFilterMySqlModel model = bloomFilterRepository.findById(id).orElse(null);
        if (model == null) {
            throw new StatusCodeWithException("数据不存在！", StatusCode.DATA_NOT_FOUND);
        }

        DataSetDetailOutputModel outputModel = ModelMapper.map(model, DataSetDetailOutputModel.class);

        PreviewApi.Input input = new PreviewApi.Input();
        input.setId(id);
        input.setRows(model.getRows());
        DataSetPreviewOutputModel previewOutputModel =  preview(input);

        outputModel.setPreviewData(previewOutputModel);
        return outputModel;
    }
}
