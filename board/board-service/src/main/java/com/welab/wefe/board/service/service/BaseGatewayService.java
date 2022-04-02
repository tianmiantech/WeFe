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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import com.welab.wefe.board.service.dto.globalconfig.GatewayConfigModel;
import com.welab.wefe.board.service.proto.TransferServiceGrpc;
import com.welab.wefe.board.service.proto.meta.basic.BasicMetaProto;
import com.welab.wefe.board.service.proto.meta.basic.GatewayMetaProto;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.GatewayActionType;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author zane
 */
public class BaseGatewayService extends AbstractService {

    @Autowired
    private GlobalConfigService globalConfigService;


    /**
     * Send a message to your own gateway service
     */
    protected JSONObject sendToMyselfGateway(GatewayActionType action, String data, GatewayProcessorType processorType) throws StatusCodeWithException {
        return sendToMyselfGateway(null, action, data, processorType);
    }

    /**
     * Send a message to your own gateway service
     */
    protected JSONObject sendToMyselfGateway(String gatewayUri, GatewayActionType action, String data, GatewayProcessorType processorType) throws StatusCodeWithException {
        if (gatewayUri == null) {
            GatewayConfigModel gatewayConfig = globalConfigService.getGatewayConfig();
            if (gatewayConfig != null) {
                gatewayUri = gatewayConfig.intranetBaseUri;
            }
        }

        return callGateway(
                gatewayUri,
                CacheObjects.getMemberId(),
                CacheObjects.getMemberName(),
                action,
                data,
                processorType);
    }

    /**
     * Send message to other party's gateway service
     */
    protected JSONObject sendToOtherGateway(String dstMemberId, GatewayActionType action, String data, GatewayProcessorType processorType) throws StatusCodeWithException {
        return callGateway(
                globalConfigService.getGatewayConfig().intranetBaseUri,
                dstMemberId,
                CacheObjects.getMemberName(dstMemberId),
                action,
                data,
                processorType
        );
    }

    /**
     * Basic method send message to gateway service by grpc
     *
     * @param gatewayUri    gateway service address
     * @param dstMemberId   the member_id of the target member
     * @param dstMemberName The member_name of the target member
     * @param action        action of the message
     * @param data          data of the message
     * @param processorType enum, see:{@link com.welab.wefe.common.wefe.enums.GatewayProcessorType}
     */
    private JSONObject callGateway(String gatewayUri, String dstMemberId, String dstMemberName, GatewayActionType action, String data, GatewayProcessorType processorType) throws StatusCodeWithException {

        if (StringUtil.isEmpty(gatewayUri)) {
            StatusCode.RPC_ERROR.throwException("尚未设置 gateway 内网地址，请在[全局设置][系统设置]中设置 gateway 服务的内网地址。");
        }

        GatewayMetaProto.TransferMeta transferMeta = buildTransferMeta(dstMemberId, dstMemberName, action, data, processorType);
        ManagedChannel grpcChannel = null;
        String message = "[grpc] end to " + dstMemberName;
        try {
            grpcChannel = getGrpcChannel(gatewayUri);
            TransferServiceGrpc.TransferServiceBlockingStub clientStub = TransferServiceGrpc.newBlockingStub(grpcChannel);
            BasicMetaProto.ReturnStatus returnStatus = clientStub.send(transferMeta);
            if (returnStatus.getCode() != 0) {
                StatusCode.REMOTE_SERVICE_ERROR.throwException(returnStatus.getMessage());
            }

            message += "success request:" + data;
            LOG.info(message);

            return JSON.parseObject(returnStatus.getData());
        } catch (Exception e) {
            message += "fail message:" + e.getMessage() + " request:" + data;
            LOG.error(message);

            LOG.error("Request gateway exception, message: " + transferMetaToString(transferMeta) + ",exception：" + e.getMessage(), e);

            try {
                checkPermission(e);
            } catch (StatusCodeWithException ex) {
                StatusCode.RPC_ERROR.throwException(ex.getMessage());
            }
            StatusCode.RPC_ERROR.throwException(e.getMessage());
        } finally {
            if (null != grpcChannel) {
                try {
                    grpcChannel.shutdownNow().awaitTermination(2, TimeUnit.SECONDS);
                } catch (Exception e) {
                    LOG.error("Closing gateway connection exception：", e);
                }
            }
        }

        return null;
    }


    /**
     * convert TransferMeta to string
     */
    private String transferMetaToString(MessageOrBuilder message) {
        try {
            return JsonFormat.printer().print(message);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Check gateway permissions issues, such as IP whitelist and signature issues
     *
     * @param e The exception object returned by the access gateway
     */
    protected void checkPermission(Exception e) throws StatusCodeWithException {
        String errorMsg = e.getMessage();
        // Connection unavailable
        String connectionDisableTips = "UNAVAILABLE";
        // The gateway prompts abnormal information in response to the IP whitelist
        String ipPermissionTips = "PERMISSION_DENIED";
        // The gateway responds to the signature and prompts abnormal information
        String signPermissionTips = "UNAUTHENTICATED";
        if (StringUtil.isEmpty(errorMsg) || errorMsg.contains(connectionDisableTips)) {
            throw new StatusCodeWithException("连接不可用，请检查网关地址是否正确或网络连接是否正常或网关服务是否已启动", StatusCode.RPC_ERROR);
        }
        if (errorMsg.contains(ipPermissionTips)) {
            throw new StatusCodeWithException("请在 [全局设置] -> [系统设置] 菜单下添加 board 服务的 IP 地址到 gateway 白名单，详细信息请查看 [Dashboard] 菜单", StatusCode.IP_LIMIT);
        }
        if (errorMsg.contains(signPermissionTips)) {
            throw new StatusCodeWithException("签名失败，请确保Member的公私钥正确性以及公钥是否已上传", StatusCode.RPC_ERROR);
        }
    }

    private ManagedChannel getGrpcChannel(String gatewayUri) throws StatusCodeWithException {
        if (StringUtil.isEmpty(gatewayUri)) {
            throw new StatusCodeWithException("请到 [全局设置] -> [系统设置] 菜单下配置网关地址信息，格式为 IP:PORT", StatusCode.PARAMETER_VALUE_INVALID);
        }

        if (!isValidGatewayUri(gatewayUri)) {
            throw new StatusCodeWithException("网关地址格式不正确，格式应为 IP:PORT", StatusCode.PARAMETER_VALUE_INVALID);
        }

        return ManagedChannelBuilder
                .forTarget(gatewayUri)
                .usePlaintext()
                .maxInboundMessageSize(2000 * 1024 * 1024)
                .build();
    }

    protected GatewayMetaProto.TransferMeta buildTransferMeta(String dstMemberId, String dstMemberName, GatewayActionType action, String data, GatewayProcessorType processorType) {
        GatewayMetaProto.Member.Builder builder = GatewayMetaProto.Member.newBuilder()
                .setMemberId(dstMemberId);

        if (StringUtil.isNotBlank(dstMemberName)) {
            builder.setMemberName(dstMemberName);
        }

        GatewayMetaProto.Member dstMember = builder
                .build();

        GatewayMetaProto.Content content = GatewayMetaProto.Content.newBuilder()
                .setObjectData(data)
                .build();

        return GatewayMetaProto.TransferMeta.newBuilder()
                .setAction(action.name())
                .setDst(dstMember)
                .setContent(content)
                .setSessionId(UUID.randomUUID().toString().replaceAll("-", ""))
                .setProcessor(processorType.name())
                .build();

    }

    /**
     * Check if it is a legal gateway format
     *
     * @param gatewayUri Gateway address, the format must be: <ip/host>:<port>
     */
    private boolean isValidGatewayUri(String gatewayUri) {
        if (StringUtil.isEmpty(gatewayUri)) {
            return false;
        }

        String splitSymbol = ":";
        return gatewayUri.contains(splitSymbol) && gatewayUri.split(splitSymbol).length <= 2 && StringUtil.isNumeric(gatewayUri.split(splitSymbol)[1]);
    }

}
