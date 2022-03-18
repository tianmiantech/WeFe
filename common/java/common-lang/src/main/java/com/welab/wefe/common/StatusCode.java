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

package com.welab.wefe.common;

import com.welab.wefe.common.exception.StatusCodeWithException;

/**
 * Status code table
 * <p> Code table distribution</p>
 * <ol>
 * <li> [less than 0] reserved interval, used to declare the status of system level non error type </li>
 * <li> [beginning with 1] system level error code </li>
 * <li> [beginning with 2] service level status code, with a length of 5 digits or more. The first three digits are the service number. There is no need to apply for the service number, which is increased from 201, and the one beginning with 200 is the general status code.</li>
 * </ol>
 *
 * @author Zane
 */
public enum StatusCode {

    /**
     *
     */
    SUCCESS(0, "success"),

    /**
     * System level error codes
     */
    SYSTEM_NOT_BEEN_INITIALIZED(10000, "系统尚未初始化，请先初始化"),
    SYSTEM_ERROR(10001, "系统错误"),
    REMOTE_SERVICE_ERROR(10003, "远程服务错误"),
    IP_LIMIT(10004, "IP被限制请求"),
    PERMISSION_DENIED(10005, "非法权限"),
    LOGIN_REQUIRED(10006, "请先登陆"),
    UNSUPPORTED_HANDLE(10007, "不允许该操作：%s"),
    SYSTEM_BUSY(10009, "系统繁忙，请稍后再试"),
    RPC_ERROR(10011, "RPC 错误"),
    ILLEGAL_REQUEST(10012, "非法请求"),
    INVALID_USER(10013, "非法用户"),
    PARAMETER_VALUE_INVALID(10017, "参数值非法 (%s): (%s)"),
    DATA_NOT_FOUND(10019, "数据不存在"),
    REQUEST_API_NOT_FOUND(10020, "接口不存在：%s"),
    PRIMARY_KEY_CONFLICT(10026, "数据 (%s) 已经存在 (%s)"),
    UNEXPECTED_ENUM_CASE(10027, "枚举值错误：(%s)"),
    DIRECTORY_NOT_FOUND(10028, "字典不存在"),
    PARAMETER_CAN_NOT_BE_EMPTY(10029, "%s 参数不能唯恐!"),
    SQL_ERROR(10030, "SQL执行失败"),
    INVALID_MEMBER(10031, "非法成员 (%s)"),
    FILE_IO_ERROR(10035, "文件读写失败"),
    RSA_ERROR(10036, "Rsa 错误"),
    DUPLICATE_RESOURCE_ERROR(10037, "资源重复"),


    FILE_DOES_NOT_EXIST(10038, "文件不存在，fileId: (%s)"),
    INVALID_PARAMETER(10039, "参数非法 (%s)"),
    MISSING_DATA(10040, "缺失数据 (%s)"),
    /**
     * Database related error status code
     */
    DATABASE_LOST(10300, "数据库连接失败"),
    INVALID_DATASET(10400, "非法的数据集"),
    /**
     * Service level status code
     */
    DATA_EXISTED(20001, "数据已经存在"),

    /**
     * board
     * An error occurred in the node of the flowchart
     */
    ERROR_IN_FLOW_GRAPH_NODE(30001, ""),
    /**
     * 添加数据资源时的表单参数错误
     * 区别于数据资源文件错误，这种错误不需要删除文件。
     */
    ERROR_IN_DATA_RESOURCE_ADD_FORM(30002, ""),

    /**
     * serving
     */
    CLIENT_SERVICE_EXIST(40001, "该客户已存在此服务！"),
    ERROR_PUBKEY_LENGTH(40002, "公钥长度不符合规范！"),
    CLIENT_NAME_EXIST(40003, "客户名称已存在！");

    private int code;
    private String description;

    StatusCode(int code, String message) {
        this.code = code;
        this.description = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage(String... args) {
        return String.format(description, args);
    }

    public StatusCodeWithException throwException() throws StatusCodeWithException {
        throw new StatusCodeWithException(this.getMessage(), this);
    }

    public StatusCodeWithException throwException(String message) throws StatusCodeWithException {
        throw new StatusCodeWithException(message, this);
    }

    public StatusCodeWithException throwException(Exception e) throws StatusCodeWithException {
        throw new StatusCodeWithException(e.getClass().getSimpleName() + " " + e.getMessage(), this);
    }


    @Override
    public String toString() {
        return code + "";
    }
}
