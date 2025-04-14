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

import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.entity.union.TableDataSet;
import com.welab.wefe.common.data.mongodb.repo.TableDataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.union.service.api.dataresource.dataset.table.PutApi;
import com.welab.wefe.union.service.service.contract.TableDataSetContractService;
import com.welab.wefe.union.service.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TableDataSetService extends AbstractDataResourceService {
    @Autowired
    protected TableDataSetContractService tableDataSetContractService;
    @Autowired
    protected TableDataSetMongoReop tableDataSetMongoReop;

    public void add(PutApi.Input input) throws StatusCodeWithException {
        TableDataSet tableDataSet = tableDataSetMongoReop.findByDataResourceId(input.getDataResourceId());
        DataResource dataResource = dataResourceMongoReop.find(input.getDataResourceId(), input.curMemberId);
        if (dataResource == null) {
            if (tableDataSet == null) {
                tableDataSetContractService.add(transferPutInputToTableDataSet(input));
                dataResourceContractService.add(transferPutInputToDataResource(input));
            } else {
                dataResourceContractService.add(transferPutInputToDataResource(input));
            }
        } else {
            tableDataSet.setContainsY(input.isContainsY() ? "1" : "0");
            tableDataSet.setColumnCount(String.valueOf(input.getColumnCount()));
            tableDataSet.setColumnNameList(input.getColumnNameList());
            tableDataSet.setFeatureCount(String.valueOf(input.getFeatureCount()));
            tableDataSet.setFeatureNameList(input.getFeatureNameList());
            tableDataSetContractService.update(tableDataSet);

            updateDataResource(dataResource, input);
        }
    }

    private DataResource transferPutInputToDataResource(PutApi.Input input) {
        DataResource out = ModelMapper.map(input, DataResource.class);
        out.setMemberId(input.getCurMemberId());
        return out;
    }

    private TableDataSet transferPutInputToTableDataSet(PutApi.Input input) {
        TableDataSet out = ModelMapper.map(input, TableDataSet.class);
        out.setContainsY(String.valueOf(input.isContainsY() ? 1 : 0));
        out.setCreatedTime(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new java.util.Date()));
        out.setUpdatedTime(DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new java.util.Date()));
        return out;
    }
}
