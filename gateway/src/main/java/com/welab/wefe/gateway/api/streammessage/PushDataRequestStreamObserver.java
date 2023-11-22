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

package com.welab.wefe.gateway.api.streammessage;

import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.service.processors.DsourceProcessor;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Streaming server interface
 *
 * @author aaron.li
 **/
public class PushDataRequestStreamObserver implements StreamObserver<GatewayMetaProto.TransferMeta> {
    private final Logger LOG = LoggerFactory.getLogger(PushDataRequestStreamObserver.class);

    private StreamObserver<GatewayMetaProto.TransferMeta> responseObserver;

    public PushDataRequestStreamObserver(StreamObserver<GatewayMetaProto.TransferMeta> responseObserver) {
        this.responseObserver = responseObserver;
    }

    @Override
    public void onNext(GatewayMetaProto.TransferMeta transferMeta) {
        try {

            // save db
            DsourceProcessor dsourceProcessor = GatewayServer.CONTEXT.getBean(DsourceProcessor.class);
            dsourceProcessor.recvStreamDateHandle(transferMeta);
            responseObserver.onNext(transferMeta.toBuilder().clearContent().setTransferStatus(GatewayMetaProto.TransferStatus.COMPLETE).build());
        } catch (Exception e) {
            LOG.error("PushDataRequestStreamObserver onNext called exception：", e);
            responseObserver.onNext(transferMeta.toBuilder().clearContent().setTransferStatus(GatewayMetaProto.TransferStatus.ERROR).build());
        }
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("Encountered error in pushData: ", throwable);
    }

    @Override
    public void onCompleted() {
        // Inform the client that data processing has been completed
        responseObserver.onCompleted();
    }
}
