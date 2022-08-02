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

package com.welab.wefe.gateway.init.grpc;

import com.welab.wefe.gateway.common.RpcServerUseScopeEnum;
import io.netty.handler.ssl.SslContext;

import javax.net.ssl.SSLException;

/**
 * Grpc outer server
 */
public class GrpcOuterServer extends AbstractGrpcServer {
    public GrpcOuterServer(int port) {
        super(port);
    }

    @Override
    protected RpcServerUseScopeEnum useScope() {
        return RpcServerUseScopeEnum.OUTER;
    }

    @Override
    protected SslContext buildSslContext() throws SSLException {
        return null;
    }
}
