/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;

/**
 * @author aaron.li
 **/
public class ReturnStatusBuilder {
    public static BasicMetaProto.ReturnStatus create(int code, String message) {
        return BasicMetaProto.ReturnStatus.newBuilder()
                .setCode(code)
                .setMessage(message)
                .build();
    }

    public static BasicMetaProto.ReturnStatus create(int code, String message, String sessionId) {
        return BasicMetaProto.ReturnStatus.newBuilder()
                .setCode(code)
                .setMessage(message)
                .setSessionId(sessionId)
                .build();
    }

    public static BasicMetaProto.ReturnStatus create(ReturnStatusEnum returnStatusEnum, String sessionId, String data) {
        return BasicMetaProto.ReturnStatus.newBuilder()
                .setCode(returnStatusEnum.getCode())
                .setMessage(returnStatusEnum.getMessage())
                .setSessionId(sessionId)
                .setData(data)
                .build();
    }

    public static BasicMetaProto.ReturnStatus ok() {
        return BasicMetaProto.ReturnStatus.newBuilder()
                .setCode(ReturnStatusEnum.OK.getCode())
                .setMessage(ReturnStatusEnum.OK.getMessage())
                .build();
    }

    public static BasicMetaProto.ReturnStatus ok(String sessionId) {
        return BasicMetaProto.ReturnStatus.newBuilder()
                .setCode(ReturnStatusEnum.OK.getCode())
                .setMessage(ReturnStatusEnum.OK.getMessage())
                .setSessionId(sessionId)
                .build();
    }

    public static BasicMetaProto.ReturnStatus ok(String sessionId, String data) {
        return BasicMetaProto.ReturnStatus.newBuilder()
                .setCode(ReturnStatusEnum.OK.getCode())
                .setMessage(ReturnStatusEnum.OK.getMessage())
                .setSessionId(sessionId)
                .setData(data)
                .build();
    }

    public static BasicMetaProto.ReturnStatus paramError(String attachMsg, String sessionId) {
        return BasicMetaProto.ReturnStatus.newBuilder()
                .setCode(ReturnStatusEnum.PARAM_ERROR.getCode())
                .setMessage(ReturnStatusEnum.PARAM_ERROR.getMessage() + ":" + attachMsg)
                .setSessionId(sessionId)
                .build();
    }

    public static BasicMetaProto.ReturnStatus paramError(String attachMsg) {
        return BasicMetaProto.ReturnStatus.newBuilder()
                .setCode(ReturnStatusEnum.PARAM_ERROR.getCode())
                .setMessage(ReturnStatusEnum.PARAM_ERROR.getMessage() + ":" + attachMsg)
                .build();
    }


    public static BasicMetaProto.ReturnStatus sysExc() {
        return BasicMetaProto.ReturnStatus.newBuilder()
                .setCode(ReturnStatusEnum.SYS_EXCEPTION.getCode())
                .setMessage(ReturnStatusEnum.SYS_EXCEPTION.getMessage())
                .build();
    }

    public static BasicMetaProto.ReturnStatus sysExc(String attachMsg, String sessionId) {
        return BasicMetaProto.ReturnStatus.newBuilder()
                .setCode(ReturnStatusEnum.SYS_EXCEPTION.getCode())
                .setMessage(ReturnStatusEnum.SYS_EXCEPTION.getMessage() + ":" + attachMsg)
                .setSessionId(sessionId)
                .build();
    }

    public static BasicMetaProto.ReturnStatus info(String attachMsg, String sessionId) {
        return BasicMetaProto.ReturnStatus.newBuilder()
                .setCode(ReturnStatusEnum.OK.getCode())
                .setMessage(ReturnStatusEnum.OK.getMessage() + ":" + attachMsg)
                .setSessionId(sessionId)
                .build();
    }
}
