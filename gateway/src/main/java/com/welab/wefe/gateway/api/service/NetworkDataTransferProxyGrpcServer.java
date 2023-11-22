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
import com.welab.wefe.gateway.api.service.proto.NetworkDataTransferProxyServiceGrpc;
import com.welab.wefe.gateway.api.streammessage.PushDataRequestStreamObserver;
import com.welab.wefe.gateway.base.RpcServer;
import com.welab.wefe.gateway.interceptor.AntiTamperServerInterceptor;
import com.welab.wefe.gateway.interceptor.SignVerifyServerInterceptor;
import com.welab.wefe.gateway.interceptor.SystemTimestampVerifyServerInterceptor;
import com.welab.wefe.gateway.service.base.AbstractRecvTransferMetaService;
import com.welab.wefe.gateway.util.TransferMetaUtil;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Transmission interface between gateway and gateway
 *
 * @author aaron.li
 **/
@RpcServer(interceptors = {AntiTamperServerInterceptor.class, SignVerifyServerInterceptor.class, SystemTimestampVerifyServerInterceptor.class})
public class NetworkDataTransferProxyGrpcServer extends NetworkDataTransferProxyServiceGrpc.NetworkDataTransferProxyServiceImplBase {
    private final Logger LOG = LoggerFactory.getLogger(NetworkDataTransferProxyGrpcServer.class);

    @Autowired
    private AbstractRecvTransferMetaService recvTransferMetaService;

    @Override
    public void push(GatewayMetaProto.TransferMeta request, StreamObserver<BasicMetaProto.ReturnStatus> responseObserver) {
        LOG.info("NetworkDataTransferProxyGrpcServer push called session id: {}, data size kb: {}", request.getSessionId(), TransferMetaUtil.getKbSize(request));
        responseObserver.onNext(recvTransferMetaService.doHandle(request));
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<GatewayMetaProto.TransferMeta> pushData(StreamObserver<GatewayMetaProto.TransferMeta> responseObserver) {
        return new PushDataRequestStreamObserver(responseObserver);
    }

}
