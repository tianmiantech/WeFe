package com.webank.cert.mgr.model;

import com.webank.cert.mgr.exception.CertMgrException;

/**
 * @author aaronchu
 */
public class CommonResponse<TBody> {

    private static final int SUCCESS_CODE = 0;

    private int code;

    private String message;

    private TBody data;

    public CommonResponse(int code, String message, TBody data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse success(T data) {
        return new CommonResponse(SUCCESS_CODE, "", data);
    }

    public static CommonResponse fail(Exception error) {
        if (error instanceof CertMgrException) {
            CertMgrException certException = (CertMgrException) error;
            return new CommonResponse(certException.getCodeMessageEnums().getExceptionCode(),
                    certException.getCodeMessageEnums().getExceptionMessage(), null);
        }
        return new CommonResponse(-1, error.getMessage(), null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TBody getData() {
        return data;
    }

    public void setData(TBody data) {
        this.data = data;
    }

}
