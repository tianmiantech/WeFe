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
    private String fileType;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
