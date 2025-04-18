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

package com.welab.wefe.board.service.dto.kernel;

import com.welab.wefe.board.service.api.member.GetMemberMachineLearningEnvApi;
import com.welab.wefe.board.service.component.DataIOComponent;
import com.welab.wefe.board.service.database.entity.job.JobMemberMySqlModel;
import com.welab.wefe.board.service.dto.kernel.machine_learning.Env;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.GatewayService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.FcCloudProvider;
import com.welab.wefe.common.wefe.enums.JobBackendType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

import java.util.ArrayList;
import java.util.List;


/**
 * @author zane.luo
 */
public class Member {

    private String memberId;
    private String memberName;
    private JobMemberRole memberRole;
    private JobBackendType backend;
    private FcCloudProvider fcProvider;

    public Member() {
    }

    /**
     * 创建用于深度学习的 Member 对象
     */
    public static List<Member> forDeepLearning(List<JobMemberMySqlModel> members) {
        List<Member> list = new ArrayList<>();
        if (members == null) {
            return list;
        }

        for (JobMemberMySqlModel member : members) {
            Member m = new Member();
            m.memberId = member.getMemberId();
            m.memberName = CacheObjects.getMemberName(member.getMemberId());
            m.memberRole = member.getJobRole();
            list.add(m);
        }
        return list;
    }

    public static List<Member> forMachineLearning(List<JobMemberMySqlModel> members) {
        List<Member> list = new ArrayList<>();
        if (members == null) {
            return list;
        }

        for (JobMemberMySqlModel member : members) {
            list.add(forMachineLearning(member));
        }
        return list;
    }

    /**
     * 创建一个新的 Member 对象，用于 Machine Learning 的 Job。
     */
    public static Member forMachineLearning(JobMemberMySqlModel member) {
        return buildMachineLearningMember(member.getMemberId(), member.getJobRole());
    }

    /**
     * 创建一个新的 Member 对象，用于 Machine Learning 的 Job。
     */
    public static Member forMachineLearning(DataIOComponent.DataSetItem dataSetItem) {
        return buildMachineLearningMember(dataSetItem.getMemberId(), dataSetItem.getMemberRole());
    }

    /**
     * 创建一个新的 Member 对象，用于 Machine Learning 的 Job。
     */
    public static Member forMachineLearning(String memberId, JobMemberRole role) {
        return buildMachineLearningMember(memberId, role);
    }

    private static Member buildMachineLearningMember(String memberId, JobMemberRole memberRole) {
        Member member = new Member();
        member.setMemberId(memberId);
        member.setMemberName(CacheObjects.getMemberName(memberId));
        member.setMemberRole(memberRole);

        Env env = getMemberEnv(memberId);
        member.backend = env.getCalculationEngineConfig().backend;
        if (member.backend == JobBackendType.FC) {
            member.fcProvider = env.getCalculationEngineConfig().fcCloudProvider;
        }

        return member;
    }

    private static Env getMemberEnv(String memberId) {
        // 自己，从本地取。
        if (CacheObjects.isCurrentMember(memberId)) {
            return Env.get();
        }

        GatewayService gatewayService = Launcher.getBean(GatewayService.class);
        Env env = null;
        try {
            env = gatewayService.callOtherMemberBoard(memberId, GetMemberMachineLearningEnvApi.class, Env.class);
        } catch (StatusCodeWithException e) {
            return null;
        }
        return env;
    }

    //region getter/setter

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

    public JobMemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(JobMemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public JobBackendType getBackend() {
        return backend;
    }

    public void setBackend(JobBackendType backend) {
        this.backend = backend;
    }

    public FcCloudProvider getFcProvider() {
        return fcProvider;
    }

    public void setFcProvider(FcCloudProvider fcProvider) {
        this.fcProvider = fcProvider;
    }

    //endregion
}
