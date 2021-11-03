package com.welab.wefe.manager.service.dto.agreement;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/28
 */
public class QueryFileInput extends AbstractApiInput {
    @Check(require = true)
    private String fileId;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
