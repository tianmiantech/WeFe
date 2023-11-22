/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.fieldvalidate;

/**
 * Enumerations: types of standard fields
 *
 * @author hunter.zhao
 */
public enum StandardFieldType {
    /**
     * Chinese name
     */
    PersonalName("姓名"),
    CompanyName("单位名称"),
    Email("e-mail"),
    CNID("身份证号码"),
    PhoneNumber("手机号"),
    QQNumber("QQ 号"),
    NONE("非标准字段");

    private String desc;

    StandardFieldType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * Check whether the input string meets the requirements of the standard field
     */
    public boolean check(Object value) {
        if (value == null || value.toString().length() == 0) {
            return true;
        }
        switch (this) {
            case PersonalName:
                return FieldAssert.isPersonalName(value.toString());
            case PhoneNumber:
                return FieldAssert.isPhoneNumber(value.toString());
            case CNID:
                return FieldAssert.isCnid(value.toString());
            case Email:
                return FieldAssert.isEmail(value.toString());
            case CompanyName:
                return FieldAssert.isCompanyName(value.toString());
            case QQNumber:
                return FieldAssert.isQqNumber(value.toString());
            case NONE:
                return true;
            default:
                throw new RuntimeException("Standard field validation not implemented：" + this.getDesc());
        }

    }

    /**
     * Does this type of field need to be standardized
     */
    public boolean needStandardize() {
        switch (this) {
            case PhoneNumber:
            case Email:
            case CNID:
                return true;
            default:
                return false;
        }
    }

    /**
     * Standardize the field content
     */
    public Object standardize(Object value) {
        if (value == null) {
            return null;
        }
        if (!needStandardize()) {
            throw new RuntimeException("当前字段未声明为需要标准化处理");
        }

        switch (this) {
            case PhoneNumber:
                return cleanPhoneNumber(value.toString());
            case Email:
                // Mailbox normalized to lowercase
                return value.toString().toLowerCase();
            case CNID:
                // The ID card is standardized to uppercase
                return value.toString().toUpperCase();
            default:
                throw new RuntimeException("Field standardization not implemented：" + this.getDesc());
        }
    }

    private String cleanPhoneNumber(String str) {
        StringBuilder sbuilder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                sbuilder.append(str.charAt(i));
            }

        }
        return sbuilder.toString();
    }
}
