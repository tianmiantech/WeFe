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

package com.welab.wefe.board.service.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.project.project.AddApi;
import com.welab.wefe.board.service.database.entity.job.JobMemberMySqlModel;
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
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.GatewayActionType;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.clickhouse.util.apache.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author seven.zeng
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

        checkJobMemberList(members);
        for (JobMemberMySqlModel member : members) {
            // Skip self
            if (CacheObjects.getMemberId().equals(member.getMemberId())) {
                continue;
            }

            sendToBoardRedirectApi(member.getMemberId(), me.getJobRole(), input, api);

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

        checkProjectMemberList(members);
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
            sendToBoardRedirectApi(member.getMemberId(), me.getMemberRole(), input, api);

        }
    }


    private void checkProjectMemberList(List<ProjectMemberMySqlModel> members) throws StatusCodeWithException {
        List<String> ids = members.stream().map(x -> x.getMemberId()).collect(Collectors.toList());
        checkMemberList(ids);
    }

    private void checkJobMemberList(List<JobMemberMySqlModel> members) throws StatusCodeWithException {
        List<String> ids = members.stream().map(x -> x.getMemberId()).collect(Collectors.toList());
        checkMemberList(ids);
    }

    /**
     * check member list, if any member in blacklist, throw exception.
     */
    private void checkMemberList(List<String> ids) throws StatusCodeWithException {
        List<String> blacklistMembers = ids.stream().filter(x -> CacheObjects.getMemberBlackList().contains(x)).collect(Collectors.toList());
        if (!blacklistMembers.isEmpty()) {
            String first = blacklistMembers.get(0);
            StatusCode
                    .ILLEGAL_REQUEST
                    .throwException("成员 " + CacheObjects.getMemberName(first)
                            + "（" + first
                            + "）在我方黑名单中，无法向其发送消息，如有必要，请在黑名单中移除该成员后再进行操作。"
                    );
        }
    }

    /**
     * Get the member list in the project and de duplicate it.
     */
    private List<ProjectMemberMySqlModel> findMembersByProjectId(String projectId) {
        List<ProjectMemberMySqlModel> members = projectMemberService.findListByProjectId(projectId);

        ProjectMemberMySqlModel promoter = members.stream()
                .filter(x -> x.getMemberRole() == JobMemberRole.promoter && StringUtils.isBlank(x.getInviterId()))
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
    public void refreshSystemConfigCache() {
        sendToMyselfGateway(
                GatewayActionType.refresh_system_config_cache,
                "refresh_system_config_cache",
                GatewayProcessorType.refreshSystemConfigCacheProcessor
        );
    }

    /**
     * Notify the gateway to update the member blacklist cache
     */
    public void refreshMemberBlacklistCache() {
        sendToMyselfGateway(
                GatewayActionType.refresh_system_config_cache,
                "refresh_member_blacklist_cache",
                GatewayProcessorType.refreshMemberBlacklistCacheProcessor
        );
    }

    /**
     * Notify the gateway to update the IP whitelist cache
     */
    public void refreshIpWhiteListCache() {
        sendToMyselfGateway(
                GatewayActionType.refresh_system_config_cache,
                "refresh_ip_white_list_cache",
                GatewayProcessorType.refreshSystemConfigCacheProcessor
        );
    }

    /**
     * Call the board of other members
     */
    public <T> T callOtherMemberBoard(String dstMemberId, Class<?> api, Object params, Class<T> resultClass) throws MemberGatewayException {
        Api annotation = api.getAnnotation(Api.class);
        ApiResult<JSONObject> apiResult = callOtherMemberBoard(dstMemberId, annotation.path(),
                params instanceof JSONObject
                        ? (JSONObject) params
                        : JObject.create(params)
        );

        if (apiResult.data != null) {
            return apiResult.data.toJavaObject(resultClass);
        }
        return null;
    }

    /**
     * Call the board of other members
     */
    public ApiResult<JSONObject> callOtherMemberBoard(String dstMemberId, String api, JSONObject data) throws MemberGatewayException {

        String request = JObject.create()
                .append("url", api)
                .append("method", "POST")
                .append("body", data)
                .toStringWithNull();

        ApiResult<JSONObject> result = sendToOtherGateway(dstMemberId, GatewayActionType.http_job, request, GatewayProcessorType.boardHttpProcessor);
        if (!result.success()) {
            throw new MemberGatewayException(dstMemberId, result.getMessage());
        }

        return result;
    }

    /**
     * Send the request to the gateway/redirect interface in the board
     */
    public ApiResult<?> sendToBoardRedirectApi(String receiverMemberId, JobMemberRole senderRole, Object data, Class<?> api) throws MemberGatewayException {
        Api annotation = api.getAnnotation(Api.class);

        return callOtherMemberBoard(receiverMemberId, "gateway/redirect",
                JObject
                        .create()
                        .put("api", annotation.path())
                        .put("data", data)
                        .put("caller_member_id", CacheObjects.getMemberId())
                        .put("caller_member_name", CacheObjects.getMemberName())
                        .put("caller_member_role", senderRole.name())
        );

    }


    /**
     * Check the connectivity of the gateway
     *
     * @param gatewayUri Gateway IP: prot address. If the value is not empty, it means to directly test its own gateway connectivity
     */
    public void checkMemberRouteConnect(String gatewayUri) throws StatusCodeWithException {

        if (StringUtil.isEmpty(gatewayUri)) {
            gatewayUri = globalConfigService.getGatewayConfig().intranetBaseUri;
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

        ApiResult<?> result = sendToMyselfGateway(gatewayUri, GatewayActionType.http_job, data, GatewayProcessorType.boardHttpProcessor);
        if (!result.success()) {
            throw new MemberGatewayException(CacheObjects.getMemberId(), result.getMessage());
        }

    }

    /**
     * Check the alive of the gateway
     *
     * @param gatewayUri Gateway IP: prot address. If the value is not empty, it means to directly test its own gateway alive
     */
    public void pingGatewayAlive(String gatewayUri) throws StatusCodeWithException {

        if (StringUtil.isEmpty(gatewayUri)) {
            gatewayUri = globalConfigService.getGatewayConfig().intranetBaseUri;
        }

        ApiResult<?> result = sendToMyselfGateway(gatewayUri, GatewayActionType.not_null, JObject.create().toString(), GatewayProcessorType.gatewayAliveProcessor);
        if (!result.success()) {
            throw new MemberGatewayException(CacheObjects.getMemberId(), result.getMessage());
        }
    }


}
