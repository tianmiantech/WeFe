/**
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

import com.welab.wefe.board.service.api.member.ServiceStatusCheckApi;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.job.ProjectMemberMySqlModel;
import com.welab.wefe.board.service.dto.vo.MemberServiceStatusOutput;
import com.welab.wefe.board.service.exception.MemberGatewayException;
import com.welab.wefe.board.service.sdk.FlowService;
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.data.storage.config.JdbcParamConfig;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.repo.Storage;
import com.welab.wefe.common.data.storage.service.StorageService;
import com.welab.wefe.common.enums.MemberService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.welab.wefe.board.service.service.DataSetStorageService.DATABASE_NAME;

/**
 * @author lonnie
 */
@Service
public class ServiceCheckService extends AbstractService {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FlowService flowService;
    @Autowired
    private UnionService unionService;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private JdbcParamConfig jdbcParamConfig;

    @Autowired
    private Config config;

    @Autowired
    private GlobalConfigService globalConfigService;

    /**
     * check if each service is available
     */
    public ServiceStatusCheckApi.Output checkMemberServiceStatus(ServiceStatusCheckApi.Input input) throws MemberGatewayException {

        String memberId = input.getMemberId();
        // If you are not checking your own status, go to the gateway.
        if (!CacheObjects.getMemberId().equals(memberId)) {
            return gatewayService.callOtherMemberBoard(
                    memberId,
                    ServiceStatusCheckApi.class,
                    JObject.create(input),
                    ServiceStatusCheckApi.Output.class
            );
        }

        LinkedHashMap<MemberService, MemberServiceStatusOutput> result = new LinkedHashMap<>();

        if (input.getService() == null) {
            result.put(MemberService.gateway, checkLocalGatewayStatus());
            result.put(MemberService.flow, checkFlowServiceStatus(!input.fromGateway()));
            result.put(MemberService.union, checkUnionServiceStatus());
            result.put(MemberService.storage, checkStorageServiceStatus(!input.fromGateway()));
            return new ServiceStatusCheckApi.Output(result);
        }

        switch (input.getService()) {
            case union:
                result.put(MemberService.union, checkUnionServiceStatus());
                break;

            case gateway:
                result.put(MemberService.gateway, checkLocalGatewayStatus());
                break;

            case flow:
                result.put(MemberService.flow, checkFlowServiceStatus(!input.fromGateway()));
                break;

            case storage:
                result.put(MemberService.storage, checkStorageServiceStatus(!input.fromGateway()));
                break;

            default:
                break;
        }

        return new ServiceStatusCheckApi.Output(result);
    }

    /**
     * check if the union service is available
     */
    public MemberServiceStatusOutput checkUnionServiceStatus() {
        MemberServiceStatusOutput output = new MemberServiceStatusOutput(MemberService.union);
        output.setValue(config.getUNION_BASE_URL());
        try {
            unionService.queryMember(0, 1);
            output.setSuccess(true);
        } catch (StatusCodeWithException e) {
            output.setSuccess(true);
            output.setMessage(e.getMessage());
        }

        return output;
    }

    /**
     * check if the gateway service is available
     */
    public MemberServiceStatusOutput checkLocalGatewayStatus() {
        MemberServiceStatusOutput output = new MemberServiceStatusOutput(MemberService.gateway);
        output.setValue(globalConfigService.getGatewayConfig().intranetBaseUri);

        try {
            GatewayOnlineCheckResult result = checkGatewayConnect(globalConfigService.getGatewayConfig().intranetBaseUri);

            output.setSuccess(result.online);
            output.setMessage(result.error);

        } catch (Exception e) {
            output.setSuccess(false);
            output.setMessage(e.getMessage());
        }


        return output;
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
     * check if the storage service is available
     */
    public MemberServiceStatusOutput checkStorageServiceStatus(boolean checkerIsMyself) {
        MemberServiceStatusOutput output = new MemberServiceStatusOutput(MemberService.storage);

        // The connection string is non-exposed information and cannot be output to other members.
        if (checkerIsMyself) {
            output.setValue(jdbcParamConfig.getUrl());
        }


        Storage storage = storageService.getStorage();
        String name = RandomStringUtils.randomAlphabetic(6);
        try {
            storage.put(DATABASE_NAME, name, new DataItemModel<>(name, "test"));
            output.setSuccess(true);
        } catch (Exception e) {
            super.log(e);
            output.setSuccess(false);
            output.setMessage(config.getDbType().name() + " put异常，请检查相关配置是否正确。");
            return output;
        }

        try {
            storage.dropTB(DATABASE_NAME, name);
            output.setSuccess(true);
        } catch (Exception e) {
            output.setSuccess(false);
            output.setMessage(config.getDbType().name() + " drop异常，请检查相关配置是否正确。");
        }

        return output;
    }

    /**
     * check if the flow service is available
     */
    public MemberServiceStatusOutput checkFlowServiceStatus(boolean checkerIsMyself) {
        MemberServiceStatusOutput output = new MemberServiceStatusOutput(MemberService.flow);

        if (checkerIsMyself) {
            output.setValue(globalConfigService.getFlowConfig().intranetBaseUri);
        }

        try {
            JObject result = flowService.dashboard();

            JObject board = result.getJObject(MemberService.board.name());
            JObject gateway = result.getJObject(MemberService.gateway.name());

            output = buildResult(MemberService.board.name(), board, output);
            output = buildResult(MemberService.gateway.name(), gateway, output);

        } catch (Exception e) {
            output.setSuccess(false);
            output.setMessage(e.getMessage());
        }

        return output;
    }

    private MemberServiceStatusOutput buildResult(String checkpoint, JObject obj, MemberServiceStatusOutput output) {

        if (obj == null) {
            output.setSuccess(false);
            output.setMessage("flow 服务不可用，检查点 " + checkpoint + " 检查失败。");
        } else if (obj.getInteger("code") != null && obj.getInteger("code") == 0) {
            output.setSuccess(true);
            output.setMessage(obj.getString("message"));
        } else {
            output.setSuccess(false);
            output.setMessage(obj.getString("message"));
        }

        return output;
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
