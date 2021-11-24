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
import com.welab.wefe.common.data.mongodb.entity.union.DataSet;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetExtJSON;
import com.welab.wefe.common.data.mongodb.entity.union.ext.ImageDataSetExtJSON;
import com.welab.wefe.common.data.mongodb.repo.DataSetMongoReop;
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
        ImageDataSet dataSet = new ImageDataSet();
        dataSet.setDataSetId(StringUtil.strTrim2(params.getString(0)));
        dataSet.setMemberId(StringUtil.strTrim2(params.getString(1)));
        dataSet.setName(StringUtil.strTrim2(params.getString(2)));
        dataSet.setTags(StringUtil.strTrim2(params.getString(3)));
        dataSet.setDescription(StringUtil.strTrim2(params.getString(4)));
        dataSet.setStorageType(StringUtil.strTrim2(params.getString(5)));
        dataSet.setForJobType(params.getString(6));
        dataSet.setLabelList(StringUtil.strTrim2(params.getString(7)));
        dataSet.setSampleCount(StringUtil.strTrim2(params.getString(8)));
        dataSet.setLabeledCount(StringUtil.strTrim2(params.getString(9)));
        dataSet.setCompleted(StringUtil.strTrim2(params.getString(10)));
        dataSet.setFilesSize(StringUtil.strTrim2(params.getString(11)));
        dataSet.setPublicLevel(StringUtil.strTrim2(params.getString(12)));
        dataSet.setPublicMemberList(StringUtil.strTrim2(params.getString(13)));
        dataSet.setUsageCountInJob(StringUtil.strTrim2(params.getString(14)));
        dataSet.setUsageCountInFlow(StringUtil.strTrim2(params.getString(15)));
        dataSet.setUsageCountInProject(StringUtil.strTrim2(params.getString(16)));
        if(isAdd) {
            dataSet.setCreatedTime(StringUtil.strTrim2(params.getString(17)));
            dataSet.setUpdatedTime(StringUtil.strTrim2(params.getString(18)));
            dataSet.setExtJson(extJSON);
        } else {
            dataSet.setUpdatedTime(StringUtil.strTrim2(params.getString(17)));
        }

        dataSetMongoReop.upsert(dataSet);

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
        String completed = eventBO.getEntity().get("completed").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        dataSetMongoReop.updateLabeledCount(dataSetId, labeledCount, completed, updatedTime);
    }

    private void parseUpdateExtJson() {
        String id = eventBO.getEntity().get("id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        dataSetMongoReop.updateExtJSONById(id, extJSON, updatedTime);
    }

}
