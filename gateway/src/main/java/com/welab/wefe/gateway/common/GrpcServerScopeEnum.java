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

package com.welab.wefe.gateway.common;

/**
 * Usage scope of RPC service interface
 */
public enum GrpcServerScopeEnum {
    INTERNAL("Internal services;In principle, the service interface provided can only be called from the internal network"),
    EXTERNAL("External services;In principle, the provided service interface is provided for public network calls"),
    BOTH("Internal or External services;In principle, the service interface provided can be called from internal network or external network");

    private String desc;

    GrpcServerScopeEnum(String desc) {
        this.desc = desc;
    }
}
