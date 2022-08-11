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
import com.welab.wefe.common.data.mongodb.entity.union.TrustCerts;
import com.welab.wefe.common.data.mongodb.entity.union.ext.TrustCertsExtJSON;
import com.welab.wefe.common.data.mongodb.repo.TrustCertsMongoRepo;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yuxin.zhang
 */
public class TrustCertsContractEventParser extends AbstractParser {
    protected TrustCertsMongoRepo trustCertsMongoRepo = BlockchainDataSyncApp.CONTEXT.getBean(TrustCertsMongoRepo.class);
    protected TrustCertsExtJSON extJSON;

    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, TrustCertsExtJSON.class) : new TrustCertsExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.TrustCerts.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.TrustCerts.DELETE_BY_CERT_ID_EVENT:
                parseDeleteByTrustCertsIdEvent();
                break;
            default:
                throw new BusinessException("event name valid:" + eventBO.getEventName());
        }
    }

    private void parseInsertEvent() {
        TrustCerts trustCerts = new TrustCerts();
        trustCerts.setCertId(StringUtil.strTrim2(params.getString(0)));
        trustCerts.setMemberId(StringUtil.strTrim2(params.getString(1)));
        trustCerts.setSerialNumber(StringUtil.strTrim2(params.getString(2)));
        trustCerts.setCertContent(StringUtil.strTrim2(params.getString(3)));
        trustCerts.setpCertId(StringUtil.strTrim2(params.getString(4)));
        trustCerts.setIssuerOrg(StringUtil.strTrim2(params.getString(5)));
        trustCerts.setIssuerCn(StringUtil.strTrim2(params.getString(6)));
        trustCerts.setSubjectOrg(StringUtil.strTrim2(params.getString(7)));
        trustCerts.setSubjectCn(StringUtil.strTrim2(params.getString(8)));
        trustCerts.setIsCaCert(StringUtil.strTrim2(params.getString(9)));
        trustCerts.setIsRootCert(StringUtil.strTrim2(params.getString(10)));
        trustCerts.setCreatedTime(StringUtil.strTrim2(params.getString(11)));
        trustCerts.setUpdatedTime(StringUtil.strTrim2(params.getString(12)));
        trustCerts.setExtJson(extJSON);

        trustCertsMongoRepo.save(trustCerts);

    }

    private void parseDeleteByTrustCertsIdEvent() {
        String certId = eventBO.getEntity().get("cert_id").toString();
        trustCertsMongoRepo.deleteByTrustCertsId(certId);
    }


}
