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

package com.welab.wefe.parser;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.BlockchainDataSyncApp;
import com.welab.wefe.common.data.mongodb.entity.union.TableDataSet;
import com.welab.wefe.common.data.mongodb.entity.union.ext.TableDataSetExtJSON;
import com.welab.wefe.common.data.mongodb.repo.TableDataSetMongoReop;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * TableDataSetContract Event information interpreter
 *
 * @author yuxin.zhang
 */
public class TableDataSetContractEventParser extends AbstractParser {
    protected TableDataSetMongoReop tableDataSetMongoReop = BlockchainDataSyncApp.CONTEXT.getBean(TableDataSetMongoReop.class);
    protected TableDataSetExtJSON extJSON;


    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, TableDataSetExtJSON.class) : new TableDataSetExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.TableDataSetEvent.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.TableDataSetEvent.UPDATE_EVENT:
                parseUpdateEvent();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            default:
                throw new BusinessException("event name valid:" + eventBO.getEventName());
        }
    }

    private void parseInsertEvent() {
        TableDataSet tableDataSet = new TableDataSet();
        tableDataSet.setDataResourceId(StringUtil.strTrim2(params.getString(0)));
        tableDataSet.setContainsY(StringUtil.strTrim2(params.getString(1)));
        tableDataSet.setColumnCount(StringUtil.strTrim2(params.getString(2)));
        tableDataSet.setColumnNameList(params.getString(3));
        tableDataSet.setFeatureCount(StringUtil.strTrim2(params.getString(4)));
        tableDataSet.setFeatureNameList(StringUtil.strTrim2(params.getString(5)));
        tableDataSet.setCreatedTime(StringUtil.strTrim2(params.getString(6)));
        tableDataSet.setUpdatedTime(StringUtil.strTrim2(params.getString(7)));
        tableDataSet.setExtJson(extJSON);
        tableDataSetMongoReop.upsert(tableDataSet);
    }

    private void parseUpdateEvent() throws BusinessException {
        String dataResourceId = eventBO.getEntity().get("data_resource_id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();

        TableDataSet tableDataSet = getTableDataSet(dataResourceId);

        tableDataSet.setContainsY(StringUtil.strTrim2(params.getString(0)));
        tableDataSet.setColumnCount(StringUtil.strTrim2(params.getString(1)));
        tableDataSet.setColumnNameList(params.getString(2));
        tableDataSet.setFeatureCount(StringUtil.strTrim2(params.getString(3)));
        tableDataSet.setFeatureNameList(StringUtil.strTrim2(params.getString(4)));

        tableDataSet.setUpdatedTime(updatedTime);
        tableDataSetMongoReop.upsert(tableDataSet);
    }



    private void parseUpdateExtJson() throws BusinessException {
        String dataResourceId = eventBO.getEntity().get("data_resource_id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        TableDataSet tableDataSet = getTableDataSet(dataResourceId);
        tableDataSet.setExtJson(extJSON);
        tableDataSet.setUpdatedTime(updatedTime);
        tableDataSetMongoReop.upsert(tableDataSet);
    }

    private TableDataSet getTableDataSet(String dataResourceId) throws BusinessException {
        TableDataSet tableDataSet = tableDataSetMongoReop.findByDataResourceId(dataResourceId);
        if(tableDataSet == null) {
            throw new BusinessException("Data does not exist dataResourceId:" + dataResourceId);
        }
        return tableDataSet;
    }

}
