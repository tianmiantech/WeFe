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

package com.welab.wefe.board.service.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.project.flow.*;
import com.welab.wefe.board.service.api.project.node.UpdateApi;
import com.welab.wefe.board.service.api.project.project.AddApi;
import com.welab.wefe.board.service.api.service.AliveApi;
import com.welab.wefe.board.service.database.entity.job.JobMemberMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectFlowMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMemberMySqlModel;
import com.welab.wefe.board.service.database.repository.JobMemberRepository;
import com.welab.wefe.board.service.exception.MemberGatewayException;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.checkpoint.dto.ServiceAvailableCheckOutput;
import com.welab.wefe.common.wefe.dto.global_config.GatewayConfigModel;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Service
public class GatewayService extends BaseGatewayService {

    @Autowired
    JobMemberRepository jobMemberRepo;

    @Autowired
    MessageService messageService;

    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private JobMemberService jobMemberService;
    @Autowired
    private GlobalConfigService globalConfigService;
    @Autowired
    private ProjectFlowService projectFlowService;

    /**
     * Synchronize messages to all job participants
     *
     * @param jobId job id
     * @param input Data to send
     * @param api   Send to target api
     */
    public void syncToOtherJobMembers(String jobId, AbstractApiInput input, Class<?> api) throws StatusCodeWithException {

        if (input.fromGateway()) {
            return;
        }

        List<JobMemberMySqlModel> members = jobMemberService.findListByJobId(jobId);

        JobMemberMySqlModel me = members.stream().filter(x -> CacheObjects.getMemberId().equals(x.getMemberId())).findFirst().orElse(null);
        // If I'm not in this project, don't send a broadcast
        if (me == null) {
            return;
        }

        checkBeforeSyncToJobMembers(members);
        for (JobMemberMySqlModel member : members) {
            // Skip self
            if (CacheObjects.getMemberId().equals(member.getMemberId())) {
                continue;
            }

            callOtherMemberBoard(member.getMemberId(), me.getJobRole(), api, input);
        }
    }


    /**
     * Synchronize messages to all members
     */
    public void syncToOtherFormalProjectMembers(String projectId, AbstractApiInput input, Class<?> api) throws StatusCodeWithException {
        syncToOtherProjectMembers(projectId, input, api, true, false);
    }

    /**
     * Synchronize messages to all members who have not exited
     */
    public void syncToNotExistedMembers(String projectId, AbstractApiInput input, Class<?> api) throws StatusCodeWithException {
        syncToOtherProjectMembers(projectId, input, api, false, false);
    }

    /**
     * Synchronize messages to all members (including informal members and exited members)
     */
    public void syncToAllMembers(String projectId, AbstractApiInput input, Class<?> api) throws StatusCodeWithException {
        syncToOtherProjectMembers(projectId, input, api, false, true);
    }

    /**
     * Synchronize messages to all project members
     *
     * @param projectId            project id
     * @param input                api input parameter
     * @param api                  Send to target api
     * @param excludeFormalMember  Is exclude informal members?
     * @param includeExistedMember Is include exited members?
     */
    private void syncToOtherProjectMembers(String projectId, AbstractApiInput input, Class<?> api, boolean excludeFormalMember, boolean includeExistedMember) throws StatusCodeWithException {
        // If the request comes from the gateway, it is no longer broadcast.
        if (input.fromGateway()) {
            return;
        }

        List<ProjectMemberMySqlModel> members = findMembersByProjectId(projectId);

        ProjectMemberMySqlModel me = members.stream().filter(x -> CacheObjects.getMemberId().equals(x.getMemberId())).findFirst().orElse(null);
        // If I'm not in this project, don't send a broadcast.
        if (me == null) {
            return;
        }

        boolean needSkipOtherPromoters = false;
        String flowId = "";
        // 对流程相关操作特殊处理
        if (input instanceof UpdateApi.Input) {
            flowId = ((UpdateApi.Input) input).getFlowId();
        } else if (input instanceof UpdateFlowGraphApi.Input) {
            flowId = ((UpdateFlowGraphApi.Input) input).getFlowId();
        } else if (input instanceof UpdateFlowBaseInfoApi.Input) {
            flowId = ((UpdateFlowBaseInfoApi.Input) input).getFlowId();
        } else if (input instanceof AddFlowApi.Input) {
            flowId = ((AddFlowApi.Input) input).getFlowId();
        } else if (input instanceof CopyFlowApi.Input) {
            flowId = ((CopyFlowApi.Input) input).getSourceFlowId();
        } else if (input instanceof DeleteApi.Input) {
            flowId = ((DeleteApi.Input) input).getFlowId();
        }
        if (StringUtils.isNotBlank(flowId)) {
            ProjectFlowMySqlModel flow = projectFlowService.findOne(flowId);
            if (flow.getFederatedLearningType() == FederatedLearningType.horizontal
                    || flow.getFederatedLearningType() == FederatedLearningType.vertical) {
                needSkipOtherPromoters = true;
            }
        }

        checkBeforeSyncToProjectMembers(members);
        for (ProjectMemberMySqlModel member : members) {
            // Skip self
            if (CacheObjects.getMemberId().equals(member.getMemberId())) {
                continue;
            }

            // Skip exited members
            if (!includeExistedMember && member.isExited()) {
                continue;
            }

            // Skip members that have disagree the audit
            if (excludeFormalMember) {
                if (AuditStatus.disagree == member.getAuditStatus()) {
                    continue;
                }
            }
            if (input instanceof AddApi.Input) {
                ((AddApi.Input) input).setRole(member.getMemberRole());
            }

            if (needSkipOtherPromoters && member.getMemberRole() == JobMemberRole.promoter) {
                continue;
            }
            callOtherMemberBoard(member.getMemberId(), me.getMemberRole(), api, input);
        }
    }

    /**
     * @see {{@link #checkBeforeSyncToOtherMembers(List)}}
     */
    private void checkBeforeSyncToProjectMembers(List<ProjectMemberMySqlModel> members) throws StatusCodeWithException {
        List<String> ids = members
                .stream()
                .map(ProjectMemberMySqlModel::getMemberId)
                .collect(Collectors.toList());

        checkBeforeSyncToOtherMembers(ids);
    }

    /**
     * @see {{@link #checkBeforeSyncToOtherMembers(List)}}
     */
    private void checkBeforeSyncToJobMembers(List<JobMemberMySqlModel> members) throws StatusCodeWithException {
        List<String> ids = members
                .stream()
                .map(JobMemberMySqlModel::getMemberId)
                .collect(Collectors.toList());

        checkBeforeSyncToOtherMembers(ids);
    }

    /**
     * cache
     */
    protected static final ExpiringMap<String, Object> CACHE_MAP = ExpiringMap.builder().expiration(60, TimeUnit.SECONDS).maxSize(500).build();

    /**
     * 在将消息广播到其它成员之前的检查
     * 1. 检查是否在黑名单中
     * 2. 检查与之通信情况是否正常
     */
    private void checkBeforeSyncToOtherMembers(List<String> memberIds) throws StatusCodeWithException {
        List<String> ids = memberIds
                .stream()
                // 剔除自己
                .filter(x -> !CacheObjects.getMemberId().equals(x))
                // 去重
                .distinct()
                .collect(Collectors.toList());

        for (String memberId : ids) {
            // 检查是否在黑名单中
            if (CacheObjects.getMemberBlackList().contains(memberId)) {
                StatusCode
                        .ILLEGAL_REQUEST
                        .throwException("成员 " + CacheObjects.getMemberName(memberId)
                                + " 在我方黑名单中，无法向其发送消息，如有必要，请在黑名单中移除该成员后再进行操作。"
                        );
            }

            // 检查通信是否正常，避免在多成员广播时，出现部分成员成功，部分失败的情况。
            String key = memberId + "_call_board_alive";
            // 使用缓存，避免高频请求。
            if (!CACHE_MAP.containsKey(key)) {
                try {
                    callOtherMemberBoard(memberId, AliveApi.class, null, Object.class);
                    CACHE_MAP.put(key, null);
                } catch (StatusCodeWithException e) {
                    throw e;
                }
            }

        }
    }


    /**
     * Get the member list in the project and de duplicate it.
     */
    private List<ProjectMemberMySqlModel> findMembersByProjectId(String projectId) {
        List<ProjectMemberMySqlModel> members = projectMemberService.findListByProjectId(projectId);

        ProjectMemberMySqlModel promoter = members.stream()
                .filter(x -> x.getMemberRole() == JobMemberRole.promoter && StringUtil.isBlank(x.getInviterId()))
                .findFirst().orElse(null);

        // Since the initiator models with itself, the records of the initiator as a provider should be eliminated to
        // avoid sending a message to the promoter multiple times
        return members
                .stream()
                .filter(x -> !(x.getMemberRole() == JobMemberRole.provider && x.getMemberId().equals(promoter.getMemberId())))
                .collect(Collectors.toList());

    }

    /**
     * Notify the gateway to update the system configuration cache
     */
    public void refreshSystemConfigCache() throws StatusCodeWithException {
        sendToMyselfGateway(
                "",
                GatewayProcessorType.refreshSystemConfigCacheProcessor
        );
    }

    /**
     * Notify the gateway to update the member blacklist cache
     */
    public void refreshMemberBlacklistCache() throws StatusCodeWithException {
        sendToMyselfGateway(
                "",
                GatewayProcessorType.refreshMemberBlacklistCacheProcessor
        );
    }

    /**
     * Notify the gateway to update the partner config cache
     */
    public void refreshPartnerConfigCache() throws StatusCodeWithException {
        sendToMyselfGateway(
                "",
                GatewayProcessorType.refreshPartnerConfigCacheProcessor
        );
    }

    /**
     * Notify the gateway to update the IP whitelist cache
     */
    public void refreshIpWhiteListCache() throws StatusCodeWithException {
        sendToMyselfGateway(
                "",
                GatewayProcessorType.refreshSystemConfigCacheProcessor
        );
    }

    /**
     * Notify the gateway to update the system configuration cache
     */
    public void restartExternalGrpcServer() throws StatusCodeWithException {
        sendToMyselfGateway(
                "",
                GatewayProcessorType.restartExternalGrpcServer
        );
    }

    /**
     * 检查gateway是否活着
     */
    public void checkGatewayAliveProcessor() throws StatusCodeWithException {
        sendToMyselfGateway(
                "",
                GatewayProcessorType.gatewayAliveProcessor
        );
    }

    public ServiceAvailableCheckOutput getLocalGatewayAvailable() throws StatusCodeWithException {
        return sendToMyselfGateway(
                "",
                GatewayProcessorType.gatewayAvailableProcessor
        ).toJavaObject(ServiceAvailableCheckOutput.class);
    }

    public <T> T callOtherMemberBoard(String dstMemberId, Class<?> api, Class<T> resultClass) throws StatusCodeWithException {
        return callOtherMemberBoard(dstMemberId, null, api, null, resultClass);
    }

    public void callOtherMemberBoard(String dstMemberId, Class<?> api, Object params) throws StatusCodeWithException {
        callOtherMemberBoard(dstMemberId, null, api, params, Object.class);
    }

    public void callOtherMemberBoard(String dstMemberId, JobMemberRole senderRole, Class<?> api, Object params) throws StatusCodeWithException {
        callOtherMemberBoard(dstMemberId, senderRole, api, params, Object.class);
    }

    public <T> T callOtherMemberBoard(String dstMemberId, Class<?> api, Object params, Class<T> resultClass) throws StatusCodeWithException {
        return callOtherMemberBoard(dstMemberId, null, api, params, resultClass);
    }

    /**
     * Send the request to the gateway/redirect interface in the board
     *
     * @param dstMemberId 接收请求的成员Id
     * @param senderRole  发送请求的成员角色，可以为 null。
     * @param api         被调用的接口名
     * @param params      接口请求参数
     * @param resultClass 响应结果的实体类型
     */
    public <T> T callOtherMemberBoard(String dstMemberId, JobMemberRole senderRole, Class<?> api, Object params, Class<T> resultClass) throws StatusCodeWithException {
        Api annotation = api.getAnnotation(Api.class);

        JSONObject result = callOtherMemberBoard(
                dstMemberId,
                "gateway/redirect",
                JObject
                        .create()
                        .put("api", annotation.path())
                        .put("data", params)
                        .put("caller_member_id", CacheObjects.getMemberId())
                        .put("caller_member_name", CacheObjects.getMemberName())
                        .put("caller_member_role", senderRole == null ? "" : senderRole.name())
        );

        ApiResult<?> apiResult = result.toJavaObject(ApiResult.class);
        if (!apiResult.success()) {
            throw new MemberGatewayException(dstMemberId, apiResult.message);
        }

        JSONObject data = result.getJSONObject("data");

        if (data == null) {
            return null;
        }

        if (resultClass == JObject.class) {
            return (T) JObject.create(data);
        }

        return data.toJavaObject(resultClass);
    }


    /**
     * Call the board of other members
     */
    private JSONObject callOtherMemberBoard(String dstMemberId, String api, JSONObject data) throws StatusCodeWithException {

        String request = JObject.create()
                .append("url", api)
                .append("method", "POST")
                .append("body", data)
                .toStringWithNull();

        JSONObject result = sendToOtherGateway(
                dstMemberId,
                request,
                GatewayProcessorType.boardHttpProcessor
        );


        return result;
    }


    /**
     * Check the connectivity of the gateway
     *
     * @param gatewayUri Gateway IP: prot address. If the value is not empty, it means to directly test its own gateway connectivity
     */
    public void checkMemberRouteConnect(String gatewayUri) throws StatusCodeWithException {

        if (StringUtil.isEmpty(gatewayUri)) {
            gatewayUri = globalConfigService.getModel(GatewayConfigModel.class).intranetBaseUri;
        }

        // Create request entity message
        String data = JObject.create()
                .append("url", "gateway/redirect")
                .append("method", "POST")
                .append(
                        "body",
                        JObject
                                .create()
                                .append("api", "gateway/test_route_connect")
                                .append("data", JObject.create())
                                .toString()
                )
                .toStringWithNull();
        sendToMyselfGateway(gatewayUri, data, GatewayProcessorType.boardHttpProcessor).toJavaObject(ApiResult.class);
    }


    /**
     * Check the alive of the gateway
     */
    public void pingGatewayAlive(String dstMemberId, String gatewayUri) throws StatusCodeWithException {
        sendToOtherGateway(dstMemberId, gatewayUri, JObject.create().toString(), GatewayProcessorType.gatewayAliveProcessor);
    }

}
