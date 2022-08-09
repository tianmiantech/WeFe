package com.webank.cert.mgr.enums;

public enum CertStatusEnums {

    INVALID(0, "无效"), VALID(1, "有效");

    private CertStatusEnums(int code, String name) {
        this.code = code;
        this.name = name;
    }

    private int code;
    private String name;

    public static CertStatusEnums getStatus(int code) {
        for (CertStatusEnums e : CertStatusEnums.values()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
