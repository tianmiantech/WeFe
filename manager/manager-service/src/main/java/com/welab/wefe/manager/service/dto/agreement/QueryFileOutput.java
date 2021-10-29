package com.welab.wefe.manager.service.dto.agreement;

import com.welab.wefe.common.web.dto.AbstractApiOutput;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/28
 */
public class QueryFileOutput extends AbstractApiOutput {
    private String fileName;
    private String fileType;
    private byte[] fileData;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
