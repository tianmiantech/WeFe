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

package com.welab.wefe.gateway.api.service;

import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.api.service.proto.TransferServiceGrpc;
import com.welab.wefe.gateway.base.RpcServer;
import com.welab.wefe.gateway.interceptor.IpAddressWhiteListServerInterceptor;
import com.welab.wefe.gateway.service.base.AbstractRecvTransferMetaService;
import com.welab.wefe.gateway.service.base.AbstractSendTransferMetaService;
import com.welab.wefe.gateway.util.TransferMetaUtil;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The service interface provided to the client flow module or board module
 *
 * @author aaron.li
 **/
@RpcServer(interceptors = {IpAddressWhiteListServerInterceptor.class})
public class TransferGrpcServer extends TransferServiceGrpc.TransferServiceImplBase {

    private final Logger LOG = LoggerFactory.getLogger(TransferGrpcServer.class);

    @Autowired
    private AbstractRecvTransferMetaService mRecvTransferMetaService;

    @Autowired
    private AbstractSendTransferMetaService mSendTransferMetaService;

    /**
     * Send message to remote
     *
     * @param request          Message to be sent
     * @param responseObserver Remote response
     */
    @Override
    public void send(GatewayMetaProto.TransferMeta request, StreamObserver<BasicMetaProto.ReturnStatus> responseObserver) {
        LOG.info("TransferGrpcServer send called, request session id：{} data size kb: {}", request.getSessionId(), TransferMetaUtil.getKbSize(request));
        responseObserver.onNext(mSendTransferMetaService.send(request));
        responseObserver.onCompleted();
    }

    /**
     * Provides a service interface for the client(flow module or board module) to pull data
     */
    @Override
    public void recv(GatewayMetaProto.TransferMeta request, StreamObserver<GatewayMetaProto.TransferMeta> responseObserver) {
        LOG.info("TransferGrpcServer recv called, request：" + request.toString());
        responseObserver.onNext(mRecvTransferMetaService.recv(request));
        responseObserver.onCompleted();
    }

    /**
     * Provides a service interface for the client(flow module or board module) to check data status
     */
    @Override
    public void checkStatusNow(GatewayMetaProto.TransferMeta request, StreamObserver<GatewayMetaProto.TransferMeta> responseObserver) {
        LOG.info("TransferGrpcServer checkStatusNow called, request：" + request.toString());
        responseObserver.onNext(mRecvTransferMetaService.checkStatusNow(request));
        responseObserver.onCompleted();
    }
}
