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

package com.welab.wefe.data.fusion.service.database.entity;

import com.welab.wefe.common.wefe.enums.VerificationCodeBusinessType;
import com.welab.wefe.common.wefe.enums.VerificationCodeSendChannel;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Verification code model
 *
 * @author aaron.li
 * @date 2022/1/19 17:45
 **/
@Entity(name = "verification_code")
public class VerificationCodeMysqlModel extends AbstractMySqlModel {
    /**
     * Business id, This field can be used to associate business information
     */
    private String bizId;
    /**
     * mobile
     */
    private String mobile;
    /**
     * Verification code
     */
    private String code;
    /**
     * Whether the verification code is sent successfully. true or false
     */
    private String success;
    /**
     * Verification code send channel
     */
    @Enumerated(EnumType.STRING)
    private VerificationCodeSendChannel sendChannel;
    /**
     * Verification code business type
     */
    @Enumerated(EnumType.STRING)
    private VerificationCodeBusinessType businessType;
    private String respContent;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public VerificationCodeSendChannel getSendChannel() {
        return sendChannel;
    }

    public void setSendChannel(VerificationCodeSendChannel sendChannel) {
        this.sendChannel = sendChannel;
    }

    public VerificationCodeBusinessType getBusinessType() {
        return businessType;
    }

    public void setBusinessType(VerificationCodeBusinessType businessType) {
        this.businessType = businessType;
    }

    public String getRespContent() {
        return respContent;
    }

    public void setRespContent(String respContent) {
        this.respContent = respContent;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }
}
