/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
    SYSTEM_NOT_BEEN_INITIALIZED(10000, "The system has not been initialized, please perform initialization first."),
    SYSTEM_ERROR(10001, "System error"),
    SERVICE_UNAVAILABLE(10002, "Service suspension"),
    REMOTE_SERVICE_ERROR(10003, "Remote service error"),
    IP_LIMIT(10004, "IP restrictions cannot request this resource"),
    PERMISSION_DENIED(10005, "No permission / access denied"),
    LOGIN_REQUIRED(10006, "Please log in and visit"),
    UNSUPPORTED_HANDLE(10007, "Unsupported operation：%s"),
    SYSTEM_BUSY(10009, "There are too many tasks and the system is busy"),
    JOB_EXPIRED(10010, "Task timeout"),
    RPC_ERROR(10011, "RPC error"),
    ILLEGAL_REQUEST(10012, "Illegal request"),
    INVALID_USER(10013, "Illegal user"),
    INSUFFICIENT_APP_PERMISSIONS(10014, "Insufficient application permissions"),
    PARAMETER_VALUE_INVALID(10017, "Illegal value of parameter (%s): (%s)"),
    DATA_NOT_FOUND(10019, "Data does not exist"),
    REQUEST_API_NOT_FOUND(10020, "Interface does not exist：%s"),
    HTTP_METHOD_NOT_SUPPORTED(10021, "The requested HTTP method is not supported"),
    IP_REQUESTS_OUT_OF_RATE_LIMIT(10022, "IP request frequency exceeds the upper limit"),
    USER_REQUESTS_OUT_OF_RATE_LIMIT(10023, "User request frequency exceeds the upper limit"),
    PRIMARY_KEY_CONFLICT(10026, "Data with value (%s) already exists for parameter (%s)"),
    UNEXPECTED_ENUM_CASE(10027, "Unexpected enumeration item：(%s)"),
    DIRECTORY_NOT_FOUND(10028, "directory does not exist"),
    PARAMETER_CAN_NOT_BE_EMPTY(10029, "%s can not be empty!"),
    SQL_ERROR(10030, "SQL execution failed"),
    INVALID_MEMBER(10031, "Invalid member id (%s)"),
    BLOCK_RANGE_PARAM_INVALID(10032, "Block range error, from / toblock must be greater than 0, toblock must be greater than fromblock"),
    INVALID_EVENT(10033, "Invalid event (%s)"),
    REPEAT_SUBSCRIPTION(10034, "Do not repeat subscription"),
    FILE_IO_ERROR(10035, "File read / write failed"),
    RSA_ERROR(10036, "Rsa error"),
    DUPLICATE_RESOURCE_ERROR(10037, "Duplicate resource"),


    FILE_DOES_NOT_EXIST(10038, "file does not exist error fileId: (%s)"),
    INVALID_PARAMETER(10039, "Invalid parameter (%s)"),
    /**
     * Database related error status code
     */
    DATABASE_LOST(10300, "Database lost connection"),

    /**
     * Service level status code
     */
    DATA_EXISTED(20001, "The data already exists"),

    /**
     * board
     * An error occurred in the node of the flowchart
     */
    ERROR_IN_FLOW_GRAPH_NODE(30001, "");


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
