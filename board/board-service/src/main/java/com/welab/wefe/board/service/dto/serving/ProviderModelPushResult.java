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
package com.welab.wefe.board.service.dto.serving;

/**
 * @author hunter.zhao
 * @date 2022/6/28
 */
public class ProviderModelPushResult {
    private String memberId;

    private String memberName;

    private boolean isSuccess;


    private ProviderModelPushResult() {
    }

    public static ProviderModelPushResult create(String memberId, String memberName, boolean isSuccess) {
        ProviderModelPushResult result = new ProviderModelPushResult();
        result.memberId = memberId;
        result.memberName = memberName;
        result.isSuccess = isSuccess;
        return result;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
