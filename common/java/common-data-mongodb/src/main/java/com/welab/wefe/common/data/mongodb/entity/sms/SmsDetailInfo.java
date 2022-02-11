/**
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

package com.welab.wefe.common.data.mongodb.entity.sms;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.constant.SmsBusinessType;
import com.welab.wefe.common.data.mongodb.constant.SmsSupplierEnum;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractNormalMongoModel;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author aaron.li
 * @date 2021/10/22 11:20
 */
@Document(collection = MongodbTable.Sms.DETAIL_INFO)
public class SmsDetailInfo extends AbstractNormalMongoModel {
    private String bizId;
    private String reqId;
    private String mobile;
    private String reqContent;
    private SmsSupplierEnum supplier;
    private boolean success;
    private String respContent;
    private SmsBusinessType businessType;

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getReqContent() {
        return reqContent;
    }

    public void setReqContent(String reqContent) {
        this.reqContent = reqContent;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public SmsSupplierEnum getSupplier() {
        return supplier;
    }

    public void setSupplier(SmsSupplierEnum supplier) {
        this.supplier = supplier;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRespContent() {
        return respContent;
    }

    public void setRespContent(String respContent) {
        this.respContent = respContent;
    }

    public SmsBusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(SmsBusinessType businessType) {
        this.businessType = businessType;
    }
}
