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
import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * MemberContract Event information interpreter
 *
 * @author yuxin.zhang
 */
public class MemberContractEventParser extends AbstractParser {

    protected MemberMongoReop memberMongoReop = BlockchainDataSyncApp.CONTEXT.getBean(MemberMongoReop.class);
    protected MemberExtJSON extJSON;

    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, MemberExtJSON.class) : new MemberExtJSON();

        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.MemberEvent.INSERT_EVENT:
            case EventConstant.MemberEvent.UPDATE_EVENT:
                parseInsertAndUpdateEvent();
                break;
            case EventConstant.MemberEvent.UPDATE_EXCLUDE_PUBLICKEY_EVENT:
                parseUpdateExcludePublicKeyEvent();
                break;
            case EventConstant.MemberEvent.UPDATE_PUBLICKEY_EVENT:
                parseUpdatePublicKeyEvent();
                break;
            case EventConstant.MemberEvent.DELETE_BY_ID_EVENT:
                parseDeleteByIdEvent();
                break;
            case EventConstant.MemberEvent.UPDATE_EXCLUDE_LOGO_EVENT:
                parseUpdateExcludeLogoEvent();
                break;
            case EventConstant.MemberEvent.UPDATE_LOGO_BY_ID_EVENT:
                parseUpdateLogoByIdEvent();
                break;
            case EventConstant.MemberEvent.UPDATE_LAST_ACTIVITY_TIME_BY_ID_EVENT:
                parserUpdateLastActivityTimeByIdEvent();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            default:
                throw new BusinessException("contract name:" + eventBO.getContractName() + ",event name valid:" + eventBO.getEventName());
        }

    }

    private void parseInsertAndUpdateEvent() {
        Member member = new Member();
        member.setMemberId(StringUtil.strTrim2(params.getString(0)));
        member.setName(StringUtil.strTrim2(params.getString(1)));
        member.setMobile(StringUtil.strTrim2(params.getString(2)));
        member.setAllowOpenDataSet(StringUtil.strTrim2(params.getString(3)));
        member.setHidden(StringUtil.strTrim2(params.getString(4)));
        member.setFreezed(StringUtil.strTrim2(params.getString(5)));
        member.setLostContact(StringUtil.strTrim2(params.getString(6)));
        member.setPublicKey(StringUtil.strTrim2(params.getString(7)));
        member.setEmail(StringUtil.strTrim2(params.getString(8)));
        member.setGatewayUri(StringUtil.strTrim2(params.getString(9)));
        member.setLogo(StringUtil.strTrim2(params.getString(10)));
        member.setCreatedTime(StringUtil.strTrim2(params.getString(11)));
        member.setUpdatedTime(StringUtil.strTrim2(params.getString(12)));
        member.setLastActivityTime(StringUtil.strTrim2(params.getString(13)));

        member.setExtJson(extJSON);
        member.setDataSyncTime(System.currentTimeMillis());

        long startParser = System.currentTimeMillis();
        memberMongoReop.upsert(member);
        long endParser = System.currentTimeMillis();
        log.error("saveMember spend:" + (endParser - startParser) + "ms");
    }

    private void parseUpdateExcludePublicKeyEvent() {
        Member member = new Member();
        member.setMemberId(StringUtil.strTrim2(params.getString(0)));
        member.setName(StringUtil.strTrim2(params.getString(1)));
        member.setMobile(StringUtil.strTrim2(params.getString(2)));
        member.setAllowOpenDataSet(StringUtil.strTrim2(params.getString(3)));
        member.setHidden(StringUtil.strTrim2(params.getString(4)));
        member.setFreezed(StringUtil.strTrim2(params.getString(5)));
        member.setLostContact(StringUtil.strTrim2(params.getString(6)));
        member.setEmail(StringUtil.strTrim2(params.getString(7)));
        member.setGatewayUri(StringUtil.strTrim2(params.getString(8)));
        member.setLogo(StringUtil.strTrim2(params.getString(9)));
        member.setCreatedTime(StringUtil.strTrim2(params.getString(10)));
        member.setUpdatedTime(StringUtil.strTrim2(params.getString(11)));
        member.setLastActivityTime(StringUtil.strTrim2(params.getString(12)));
        member.setDataSyncTime(System.currentTimeMillis());
        member.setExtJson(extJSON);

        long start = System.currentTimeMillis();
        memberMongoReop.updateExcludePublicKey(member);

        long endParser = System.currentTimeMillis();
        log.error("parseUpdateExcludePublicKeyEvent spend:" + (endParser - start) + "ms");

    }

    private void parseUpdatePublicKeyEvent() {
        String id = eventBO.getEntity().get("id").toString();
        String publicKey = eventBO.getEntity().get("public_key").toString();
        memberMongoReop.updatePulicKeyById(publicKey, id);
    }

    private void parseDeleteByIdEvent() {
        String id = eventBO.getEntity().get("id").toString();
        memberMongoReop.deleteMemberById(id);
    }

    private void parseUpdateExcludeLogoEvent() {
        Member member = new Member();
        member.setMemberId(StringUtil.strTrim2(params.getString(0)));
        member.setName(StringUtil.strTrim2(params.getString(1)));
        member.setMobile(StringUtil.strTrim2(params.getString(2)));
        member.setAllowOpenDataSet(StringUtil.strTrim2(params.getString(3)));
        member.setHidden(StringUtil.strTrim2(params.getString(4)));
        member.setFreezed(StringUtil.strTrim2(params.getString(5)));
        member.setLostContact(StringUtil.strTrim2(params.getString(6)));
        member.setPublicKey(StringUtil.strTrim2(params.getString(7)));
        member.setEmail(StringUtil.strTrim2(params.getString(8)));
        member.setGatewayUri(StringUtil.strTrim2(params.getString(9)));
        member.setUpdatedTime(StringUtil.strTrim2(params.getString(10)));
        member.setLastActivityTime(StringUtil.strTrim2(params.getString(11)));
        member.setDataSyncTime(System.currentTimeMillis());
        member.setExtJson(extJSON);

        memberMongoReop.updateExcludeLogo(member);

    }

    private void parseUpdateLogoByIdEvent() {
        String id = eventBO.getEntity().get("id").toString();
        String logo = eventBO.getEntity().get("logo").toString();

        memberMongoReop.updateLogoById(logo, id);
    }

    private void parserUpdateLastActivityTimeByIdEvent() {
        String id = eventBO.getEntity().get("id").toString();
        String lastActivityTime = eventBO.getEntity().get("last_activity_time").toString();
        memberMongoReop.updateLastActivityTimeById(lastActivityTime, id);
    }

    private void parseUpdateExtJson() {
        String id = eventBO.getEntity().get("id").toString();
        memberMongoReop.updateExtJSONById(id,extJSON);
    }
}
