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
import com.welab.wefe.App;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.entity.union.ext.ImageDataSetExtJSON;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetMongoReop;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * ImageDataSetContract Event information interpreter
 *
 * @author yuxin.zhang
 */
public class ImageDataSetContractEventParser extends AbstractParser {
    protected ImageDataSetMongoReop dataSetMongoReop = App.CONTEXT.getBean(ImageDataSetMongoReop.class);
    protected ImageDataSetExtJSON extJSON;


    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, ImageDataSetExtJSON.class) : new ImageDataSetExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.ImageDataSet.INSERT_EVENT:
                parseInsertAndUpdateEvent(true);
                break;
            case EventConstant.ImageDataSet.UPDATE_EVENT:
                parseInsertAndUpdateEvent(false);
                break;
            case EventConstant.ImageDataSet.DELETE_BY_DATASETID_EVENT:
                parseDeleteByDataSetIdEvent();
                break;
            case EventConstant.ImageDataSet.UPDATE_LABELED_COUNT:
                parseUpdateLabeledCountEvent();
                break;
            case EventConstant.ImageDataSet.UPDATE_ENABLE_EVENT:
                parseUpdateEnableEvent();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            default:
                throw new BusinessException("event name valid:" + eventBO.getEventName());
        }
    }

    private void parseInsertAndUpdateEvent(boolean isAdd) {
        ImageDataSet imageDataSet = new ImageDataSet();
        imageDataSet.setDataSetId(StringUtil.strTrim2(params.getString(0)));
        imageDataSet.setMemberId(StringUtil.strTrim2(params.getString(1)));
        imageDataSet.setName(StringUtil.strTrim2(params.getString(2)));
        imageDataSet.setTags(StringUtil.strTrim2(params.getString(3)));
        imageDataSet.setDescription(StringUtil.strTrim2(params.getString(4)));
        imageDataSet.setForJobType(params.getString(5));
        imageDataSet.setLabelList(StringUtil.strTrim2(params.getString(6)));
        imageDataSet.setSampleCount(StringUtil.strTrim2(params.getString(7)));
        imageDataSet.setLabeledCount(StringUtil.strTrim2(params.getString(8)));
        imageDataSet.setLabelCompleted(StringUtil.strTrim2(params.getString(9)));
        imageDataSet.setFilesSize(StringUtil.strTrim2(params.getString(10)));
        imageDataSet.setPublicLevel(StringUtil.strTrim2(params.getString(11)));
        imageDataSet.setPublicMemberList(StringUtil.strTrim2(params.getString(12)));
        imageDataSet.setUsageCountInJob(StringUtil.strTrim2(params.getString(13)));
        imageDataSet.setUsageCountInFlow(StringUtil.strTrim2(params.getString(14)));
        imageDataSet.setUsageCountInProject(StringUtil.strTrim2(params.getString(15)));
        if(isAdd) {
            imageDataSet.setEnable(StringUtil.strTrim2(params.getString(16)));
            imageDataSet.setCreatedTime(StringUtil.strTrim2(params.getString(17)));
            imageDataSet.setUpdatedTime(StringUtil.strTrim2(params.getString(18)));
            imageDataSet.setExtJson(extJSON);
        } else {
            imageDataSet.setUpdatedTime(StringUtil.strTrim2(params.getString(16)));
        }

        dataSetMongoReop.upsert(imageDataSet);

    }

    private void parseDeleteByDataSetIdEvent() {
        String id = eventBO.getEntity().get("id").toString();
        dataSetMongoReop.deleteByDataSetId(id);
    }


    private void parseUpdateEnableEvent() {
        String dataSetId = eventBO.getEntity().get("id").toString();
        String enable = eventBO.getEntity().get("enable").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        dataSetMongoReop.updateEnable(dataSetId, enable, updatedTime);
    }

    private void parseUpdateLabeledCountEvent() {
        String dataSetId = eventBO.getEntity().get("id").toString();
        String labeledCount = eventBO.getEntity().get("labeled_count").toString();
        String completed = eventBO.getEntity().get("labelCompleted").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        dataSetMongoReop.updateLabeledCount(dataSetId, labeledCount, completed, updatedTime);
    }

    private void parseUpdateExtJson() {
        String id = eventBO.getEntity().get("id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        dataSetMongoReop.updateExtJSONById(id, extJSON, updatedTime);
    }

}
