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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.api.member.MemberAvailableCheckApi;
import com.welab.wefe.board.service.database.entity.job.ProjectMemberMySqlModel;
import com.welab.wefe.board.service.exception.MemberGatewayException;
import com.welab.wefe.board.service.sdk.FlowService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.checkpoint.CheckpointManager;
import com.welab.wefe.common.wefe.checkpoint.dto.MemberAvailableCheckOutput;
import com.welab.wefe.common.wefe.checkpoint.dto.ServiceAvailableCheckOutput;
import com.welab.wefe.common.wefe.enums.ServiceType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lonnie
 */
@Service
public class ServiceCheckService extends AbstractService {
    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private CheckpointManager checkpointManager;
    @Autowired
    private GlobalConfigService globalConfigService;
    @Autowired
    private FlowService flowService;

    /**
     * 检查指定成员的服务是否可用
     */
    public MemberAvailableCheckOutput getMemberAvailableInfo(String memberId) throws MemberGatewayException {

        // If you are not checking your own status, go to the gateway.
        if (!CacheObjects.getMemberId().equals(memberId)) {
            return gatewayService.callOtherMemberBoard(
                    memberId,
                    MemberAvailableCheckApi.class,
                    new MemberAvailableCheckApi.Input(),
                    MemberAvailableCheckOutput.class
            );
        }

        MemberAvailableCheckOutput result = new MemberAvailableCheckOutput();
        List<ServiceType> serviceTypes = Arrays.asList(
                ServiceType.BoardService,
                ServiceType.UnionService,
                ServiceType.GatewayService,
                ServiceType.FlowService
        );

        for (ServiceType type : serviceTypes) {
            result.put(type, getServiceAvailableInfo(type));
        }

        return result;
    }

    public ServiceAvailableCheckOutput getServiceAvailableInfo(ServiceType serviceType) {
        try {
            switch (serviceType) {
                case BoardService:
                    return checkpointManager.checkAll();
                case GatewayService:
                    return gatewayService.getLocalGatewayAvailable();
                case UnionService:
                    return unionService.getAvailable();
                case FlowService:
                    return flowService.getAvailable();
                default:
                    StatusCode.UNEXPECTED_ENUM_CASE.throwException();
            }
        } catch (Exception e) {
            return new ServiceAvailableCheckOutput(e.getMessage());
        }
        return null;
    }

    /**
     * Check gateway connectivity
     *
     * @param local Whether to check the local gateway
     */
    public List<GatewayOnlineCheckResult> gatewayOnlineCheck(boolean local, String projectId, List<String> memberIds) {
        List<GatewayOnlineCheckResult> checkResultList = new ArrayList<>();
        GatewayOnlineCheckResult result = null;
        if (local) {
            result = checkGatewayConnect(globalConfigService.getGatewayConfig().intranetBaseUri);
            checkResultList.add(result);
        }

        // Detect that the board of other members cannot communicate with the gateway
        if (StringUtil.isNotEmpty(projectId)) {
            List<ProjectMemberMySqlModel> members = projectMemberService.findListByProjectId(projectId);
            members.stream().filter(x -> x.getMemberId().equals(CacheObjects.getMemberId())).collect(Collectors.toList());
            for (ProjectMemberMySqlModel member : members) {
                checkResultList.add(checkGatewayConnect(null));
            }
        }
        // When creating a project, check that the board and gateway of other members are not connected.
        if (CollectionUtils.isNotEmpty(memberIds)) {
            for (String memberId : memberIds) {
                checkResultList.add(checkGatewayConnect(null));
            }
        }

        return checkResultList;
    }


    /**
     * check if the flow gateway is available
     */
    private GatewayOnlineCheckResult checkGatewayConnect(String gatewayUri) {
        GatewayOnlineCheckResult result = new GatewayOnlineCheckResult();
        try {
            gatewayService.checkMemberRouteConnect(gatewayUri);
            // No exception is reported to prove that the connection is normal
            result.setOnline(true);
        } catch (StatusCodeWithException e) {
            result.setError(e.getMessage());
        }

        return result;
    }


    /**
     * Gateway connectivity check result
     */
    public static class GatewayOnlineCheckResult {
        /**
         * error message
         */
        private String error;

        private boolean online;

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }
    }
}
