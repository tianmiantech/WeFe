package com.webank.cert.mgr.exception;


import com.webank.cert.mgr.enums.MgrExceptionCodeEnums;

/**
 * @author wesleywang
 */
public class CertMgrException extends Exception {
    /** @Fields serialVersionUID : TODO */
    private static final long serialVersionUID = 893822168485972751L;
    private MgrExceptionCodeEnums ece;

    public CertMgrException(MgrExceptionCodeEnums ece) {
        super(ece.getExceptionMessage());
        this.ece = ece;
    }

    public CertMgrException(String msg) {
        super(msg);
        this.ece.setExceptionMessage(msg);
    }

    public MgrExceptionCodeEnums getCodeMessageEnums() {
        return ece;
    }
}
