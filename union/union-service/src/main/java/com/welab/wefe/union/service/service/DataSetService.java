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

package com.welab.wefe.union.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.dataset.DataSetQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.union.DataSet;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.wefe.enums.DataResourcePublicLevel;
import com.welab.wefe.union.service.api.dataresource.dataset.nomal.DeleteApi;
import com.welab.wefe.union.service.api.dataresource.dataset.nomal.DetailApi;
import com.welab.wefe.union.service.api.dataresource.dataset.nomal.PutApi;
import com.welab.wefe.union.service.api.dataresource.dataset.nomal.QueryApi;
import com.welab.wefe.union.service.dto.dataresource.dataset.table.ApiDataSetQueryOutput;
import com.welab.wefe.union.service.dto.dataresource.dataset.table.DataSetDetailOutput;
import com.welab.wefe.union.service.service.contract.DataSetContractService;
import com.welab.wefe.union.service.service.contract.DataSetMemberPermissionContractService;
import com.welab.wefe.union.service.util.MapperUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataSetService {
    @Autowired
    protected DataSetContractService datasetContractService;
    @Autowired
    private DataSetMemberPermissionContractService dataSetMemberPermissionContractService;
    @Autowired
    protected DataSetMongoReop dataSetMongoReop;
    @Autowired
    protected DataSetContractService dataSetContractService;

    public void add(PutApi.Input input) throws StatusCodeWithException {
        com.welab.wefe.union.service.entity.DataSet dataSet = new com.welab.wefe.union.service.entity.DataSet();
        BeanUtils.copyProperties(input, dataSet);
        dataSet.setContainsY(input.getContainsY() ? 1 : 0);
        dataSet.setMemberId(input.curMemberId);

        String publicMemberList = input.getPublicMemberList();

        if (DataResourcePublicLevel.OnlyMyself.name().equals(input.getPublicLevel())) {
            dataSetMemberPermissionContractService.deleteByDataSetId(dataSet.getId());
            dataSet.setPublicLevel(input.getPublicLevel());

        } else if (DataResourcePublicLevel.Public.name().equals(input.getPublicLevel())) {
            dataSetMemberPermissionContractService.deleteByDataSetId(dataSet.getId());
            dataSet.setPublicLevel(input.getPublicLevel());

        } else if (DataResourcePublicLevel.PublicWithMemberList.name().equals(input.getPublicLevel())) {
            dataSetMemberPermissionContractService.save(dataSet.getId(), publicMemberList);
            dataSet.setPublicLevel(input.getPublicLevel());

        } else {
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "Invalid public level");
        }

        dataSetContractService.upsert(dataSet);
    }

    public void delete(DeleteApi.Input input) throws StatusCodeWithException {
        datasetContractService.deleteById(input.getId());
        dataSetMemberPermissionContractService.deleteByDataSetId(input.getId());
    }

    public DataSetDetailOutput detail(DetailApi.Input input) {
        DataSet dataSet = dataSetMongoReop.findDataSetId(input.getId());
        if (dataSet == null) {
            return null;
        }
        return MapperUtil.transferDataSetDetail(dataSet);
    }


    public PageOutput<ApiDataSetQueryOutput> query(QueryApi.Input input) {
        PageOutput<DataSetQueryOutput> pageOutput = dataSetMongoReop.findCurMemberCanSee(MapperUtil.transferToDataSetInput(input));

        List<ApiDataSetQueryOutput> list = pageOutput.getList().stream()
                .map(MapperUtil::transferToDataSetOutput)
                .collect(Collectors.toList());

        return new PageOutput<>(pageOutput.getPageIndex(), pageOutput.getTotal(), pageOutput.getPageSize(), pageOutput.getTotalPage(), list);
    }
}
