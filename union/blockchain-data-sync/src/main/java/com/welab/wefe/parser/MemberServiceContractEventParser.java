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
import com.welab.wefe.common.data.mongodb.entity.union.MemberService;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberServiceExtJSON;
import com.welab.wefe.common.data.mongodb.repo.MemberServiceMongoReop;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * MemberServiceContract Event information interpreter
 *
 * @author yuxin.zhang
 */
public class MemberServiceContractEventParser extends AbstractParser {

    protected MemberServiceMongoReop memberServiceMongoReop = App.CONTEXT.getBean(MemberServiceMongoReop.class);
    protected MemberServiceExtJSON extJSON;

    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, MemberServiceExtJSON.class) : new MemberServiceExtJSON();

        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.MemberService.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.MemberService.UPDATE_EVENT:
                parseUpdateEvent();
                break;
            case EventConstant.MemberService.UPDATE_SERVICE_STATUS_EVENT:
                parseUpdateServiceStatusEvent();
                break;
            case EventConstant.MemberService.DELETE_BY_SERVICE_ID_EVENT:
                parseDeleteByServiceIdEvent();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            default:
                throw new BusinessException("contract name:" + eventBO.getContractName() + ",event name valid:" + eventBO.getEventName());
        }

    }

    private void parseInsertEvent() {
        MemberService memberService = new MemberService();
        memberService.setServiceId(StringUtil.strTrim2(params.getString(0)));
        memberService.setMemberId(StringUtil.strTrim2(params.getString(1)));
        memberService.setName(StringUtil.strTrim2(params.getString(2)));
        memberService.setBaseUrl(StringUtil.strTrim2(params.getString(3)));
        memberService.setApiName(StringUtil.strTrim2(params.getString(4)));
        memberService.setServiceType(StringUtil.strTrim2(params.getString(5)));
        memberService.setQueryParams(StringUtil.strTrim2(params.getString(6)));
        memberService.setServiceStatus(StringUtil.strTrim2(params.getString(7)));
        memberService.setCreatedTime(StringUtil.strTrim2(params.getString(8)));
        memberService.setUpdatedTime(StringUtil.strTrim2(params.getString(9)));
        memberService.setExtJson(extJSON);
        memberService.setDataSyncTime(System.currentTimeMillis());

        memberServiceMongoReop.upsert(memberService);
    }

    private void parseUpdateEvent() {
        String serviceId = eventBO.getEntity().get("service_id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        MemberService memberService = memberServiceMongoReop.findByServiceId(serviceId);
        memberService.setName(StringUtil.strTrim2(params.getString(0)));
        memberService.setBaseUrl(StringUtil.strTrim2(params.getString(1)));
        memberService.setApiName(StringUtil.strTrim2(params.getString(2)));
        memberService.setServiceType(StringUtil.strTrim2(params.getString(3)));
        memberService.setQueryParams(StringUtil.strTrim2(params.getString(4)));
        memberService.setUpdatedTime(updatedTime);
        memberServiceMongoReop.upsert(memberService);
    }


    private void parseUpdateServiceStatusEvent() {
        String serviceId = eventBO.getEntity().get("service_id").toString();
        String serviceStatus = eventBO.getEntity().get("service_status").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        MemberService memberService = memberServiceMongoReop.findByServiceId(serviceId);
        memberService.setServiceStatus(serviceStatus);
        memberService.setUpdatedTime(updatedTime);
        memberServiceMongoReop.upsert(memberService);
    }

    private void parseDeleteByServiceIdEvent() {
        String serviceId = eventBO.getEntity().get("service_id").toString();
        memberServiceMongoReop.deleteMemberServiceById(serviceId);
    }

    private void parseUpdateExtJson() {
        String serviceId = eventBO.getEntity().get("service_id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        memberServiceMongoReop.updateExtJSONById(serviceId, updatedTime, extJSON);
    }

}
