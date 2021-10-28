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

package com.welab.wefe.common.data.mongodb.entity.union;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractNormalMongoModel;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetDefaultTagExtJSON;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.Union.AUTH_AGREEMENT_TEMPLATE)
public class AuthAgreementTemplate extends AbstractNormalMongoModel {
    private String authAgreementFileId;
    private String authAgreementFileMd5;
    private String fileName;
    private Double version;


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAuthAgreementFileId() {
        return authAgreementFileId;
    }

    public void setAuthAgreementFileId(String authAgreementFileId) {
        this.authAgreementFileId = authAgreementFileId;
    }

    public String getAuthAgreementFileMd5() {
        return authAgreementFileMd5;
    }

    public void setAuthAgreementFileMd5(String authAgreementFileMd5) {
        this.authAgreementFileMd5 = authAgreementFileMd5;
    }

    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }
}
