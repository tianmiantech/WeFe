package com.welab.wefe.manager.service.dto.agreement;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/28
 */
public class UploadAuthAgreementTemplateInput extends AbstractWithFilesApiInput {
    @Check(require = true)
    private String filename;
    @Check(require = true)
    private Double version;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }
}
