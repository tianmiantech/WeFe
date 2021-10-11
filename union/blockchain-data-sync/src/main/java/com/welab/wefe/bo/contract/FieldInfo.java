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

package com.welab.wefe.bo.contract;

/**
 * Field message object
 *
 * @author aaron.li
 **/
public class FieldInfo {
    private String sqlName;
    private String solidityName;
    private String javaName;
    private String sqlType;
    private String solidityType;
    private String javaType;
    private String javaCapName;
    private String typeMethod;

    public String getSqlName() {
        return sqlName;
    }

    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }

    public String getSolidityName() {
        return solidityName;
    }

    public void setSolidityName(String solidityName) {
        this.solidityName = solidityName;
    }

    public String getJavaName() {
        return javaName;
    }

    public void setJavaName(String javaName) {
        this.javaName = javaName;
    }

    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getSolidityType() {
        return solidityType;
    }

    public void setSolidityType(String solidityType) {
        this.solidityType = solidityType;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getJavaCapName() {
        return javaCapName;
    }

    public void setJavaCapName(String javaCapName) {
        this.javaCapName = javaCapName;
    }

    public String getTypeMethod() {
        return typeMethod;
    }

    public void setTypeMethod(String typeMethod) {
        this.typeMethod = typeMethod;
    }
}
