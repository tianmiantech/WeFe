package com.welab.wefe.union.service.constant;

public enum CertStatusEnums {

    // -1认证失败 /0未认证 /1认证中 /2已认证
    INVALID(0, "无效"), WAIT_VERIFY(1, "认证中"), VALID(2, "有效");

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
