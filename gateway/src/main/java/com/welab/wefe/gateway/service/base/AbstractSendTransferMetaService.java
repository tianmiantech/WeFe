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

package com.welab.wefe.gateway.service.base;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.common.EndpointBuilder;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.common.ReturnStatusEnum;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.service.GlobalConfigService;
import com.welab.wefe.gateway.service.MessageService;
import com.welab.wefe.gateway.util.ActionProcessorMappingUtil;
import com.welab.wefe.gateway.util.GrpcUtil;
import com.welab.wefe.gateway.util.ReturnStatusUtil;
import com.welab.wefe.gateway.util.TransferMetaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provide forwarding service base class for metadata messages submitted by flow module or board module
 *
 * @author aaron.li
 **/
public abstract class AbstractSendTransferMetaService {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MessageService mMessageService;

    @Autowired
    private GlobalConfigService globalConfigService;

    /**
     * Forward metadata message
     *
     * @param transferMeta Metadata message to be forwarded
     * @return Forwarding results
     */
    public BasicMetaProto.ReturnStatus send(GatewayMetaProto.TransferMeta transferMeta) {

        // Handle the compatibility between the old version of action and the new version of processor
        transferMeta = actionMappingProcessor(transferMeta);
        // Check the validity of parameters
        BasicMetaProto.ReturnStatus returnStatus = checkCommonReqParam(transferMeta);
        if (!ReturnStatusUtil.ok(returnStatus)) {
            LOG.error("Illegal message parameter：{} ---> {}", TransferMetaUtil.toMessageString(transferMeta), returnStatus.getMessage());
            mMessageService.saveError("消息参数非法", returnStatus, transferMeta);
            return returnStatus;
        }

        // Set the receiver and sender of the message
        transferMeta = setMemberInfo(transferMeta);

        try {
            return doHandle(transferMeta);
        } catch (Exception e) {

            return ReturnStatusBuilder
                    .sysExc(
                            e.getMessage(),
                            transferMeta.getSessionId()
                    );
        }
    }

    /**
     * Forward cached messages
     *
     * @param transferMeta Metadata message to be forwarded
     * @return Forwarding results
     */
    public abstract BasicMetaProto.ReturnStatus doHandleCache(GatewayMetaProto.TransferMeta transferMeta);

    /**
     * Forward messages
     *
     * @param transferMeta Metadata message to be forwarded
     * @return Forwarding results
     */
    public abstract BasicMetaProto.ReturnStatus doHandle(GatewayMetaProto.TransferMeta transferMeta) throws StatusCodeWithException;


    /**
     * Push message to remote(This method is mainly provided for processor calls)
     *
     * @param transferMeta Metadata message to be forwarded
     * @return Forwarding results
     */
    public BasicMetaProto.ReturnStatus pushToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        // Push message
        BasicMetaProto.ReturnStatus returnStatus = GrpcUtil.pushToRemote(transferMeta);
        if (ReturnStatusUtil.ok(returnStatus)) {
            LOG.info("Message push succeeded: {}", TransferMetaUtil.toMessageString(transferMeta));
        } else {
            LOG.error("Message push failed：{} ---> {}", TransferMetaUtil.toMessageString(transferMeta), returnStatus.getMessage());
            String dstMemberName = transferMeta.getDst().getMemberName();
            mMessageService.saveError("向 " + dstMemberName + " 推送消息失败", returnStatus, transferMeta);
        }

        return returnStatus;
    }

    /**
     * Check the validity of request parameters
     */
    public BasicMetaProto.ReturnStatus checkCommonReqParam(GatewayMetaProto.TransferMeta transferMeta) {
        if (StringUtil.isEmpty(transferMeta.getSessionId())) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "Session id cannot be empty.", transferMeta.getSessionId());
        }

        // Old version of action field
        String action = transferMeta.getAction();
        // New version of processor field
        String processor = transferMeta.getProcessor();
        if (StringUtil.isEmpty(action) && StringUtil.isEmpty(processor)) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "Action or Processor one of them should not be empty.", transferMeta.getSessionId());
        }

        // If the action field is not empty and the processor field is empty, it means that it is a request submitted by an old version of the client,
        // and the action field shall prevail
        if (StringUtil.isNotEmpty(action) && StringUtil.isEmpty(processor)) {
            processor = ActionProcessorMappingUtil.getProcessorByAction(action);
            if (StringUtil.isEmpty(processor)) {
                return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "Action not found mapping Processor, Action name:" + action, transferMeta.getSessionId());
            }
            transferMeta = transferMeta.toBuilder().setProcessor(processor).build();
        }

        GatewayMetaProto.Member dstMember = transferMeta.getDst();
        if (null == dstMember || StringUtil.isEmpty(dstMember.getMemberId())) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "dst member id is cannot be empty.", transferMeta.getSessionId());
        }

        MemberCache memberCache = MemberCache.getInstance();
        MemberEntity dstMemberEntity = memberCache.get(dstMember.getMemberId());
        if (null == dstMemberEntity) {
            // Avoid the problem that the gateway starts first and the board module starts later,
            // but the gateway cache has not been updated and the member information cannot be found
            MemberCache.getInstance().refreshCacheById(dstMember.getMemberId());
            dstMemberEntity = memberCache.get(dstMember.getMemberId());
        }
        if (null == dstMemberEntity) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "成员id[" + dstMember.getMemberId() + "]不存在，请确认成员信息是否已同步到Union.", transferMeta.getSessionId());
        }
        if (memberCache.getSelfMember().getId().equals(dstMember.getMemberId())) {
            String intranetBaseUri = globalConfigService.getGatewayConfig().intranetBaseUri;
            if (!GrpcUtil.checkGatewayUriValid(intranetBaseUri)) {
                return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "请设置自己的网关内网地址,格式为 IP:PORT", transferMeta.getSessionId());
            }
        } else if (StringUtil.isEmpty(dstMemberEntity.getIp())) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "成员[" + dstMemberEntity.getName() + "]未设置网关公网地址.", transferMeta.getSessionId());
        }

        return ReturnStatusBuilder.ok(transferMeta.getSessionId());
    }


    /**
     * Set the receiver and sender of the message
     */
    protected GatewayMetaProto.TransferMeta setMemberInfo(GatewayMetaProto.TransferMeta transferMeta) {
        MemberCache memberCache = MemberCache.getInstance();
        GatewayMetaProto.Member dstMember = transferMeta.getDst();
        MemberEntity dstMemberEntity = memberCache.get(dstMember.getMemberId());
        String dstIp = dstMemberEntity.getIp();
        int dstPort = dstMemberEntity.getPort();
        if (memberCache.getSelfMember().getId().equals(dstMember.getMemberId())) {
            String intranetBaseUri = globalConfigService.getGatewayConfig().intranetBaseUri;
            dstIp = intranetBaseUri.split(":")[0];
            dstPort = Integer.parseInt(intranetBaseUri.split(":")[1]);
        }

        GatewayMetaProto.Member srcMember = GatewayMetaProto.Member.newBuilder()
                .setMemberId(memberCache.getSelfMember().getId())
                .setMemberName(memberCache.getSelfMember().getName())
                .build();

        GatewayMetaProto.TransferMeta.Builder builder = transferMeta.toBuilder();
        builder.setSrc(srcMember)
                .getDstBuilder()
                .setMemberName(dstMemberEntity.getName())
                .setEndpoint(EndpointBuilder.create(dstIp, dstPort));

        return builder.build();

    }


    /**
     * The action field of the old version is mapped to the processor field of the new version
     */
    private GatewayMetaProto.TransferMeta actionMappingProcessor(GatewayMetaProto.TransferMeta transferMeta) {
        // Old version of action field
        String action = transferMeta.getAction();
        // New version of processor field
        String processor = transferMeta.getProcessor();
        // If the action field is not empty and the processor field is empty, it means that it is a request submitted by an old version of the client,
        // and the action field shall prevail
        if (StringUtil.isNotEmpty(action) && StringUtil.isEmpty(processor)) {
            processor = ActionProcessorMappingUtil.getProcessorByAction(action);
            transferMeta = transferMeta.toBuilder().setProcessor(StringUtil.isEmpty(processor) ? "not_found_processor" : processor).build();
        }
        return transferMeta;
    }
}
