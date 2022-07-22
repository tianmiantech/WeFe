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
package com.webank.cert.mgr.model.vo;

import java.io.Serializable;

/**
 * @author wesleywang
 */
public class CertKeyVO implements Serializable {

    private static final long serialVersionUID = 3902274893320956610L;

    private String pkId;

    private String userId;

    private String keyAlg;

    private String keyPem;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
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

    public String getKeyPem() {
        return keyPem;
    }

    public void setKeyPem(String keyPem) {
        this.keyPem = keyPem;
    }

    @Override
    public String toString() {
        return "CertKeyVO [pkId=" + pkId + ", userId=" + userId + ", keyAlg=" + keyAlg + ", keyPem=" + keyPem + "]";
    }

}
