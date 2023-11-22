package com.welab.wefe.data.fusion.service.utils;

import java.util.Arrays;
import java.util.List;

public class FileSecurityChecker {
    /**
     * 允许的文件类型
     */
    private static final List<String> ALLOW_FILE_TYPES = Arrays.asList(
            "xls", "xlsx", "csv"
    );
    /**
     * Check File Type
     *
     * @param contentType
     */
    public static boolean isValid(String contentType) {
        if (null == contentType || "".equals(contentType)) {
            return false;
        }
        for (String type : ALLOW_FILE_TYPES) {
            if (contentType.indexOf(type) > -1) {
                return true;
            }
        }
        return false;
    }
}
