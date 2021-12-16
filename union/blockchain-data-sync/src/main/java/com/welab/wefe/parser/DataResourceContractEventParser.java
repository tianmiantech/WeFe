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

package com.welab.wefe.parser;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.BlockchainDataSyncApp;
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataResourceExtJSON;
import com.welab.wefe.common.data.mongodb.repo.DataResourceMongoReop;
import com.welab.wefe.common.enums.DataResourceType;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * ImageDataSetContract Event information interpreter
 *
 * @author yuxin.zhang
 */
public class DataResourceContractEventParser extends AbstractParser {
    protected DataResourceMongoReop dataResourceMongoReop = BlockchainDataSyncApp.CONTEXT.getBean(DataResourceMongoReop.class);
    protected DataResourceExtJSON extJSON;


    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, DataResourceExtJSON.class) : new DataResourceExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.DataResourceEvent.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.DataResourceEvent.UPDATE_EVENT:
                parseUpdateEvent();
                break;
            case EventConstant.DataResourceEvent.UPDATE_ENABLE_EVENT:
                parseUpdateEnable();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            default:
                throw new BusinessException("event name valid:" + eventBO.getEventName());
        }
    }

    private void parseInsertEvent() {
        DataResource dataResource = new DataResource();
        dataResource.setDataResourceId(StringUtil.strTrim2(params.getString(0)));
        dataResource.setMemberId(StringUtil.strTrim2(params.getString(1)));
        dataResource.setName(StringUtil.strTrim2(params.getString(2)));
        dataResource.setDescription(StringUtil.strTrim2(params.getString(3)));
        dataResource.setTags(StringUtil.strTrim2(params.getString(4)));
        dataResource.setTotalDataCount(StringUtil.strTrim2(params.getString(5)));
        dataResource.setPublicLevel(StringUtil.strTrim2(params.getString(6)));
        dataResource.setPublicMemberList(StringUtil.strTrim2(params.getString(7)));
        dataResource.setUsageCountInJob(StringUtil.strTrim2(params.getString(8)));
        dataResource.setUsageCountInFlow(StringUtil.strTrim2(params.getString(9)));
        dataResource.setUsageCountInProject(StringUtil.strTrim2(params.getString(10)));
        dataResource.setUsageCountInMember(StringUtil.strTrim2(params.getString(11)));
        dataResource.setDataResourceType(DataResourceType.valueOf(StringUtil.strTrim2(params.getString(12))));
        dataResource.setCreatedTime(StringUtil.strTrim2(params.getString(13)));
        dataResource.setUpdatedTime(StringUtil.strTrim2(params.getString(14)));
        dataResource.setEnable("1");
        dataResource.setExtJson(extJSON);
        dataResourceMongoReop.upsert(dataResource);
    }

    private void parseUpdateEvent() throws BusinessException {
        String dataResourceId = eventBO.getEntity().get("data_resource_id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();

        DataResource dataResource = getImageDataSet(dataResourceId);

        dataResource.setName(params.getString(0));
        dataResource.setDescription(StringUtil.strTrim2(params.getString(1)));
        dataResource.setTags(StringUtil.strTrim2(params.getString(2)));
        dataResource.setTotalDataCount(StringUtil.strTrim2(params.getString(3)));
        dataResource.setPublicLevel(StringUtil.strTrim2(params.getString(4)));
        dataResource.setPublicMemberList(StringUtil.strTrim2(params.getString(5)));
        dataResource.setUsageCountInJob(StringUtil.strTrim2(params.getString(6)));
        dataResource.setUsageCountInFlow(StringUtil.strTrim2(params.getString(7)));
        dataResource.setUsageCountInProject(StringUtil.strTrim2(params.getString(8)));
        dataResource.setUsageCountInMember(StringUtil.strTrim2(params.getString(9)));
        dataResource.setUpdatedTime(updatedTime);

        dataResourceMongoReop.upsert(dataResource);
    }


    private void parseUpdateExtJson() throws BusinessException {
        String dataResourceId = eventBO.getEntity().get("data_resource_id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        DataResource dataResource = getImageDataSet(dataResourceId);
        dataResource.setExtJson(extJSON);
        dataResource.setUpdatedTime(updatedTime);
        dataResourceMongoReop.upsert(dataResource);
    }

    private void parseUpdateEnable() throws BusinessException {
        String dataResourceId = eventBO.getEntity().get("data_resource_id").toString();
        String enable = eventBO.getEntity().get("enable").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        DataResource dataResource = getImageDataSet(dataResourceId);
        dataResource.setEnable(enable);
        dataResource.setUpdatedTime(updatedTime);
        dataResourceMongoReop.upsert(dataResource);
    }


    private DataResource getImageDataSet(String dataResourceId) throws BusinessException {
        DataResource dataResource = dataResourceMongoReop.findByDataResourceId(dataResourceId);
        if (dataResource == null) {
            throw new BusinessException("Data does not exist dataResourceId:" + dataResourceId);
        }
        return dataResource;
    }

}
