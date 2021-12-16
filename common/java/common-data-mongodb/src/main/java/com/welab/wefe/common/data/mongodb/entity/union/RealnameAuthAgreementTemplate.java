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
import com.welab.wefe.common.data.mongodb.entity.base.AbstractBlockChainBusinessModel;
import com.welab.wefe.common.data.mongodb.entity.union.ext.RealnameAuthAgreementTemplateExtJSON;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author yuxin.zhang
 **/
@Document(collection = MongodbTable.Union.REALNAME_AUTH_AGREEMENT_TEMPLATE)
public class RealnameAuthAgreementTemplate extends AbstractBlockChainBusinessModel {
    private String templateFileId;
    private String templateFileSign;
    private String fileName;
    private String enable;
    private RealnameAuthAgreementTemplateExtJSON extJson = new RealnameAuthAgreementTemplateExtJSON();

    public String getTemplateFileId() {
        return templateFileId;
    }

    public void setTemplateFileId(String templateFileId) {
        this.templateFileId = templateFileId;
    }

    public String getTemplateFileSign() {
        return templateFileSign;
    }

    public void setTemplateFileSign(String templateFileSign) {
        this.templateFileSign = templateFileSign;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public RealnameAuthAgreementTemplateExtJSON getExtJson() {
        return extJson;
    }

    public void setExtJson(RealnameAuthAgreementTemplateExtJSON extJson) {
        this.extJson = extJson;
    }
}
