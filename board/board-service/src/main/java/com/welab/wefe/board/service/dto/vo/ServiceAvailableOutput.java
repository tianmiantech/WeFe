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

package com.welab.wefe.board.service.dto.vo;

import com.welab.wefe.common.enums.MemberService;

import java.util.List;

/**
 * 服务可用性
 *
 * @author aaron.li
 **/
public class ServiceAvailableOutput {
    /**
     * 服务名
     */
    private MemberService service;

    /**
     * 是否成功（当其下的所有服务列表为true时该值才为true，否则为false）
     */
    private boolean success;
    /**
     * 描述
     */
    private String message;

    /**
     * 相应服务列表
     */
    private List<MemberServiceStatusOutput> memberServiceStatusOutputList;

    public MemberService getService() {
        return service;
    }

    public void setService(MemberService service) {
        this.service = service;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<MemberServiceStatusOutput> getMemberServiceStatusOutputList() {
        return memberServiceStatusOutputList;
    }

    public void setMemberServiceStatusOutputList(List<MemberServiceStatusOutput> memberServiceStatusOutputList) {
        this.memberServiceStatusOutputList = memberServiceStatusOutputList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
