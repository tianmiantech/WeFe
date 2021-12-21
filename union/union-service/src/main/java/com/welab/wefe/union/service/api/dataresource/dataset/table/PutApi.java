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

package com.welab.wefe.union.service.api.dataresource.dataset.table;

import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.entity.union.TableDataSet;
import com.welab.wefe.common.data.mongodb.repo.TableDataSetMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.api.dataresource.dataset.AbstractDatResourcePutApi;
import com.welab.wefe.union.service.dto.dataresource.DataResourcePutInput;
import com.welab.wefe.union.service.mapper.TableDataSetMapper;
import com.welab.wefe.union.service.service.TableDataSetContractService;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "table_data_set/put", name = "table_data_set", rsaVerify = true, login = false)
public class PutApi extends AbstractDatResourcePutApi<PutApi.Input, AbstractApiOutput> {
    @Autowired
    protected TableDataSetContractService tableDataSetContractService;
    protected TableDataSetMongoReop tableDataSetMongoReop;

    protected TableDataSetMapper tableDataSetMapper = Mappers.getMapper(TableDataSetMapper.class);

    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException {
        TableDataSet tableDataSet = tableDataSetMongoReop.findByDataResourceId(input.getDataResourceId());
        DataResource dataResource = dataResourceMongoReop.find(input.getDataResourceId(), input.getCurMemberId());
        if (dataResource == null) {
            if (tableDataSet == null) {
                tableDataSetContractService.add(tableDataSetMapper.transferPutInputToTableDataSet(input));
                dataResourceContractService.add(tableDataSetMapper.transferPutInputToDataResource(input));
            } else {
                dataResourceContractService.add(tableDataSetMapper.transferPutInputToDataResource(input));
            }
        } else {
            tableDataSet.setContainsY(input.containsY ? "1" : "0");
            tableDataSet.setColumnCount(String.valueOf(input.columnCount));
            tableDataSet.setColumnNameList(input.columnNameList);
            tableDataSet.setFeatureCount(String.valueOf(input.featureCount));
            tableDataSet.setFeatureNameList(input.featureNameList);
            tableDataSetContractService.update(tableDataSet);

            updateDataResource(dataResource,input);
        }

        return success();
    }

    public static class Input extends DataResourcePutInput {
        @Check(require = true)
        private boolean containsY;
        private int columnCount;
        private String columnNameList;
        private int featureCount;
        private String featureNameList;

        public boolean isContainsY() {
            return containsY;
        }

        public void setContainsY(boolean containsY) {
            this.containsY = containsY;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }

        public String getColumnNameList() {
            return columnNameList;
        }

        public void setColumnNameList(String columnNameList) {
            this.columnNameList = columnNameList;
        }

        public int getFeatureCount() {
            return featureCount;
        }

        public void setFeatureCount(int featureCount) {
            this.featureCount = featureCount;
        }

        public String getFeatureNameList() {
            return featureNameList;
        }

        public void setFeatureNameList(String featureNameList) {
            this.featureNameList = featureNameList;
        }
    }
}
