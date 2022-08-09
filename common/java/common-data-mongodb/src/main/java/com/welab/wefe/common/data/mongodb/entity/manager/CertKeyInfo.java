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

package com.welab.wefe.common.data.mongodb.entity.manager;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.mapping.Document;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractNormalMongoModel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;

/**
 * @author wesleywang
 */
@Document(collection = MongodbTable.CERT_KEY_INFO)
public class CertKeyInfo extends AbstractNormalMongoModel {

    private static final long serialVersionUID = -7731011364389900165L;

    // 主键ID
    private String pkId = UUID.randomUUID().toString().replaceAll("-", "");

    // 私钥pem格式内容
    private String keyPem;

    // 用户ID
    private String userId;

    // 私钥算法
    private String keyAlg;
    
    // 创建人
    private String createdBy;
    
    
    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public String getKeyPem() throws StatusCodeWithException {
        if (StringUtils.isNotBlank(keyPem) && keyPem.startsWith("-----BEGIN")) {
            keyPem = DatabaseEncryptUtil.decrypt(keyPem);
        }
        return keyPem;
    }

    public void setKeyPem(String keyPem) throws StatusCodeWithException {
        if (StringUtils.isNotBlank(keyPem) && !keyPem.startsWith("-----BEGIN")) {
            keyPem = DatabaseEncryptUtil.encrypt(keyPem);
        }
        this.keyPem = keyPem;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKeyAlg() {
        return keyAlg;
    }

    public void setKeyAlg(String keyAlg) {
        this.keyAlg = keyAlg;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
