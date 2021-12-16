package com.welab.wefe.serving.service.enums;

/**
 * @author ivenn.zheng
 */

public enum ClientStatusEnum {

    NORMAL(1),

    DELETED(0);

    private int value;

    ClientStatusEnum(int value) {
        this.value = value;
    }
    public int getValue(){
        return value;
    }

}
