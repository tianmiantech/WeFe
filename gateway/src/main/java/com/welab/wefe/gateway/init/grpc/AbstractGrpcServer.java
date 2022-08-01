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

import io.grpc.ServerInterceptor;

import java.util.List;

public abstract class AbstractGrpcServer {

    /**
     * restart server
     */
    public abstract void restart();

    /**
     * Start daemon
     */
    protected abstract void blockUntilShutdown() throws InterruptedException;


    /**
     * Stop grpc service
     */
    protected abstract void stop();


    /**
     * The interceptor class is converted to the corresponding instance
     *
     * @param interceptors interceptor class list
     * @return Interceptor instance list
     */
    protected ServerInterceptor[] listToInstanceArray(List<Class<? extends ServerInterceptor>> interceptors) throws IllegalAccessException, InstantiationException {
        ServerInterceptor[] instanceArray = new ServerInterceptor[interceptors.size()];
        for (int i = 0; i < interceptors.size(); i++) {
            instanceArray[i] = interceptors.get(i).newInstance();
        }

        return instanceArray;
    }
}
