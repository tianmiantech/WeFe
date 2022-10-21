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
    private MessageService messageService;

    @Autowired
    private GlobalConfigService globalConfigService;

    /**
     * Forward metadata message
     *
     * @param transferMeta Metadata message to be forwarded
     * @return Forwarding results
     */
    public BasicMetaProto.ReturnStatus send(GatewayMetaProto.TransferMeta transferMeta) {
        // Check the validity of parameters
        BasicMetaProto.ReturnStatus returnStatus = checkCommonReqParam(transferMeta);
        if (!ReturnStatusUtil.ok(returnStatus)) {
            LOG.error("Illegal message parameter：{} ---> {}", TransferMetaUtil.toMessageString(transferMeta), returnStatus.getMessage());
            messageService.saveError("消息参数非法", returnStatus, transferMeta);
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
            messageService.saveError("向 " + dstMemberName + " 推送消息失败", returnStatus, transferMeta);
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

        // New version of processor field
        if (StringUtil.isEmpty(transferMeta.getProcessor())) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "Processor should not be empty.", transferMeta.getSessionId());
        }

        GatewayMetaProto.Member dstMember = transferMeta.getDst();
        if (StringUtil.isEmpty(dstMember.getMemberId())) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "dst member id is cannot be empty.", transferMeta.getSessionId());
        }

        MemberCache memberCache = MemberCache.getInstance();
        MemberEntity selfMemberEntity = memberCache.getSelfMember();
        if (!GrpcUtil.checkGatewayUriValid(selfMemberEntity.getGatewayInternalUri())) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "请设置自己的网关内网地址,格式为 HOST:PORT", transferMeta.getSessionId());
        }
        if (!GrpcUtil.checkGatewayUriValid(selfMemberEntity.getGatewayExternalUri())) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "请设置自己的网关外网地址,格式为 HOST:PORT", transferMeta.getSessionId());
        }

        MemberEntity dstMemberEntity = memberCache.get(dstMember.getMemberId());
        // Avoid the problem that the gateway starts first and the board module starts later,
        // but the gateway cache has not been updated and the member information cannot be found
        dstMemberEntity = (null == dstMemberEntity ? MemberCache.getInstance().refreshCacheById(dstMember.getMemberId()) : dstMemberEntity);
        if (null == dstMemberEntity) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "成员id[" + dstMember.getMemberId() + "]不存在，请确认成员信息是否已同步到Union.", transferMeta.getSessionId());
        }

        if (StringUtil.isEmpty(dstMemberEntity.getGatewayExternalUri())) {
            return ReturnStatusBuilder.create(ReturnStatusEnum.PARAM_ERROR.getCode(), "成员[" + dstMemberEntity.getName() + "]未设置网关公网地址.", transferMeta.getSessionId());
        }

        return ReturnStatusBuilder.ok(transferMeta.getSessionId());
    }


    /**
     * Set the receiver and sender of the message
     */
    protected GatewayMetaProto.TransferMeta setMemberInfo(GatewayMetaProto.TransferMeta transferMeta) {
        MemberCache memberCache = MemberCache.getInstance();
        GatewayMetaProto.Member srcMember = GatewayMetaProto.Member.newBuilder()
                .setMemberId(memberCache.getSelfMember().getId())
                .setMemberName(memberCache.getSelfMember().getName())
                .build();

        GatewayMetaProto.TransferMeta.Builder builder = transferMeta.toBuilder();
        return builder.setSrc(srcMember).setDst(setDstMemberInfo(transferMeta)).build();
    }

    /**
     * 设置目的成员信息
     */
    private GatewayMetaProto.Member setDstMemberInfo(GatewayMetaProto.TransferMeta transferMeta) {
        MemberCache memberCache = MemberCache.getInstance();
        GatewayMetaProto.Member dstMember = transferMeta.getDst();
        MemberEntity dstMemberEntity = memberCache.get(dstMember.getMemberId());
        dstMember = dstMember.toBuilder().setMemberName(dstMemberEntity.getName()).build();
        BasicMetaProto.Endpoint dstEndpoint = dstMember.getEndpoint();
        // 目的地址使用指定值
        if (StringUtil.isNotEmpty(dstEndpoint.getIp())) {
            return dstMember;
        }

        MemberEntity selfMemberEntity = memberCache.getSelfMember();
        if (selfMemberEntity.getId().equals(dstMember.getMemberId())) {
            dstEndpoint = EndpointBuilder.create(selfMemberEntity.getGatewayInternalUri());
        } else {
            dstEndpoint = EndpointBuilder.create(dstMemberEntity.getGatewayExternalUri());
        }

        return dstMember.toBuilder().setEndpoint(dstEndpoint).build();
    }

}
