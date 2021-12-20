package com.welab.wefe.serving.service.enums;

/**
 * @author ivenn.zheng
 */

public enum ServiceStatusEnum {

    USED(1),

    UNUSED(0);

    private int value;

    ServiceStatusEnum(int value) {
        this.value = value;
    }
    public int getValue(){
        return value;
    }

}
