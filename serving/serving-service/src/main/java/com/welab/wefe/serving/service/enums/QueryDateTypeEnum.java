/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.serving.service.enums;

/**
 * @author ivenn.zheng
 * @date 2021/12/23
 */
public enum QueryDateTypeEnum {


    YEAR(1),

    MONTH(2),

    DAY(3),

    HOUR(4);

    private int value;

    QueryDateTypeEnum(int value) {
        this.value = value;
    }
    public int getValue(){
        return value;
    }
}
