/**
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

package com.welab.wefe.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author aaron.li
 **/
public enum JavaTypeEnum {

    /**
     * type of data
     */
    BIGINTEGER("BigInteger", "Long", "bigint", "BigIntegerUtils.toLong"),
    Long("long", "Long", "bigint", "BigIntegerUtils.toLong"),
    BOOL("Boolean", "String", "varchar(8)", "String.valueOf"),
    STRING("String", "String", "varchar(4096)", "String.valueOf"),
    ByteArray("byte[]", "String", "text", "String.valueOf"),
    LISTByteArray("List<byte[]>", "String", "text", "String.valueOf"),
    LISTString("List<String>", "String", "text", "String.valueOf"),
    LISTBigInteger("List<BigInteger>", "String", "varchar(4096)", "String.valueOf"),
    LIST("List", "String", "text", "JacksonUtils.toJson");

    private String javaType;
    private String entityType;
    private String sqlType;
    private String typeMethod;

    JavaTypeEnum(String javaType, String entityType, String sqlType, String typeMethod) {
        this.javaType = javaType;
        this.entityType = entityType;
        this.sqlType = sqlType;
        this.typeMethod = typeMethod;
    }

    public static JavaTypeEnum parse(String javaType) {
        for (JavaTypeEnum type : JavaTypeEnum.values()) {
            if ((type.getJavaType().equalsIgnoreCase(StringUtils.substringBefore(javaType, "<"))
                    && !javaType.contains(">")) || type.getJavaType().equalsIgnoreCase(javaType)) {
                return type;
            }
        }
        return null;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getTypeMethod() {
        return typeMethod;
    }

    public void setTypeMethod(String typeMethod) {
        this.typeMethod = typeMethod;
    }
}
