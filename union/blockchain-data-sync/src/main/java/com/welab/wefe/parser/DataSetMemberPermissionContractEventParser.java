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
import com.welab.wefe.common.data.mongodb.entity.union.DataSetMemberPermission;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetMemberPermissionExtJSON;
import com.welab.wefe.common.data.mongodb.repo.DataSetMemberPermissionMongoRepo;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * DataSetMemberPermission Event information interpreter
 *
 * @author yuxin.zhang
 */
public class DataSetMemberPermissionContractEventParser extends AbstractParser {
    protected DataSetMemberPermissionMongoRepo dataSetMemberPermissionMongoRepo = BlockchainDataSyncApp.CONTEXT.getBean(DataSetMemberPermissionMongoRepo.class);
    protected DataSetMemberPermissionExtJSON extJSON;

    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, DataSetMemberPermissionExtJSON.class) : new DataSetMemberPermissionExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.DataSetMemberPermission.INSERT_EVENT:
            case EventConstant.DataSetMemberPermission.UPDATE_EVENT:
                parseInsertAndUpdateEvent();
                break;
            case EventConstant.DataSetMemberPermission.DELETE_BY_DATASETID_EVENT:
                parseDeleteByDataSetIdEvent();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            default:
                throw new BusinessException("contract name:" + eventBO.getContractName() + ",event name valid:" + eventBO.getEventName());
        }

    }

    private void parseInsertAndUpdateEvent() {
        Map<String, Object> data = eventBO.getEntity();
        DataSetMemberPermission dataSetMemberPermission = new DataSetMemberPermission();
        dataSetMemberPermission.setDataSetMemberPermissionId(data.get("id").toString());
        dataSetMemberPermission.setDataSetId(data.get("data_set_id").toString());
        dataSetMemberPermission.setMemberId(data.get("member_id").toString());
        dataSetMemberPermission.setCreatedTime(data.get("created_time").toString());
        dataSetMemberPermission.setUpdatedTime(data.get("updated_time").toString());
        dataSetMemberPermission.setExtJson(extJSON);

        dataSetMemberPermissionMongoRepo.upsert(dataSetMemberPermission);
    }

    private void parseDeleteByDataSetIdEvent() {
        String dataSetId = eventBO.getEntity().get("data_set_id").toString();
        dataSetMemberPermissionMongoRepo.deleteByDataSetId(dataSetId);
    }

    private void parseUpdateExtJson() {
        String dataSetId = eventBO.getEntity().get("data_set_id").toString();
        dataSetMemberPermissionMongoRepo.updateExtJSONById(dataSetId,extJSON);
    }
}
