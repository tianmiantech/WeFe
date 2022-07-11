/**
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.cert.mgr.enums;

/**
 * ExceptionCodeEnums
 *
 * @author graysonzhang
 *
 */
public enum MgrExceptionCodeEnums {

    PKEY_MGR_CERT_KEY_ALG_NOT_EXIST(1013, "key algorithms are not supported"),
    PKEY_MGR_ACCOUNT_NOT_EXIST(1006, "user account does not exist"),
    PKEY_MGR_CERT_REQUEST_NOT_EXIST(1014, "certificate request not exists"),
    PKEY_MGR_CERT_NOT_EXIST(1015, "certificate not exists"),
    PKEY_MGR_CERT_KEY_NOT_EXIST(1016, "certificate private key not exists"),
    PKEY_MGR_CERT_VALIDITY_FAILURE(1017, "the current date is out of the validity of the certificate"),
    PKEY_MGR_KEY_ADDRESS_NOT_FOUND(1018, "key address not found");

    private MgrExceptionCodeEnums(int exceptionCode, String exceptionMessage) {
        this.exceptionCode = exceptionCode;
        this.exceptionMessage = exceptionMessage;
    }

    private int exceptionCode;

    private String exceptionMessage;

    public int getExceptionCode() {
        return exceptionCode;
    }

    public void setExceptionCode(int exceptionCode) {
        this.exceptionCode = exceptionCode;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

}
