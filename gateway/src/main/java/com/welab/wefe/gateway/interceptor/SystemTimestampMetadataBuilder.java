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
import com.welab.wefe.gateway.common.GrpcConstant;
import io.grpc.Metadata;

public class SystemTimestampMetadataBuilder extends AbstractMetadataBuilder {

    public SystemTimestampMetadataBuilder(GatewayMetaProto.TransferMeta transferMeta) {
        super(transferMeta);
    }

    @Override
    public Metadata build() {
        Metadata metadata = new Metadata();
        metadata.put(GrpcConstant.SYSTEM_TIMESTAMP_HEADER_KEY, System.currentTimeMillis() + "");
        return metadata;
    }

}
