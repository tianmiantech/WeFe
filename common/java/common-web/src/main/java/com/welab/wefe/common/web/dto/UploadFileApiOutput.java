package com.welab.wefe.common.web.dto;

public class UploadFileApiOutput {
    private String fileId;

    public UploadFileApiOutput(String fileId){
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
