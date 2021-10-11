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

package com.welab.wefe.gateway.common;

import io.grpc.Metadata;

/**
 * Grpc general constants
 *
 * @author aaron.li
 **/
public class GrpcConstant {
    /**
     * Signature verification request header key
     */
    public static final Metadata.Key<String> SIGN_HEADER_KEY = Metadata.Key.of("sign_header_key", Metadata.ASCII_STRING_MARSHALLER);
    /**
     * System time cut request header key
     */
    public static final Metadata.Key<String> SYSTEM_TIMESTAMP_HEADER_KEY = Metadata.Key.of("system_timestamp_header_key", Metadata.ASCII_STRING_MARSHALLER);
    /**
     * Request data hash header key
     */
    public static final Metadata.Key<String> REQ_DATA_HASH_SIGN_HEADER_KEY = Metadata.Key.of("req_data_hash_sign_header_key", Metadata.ASCII_STRING_MARSHALLER);
    /**
     * Invalid identification of whether the request is verified by the previous interceptor (since the server has not found a way to suspend the call of the next interceptor,
     * it is necessary to add an identification after the previous interceptor fails to pass the verification, so that the next interceptor can ignore the check)
     */
    public static final Metadata.Key<String> INTERCEPTOR_VERIFIED_REQ_INVALID_HEADER_KEY = Metadata.Key.of("interceptor_verify_req_invalid_header_key", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * The difference of the maximum system time cut, in seconds
     */
    public static final int MAX_SYSTEM_TIMESTAMP_DIFF = 120;

    /**
     * Connection unavailable exception keyword
     */
    public static final String CONNECTION_DISABLE_EXP_MSG = "UNAVAILABLE";
    /**
     * IP whitelist prompt exception keyword
     */
    public static final String IP_PERMISSION_EXP_MSG = "PERMISSION_DENIED";
    /**
     * Signature prompt exception keyword
     */
    public static final String SIGN_PERMISSION_EXP_MSG = "UNAUTHENTICATED";
    /**
     * System time cut exception keyword
     */
    public static final String SYSTEM_TIMESTAMP_PERMISSION_EXP_MSG = "FAILED_PRECONDITION";
    /**
     * Tamper proof exception keyword
     */
    public static final String INVALID_ARGUMENT_EXP_MSG = "INVALID_ARGUMENT";

    /**
     * Signature JSON structure related key
     */
    public static final String SIGN_KEY_DATA = "data";
    public static final String SIGN_KEY_MEMBER_ID = "memberId";
    public static final String SIGN_KEY_SIGN = "sign";
    public static final String SIGN_KEY_TIMESTAMP = "timestamp";
    public static final String SIGN_KEY_UUID = "uuid";

}
