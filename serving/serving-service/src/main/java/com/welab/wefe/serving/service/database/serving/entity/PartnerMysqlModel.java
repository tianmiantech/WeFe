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

package com.welab.wefe.serving.service.database.serving.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.welab.wefe.serving.service.enums.ClientStatusEnum;

@Entity(name = "partner")
public class PartnerMysqlModel extends AbstractBaseMySqlModel {

    private static final long serialVersionUID = -2477812313658221499L;

    /**
     * name
     */
    private String name;
    /**
     * email
     */
    private String email;
    /**
     * partner_id
     */
    @Column(name = "partner_id")
    private String partnerId;
    /**
     * is_union_member
     */
    @Column(name = "is_union_member")
    private boolean isUnionMember;
    /**
     * serving_base_url
     */
    @Column(name = "serving_base_url")
    private String servingBaseUrl;
    /**
     * remark
     */
    private String remark;

    /**
     * status, 1 normal、 0 deleted
     */
    private Integer status = ClientStatusEnum.NORMAL.getValue();

    /**
     * partner code
     */
    private String code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public boolean isUnionMember() {
        return isUnionMember;
    }

    public void setUnionMember(boolean isUnionMember) {
        this.isUnionMember = isUnionMember;
    }

    public String getServingBaseUrl() {
        return servingBaseUrl;
    }

    public void setServingBaseUrl(String servingBaseUrl) {
        this.servingBaseUrl = servingBaseUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
