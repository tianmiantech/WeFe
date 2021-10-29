package com.welab.wefe.common.data.mongodb.entity.union.ext;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/29
 */
public class RealNameAuthFileInfo {
    private String fileId;
    private String fileMd5Sign;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileMd5Sign() {
        return fileMd5Sign;
    }

    public void setFileMd5Sign(String fileMd5Sign) {
        this.fileMd5Sign = fileMd5Sign;
    }
}
