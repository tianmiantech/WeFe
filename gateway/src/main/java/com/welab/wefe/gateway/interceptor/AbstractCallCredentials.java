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
package com.welab.wefe.gateway.interceptor;

import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import io.grpc.CallCredentials;
import io.grpc.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

public abstract class AbstractCallCredentials extends CallCredentials {
    protected Logger LOG = LoggerFactory.getLogger(this.getClass());

    private GatewayMetaProto.TransferMeta transferMeta;
    private List<AbstractMetadataBuilder> metadataBuilders = new ArrayList<>();


    public AbstractCallCredentials(GatewayMetaProto.TransferMeta transferMeta, AbstractMetadataBuilder... metadataBuilders) {
        this.transferMeta = transferMeta;
        if (null != metadataBuilders) {
            this.metadataBuilders = Arrays.asList(metadataBuilders);
        }
    }


    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
        Metadata headers = new Metadata();

        for (AbstractMetadataBuilder metadataBuilder : this.metadataBuilders) {
            headers.merge(metadataBuilder.build());
        }
        metadataApplier.apply(headers);
    }

    @Override
    public void thisUsesUnstableApi() {

    }

    public GatewayMetaProto.TransferMeta getTransferMeta() {
        return transferMeta;
    }
}
