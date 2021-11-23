package com.welab.wefe.manager.service.dto.common;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/5
 */
public class UploadFileInput extends AbstractWithFilesApiInput {
    @Check(require = true)
    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
