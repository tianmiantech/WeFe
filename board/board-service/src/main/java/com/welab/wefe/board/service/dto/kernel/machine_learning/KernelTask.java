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

package com.welab.wefe.board.service.dto.kernel.machine_learning;

import com.welab.wefe.board.service.dto.kernel.Member;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

import java.util.List;

/**
 * @author lonnie
 */
public class KernelTask {

    public KernelTask() {
    }

    public KernelTask(List<Member> members) {
        this.members = members;
    }

    private List<Member> members;

    @Check(name = "Mixed Federation promoter_id")
    private String mixPromoterMemberId;

    @Check(name = "Whether it is the main node of the current provider")
    private boolean providerMaster;
    @Check(name = "The id of the current provider")
    private String providerInnerId;
    @Check(name = "The primary node id of the current provider")
    private String providerMasterInnerId;
    @Check(name = "Other id of the current provider, not including itself")
    private List<String> providerOtherInnerId;

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public String getMixPromoterMemberId() {
        return mixPromoterMemberId;
    }

    public void setMixPromoterMemberId(String mixPromoterMemberId) {
        this.mixPromoterMemberId = mixPromoterMemberId;
    }

    public boolean isProviderMaster() {
        return providerMaster;
    }

    public void setProviderMaster(boolean providerMaster) {
        this.providerMaster = providerMaster;
    }

    public String getProviderInnerId() {
        return providerInnerId;
    }

    public void setProviderInnerId(String providerInnerId) {
        this.providerInnerId = providerInnerId;
    }

    public String getProviderMasterInnerId() {
        return providerMasterInnerId;
    }

    public void setProviderMasterInnerId(String providerMasterInnerId) {
        this.providerMasterInnerId = providerMasterInnerId;
    }

    public List<String> getProviderOtherInnerId() {
        return providerOtherInnerId;
    }

    public void setProviderOtherInnerId(List<String> providerOtherInnerId) {
        this.providerOtherInnerId = providerOtherInnerId;
    }
}
