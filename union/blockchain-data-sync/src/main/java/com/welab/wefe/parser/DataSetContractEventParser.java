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

package com.welab.wefe.parser;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.BlockchainDataSyncApp;
import com.welab.wefe.common.data.mongodb.entity.union.DataSet;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetExtJSON;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * DataSetContract Event information interpreter
 *
 * @author yuxin.zhang
 */
public class DataSetContractEventParser extends AbstractParser {
    protected DataSetMongoReop dataSetMongoReop = BlockchainDataSyncApp.CONTEXT.getBean(DataSetMongoReop.class);
    protected DataSetExtJSON extJSON;


    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, DataSetExtJSON.class) : new DataSetExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.DataSetEvent.INSERT_EVENT:
            case EventConstant.DataSetEvent.UPDATE_EVENT:
                parseInsertAndUpdateEvent();
                break;
            case EventConstant.DataSetEvent.DELETE_BY_DATASETID_EVENT:
                parseDeleteByDataSetIdEvent();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            default:
                throw new BusinessException("event name valid:" + eventBO.getEventName());
        }
    }

    private void parseInsertAndUpdateEvent() {
        DataSet dataSet = new DataSet();
        dataSet.setDataSetId(StringUtil.strTrim2(params.getString(0)));
        dataSet.setMemberId(StringUtil.strTrim2(params.getString(1)));
        dataSet.setName(StringUtil.strTrim2(params.getString(2)));
        dataSet.setContainsY(StringUtil.strTrim2(params.getString(3)));
        dataSet.setRowCount(StringUtil.strTrim2(params.getString(4)));
        dataSet.setColumnCount(StringUtil.strTrim2(params.getString(5)));
        dataSet.setColumnNameList(params.getString(6));
        dataSet.setFeatureCount(StringUtil.strTrim2(params.getString(7)));
        dataSet.setFeatureNameList(StringUtil.strTrim2(params.getString(8)));
        dataSet.setPublicLevel(StringUtil.strTrim2(params.getString(9)));
        dataSet.setPublicMemberList(StringUtil.strTrim2(params.getString(10)));
        dataSet.setUsageCountInJob(StringUtil.strTrim2(params.getString(11)));
        dataSet.setUsageCountInFlow(StringUtil.strTrim2(params.getString(12)));
        dataSet.setUsageCountInProject(StringUtil.strTrim2(params.getString(13)));
        dataSet.setDescription(StringUtil.strTrim2(params.getString(14)));
        dataSet.setTags(StringUtil.strTrim2(params.getString(15)));
        dataSet.setCreatedTime(StringUtil.strTrim2(params.getString(16)));
        dataSet.setUpdatedTime(StringUtil.strTrim2(params.getString(17)));
        dataSet.setExtJson(extJSON);

        dataSetMongoReop.upsert(dataSet);

    }

    private void parseDeleteByDataSetIdEvent() {
        String id = eventBO.getEntity().get("id").toString();
        dataSetMongoReop.deleteByDataSetId(id);
    }


    private void parseUpdateExtJson() {
        String id = eventBO.getEntity().get("id").toString();
        dataSetMongoReop.updateExtJSONById(id,extJSON);
    }

}
