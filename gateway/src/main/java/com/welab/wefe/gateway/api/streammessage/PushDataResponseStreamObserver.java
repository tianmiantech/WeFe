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

import com.google.common.util.concurrent.SettableFuture;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.service.base.AbstractTransferMetaDataSource;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Streaming client interface
 *
 * @author aaron.li
 **/
public class PushDataResponseStreamObserver implements StreamObserver<GatewayMetaProto.TransferMeta> {
    private final Logger LOG = LoggerFactory.getLogger(PushDataResponseStreamObserver.class);

    private SettableFuture<Void> finishFuture;
    private AbstractTransferMetaDataSource.AsyncResponseCollector asyncResponseCollector;

    public PushDataResponseStreamObserver(SettableFuture<Void> finishFuture, AbstractTransferMetaDataSource.AsyncResponseCollector asyncResponseCollector) {
        this.finishFuture = finishFuture;
        this.asyncResponseCollector = asyncResponseCollector;
    }

    @Override
    public void onNext(GatewayMetaProto.TransferMeta transferMeta) {
        transferMeta = transferMeta.toBuilder().clearContent().build();
        if (GatewayMetaProto.TransferStatus.COMPLETE.equals(transferMeta.getTransferStatus())) {
            asyncResponseCollector.getSuccessList().add(transferMeta);
        } else {
            // Collect error request messages
            asyncResponseCollector.getFailedList().add(transferMeta);
        }
    }

    @Override
    public void onError(Throwable t) {
        LOG.error("push data error: ", t);
        onCompleted();
    }

    @Override
    public void onCompleted() {
        finishFuture.set(null);
    }
}
