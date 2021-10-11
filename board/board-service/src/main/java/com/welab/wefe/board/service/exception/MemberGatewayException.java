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

package com.welab.wefe.board.service.exception;

import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;

/**
 * @author zane.luo
 */
public class MemberGatewayException extends StatusCodeWithException {
    private final String memberId;


    public MemberGatewayException(String memberId, String message) {
        super(message, StatusCode.REMOTE_SERVICE_ERROR);
        this.memberId = memberId;
    }

    @Override
    public String getMessage() {
        String memberName = CacheObjects.getMemberName(memberId);
        if (memberName == null) {
            memberName = memberId;
        }

        return "请求成员 " + memberName + " 失败：" + super.getMessage();
    }

    public String getMemberId() {
        return memberId;
    }
}
