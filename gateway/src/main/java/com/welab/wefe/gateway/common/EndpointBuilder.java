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

import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;

/**
 * @author aaron.li
 **/
public class EndpointBuilder {

    public static BasicMetaProto.Endpoint create(String ip, int port) {
        return BasicMetaProto.Endpoint.newBuilder()
                .setIp(ip)
                .setPort(port)
                .build();
    }

    public static BasicMetaProto.Endpoint create(String uri) {
        return BasicMetaProto.Endpoint.newBuilder()
                .setIp(uri.split(":")[0])
                .setPort(Integer.parseInt(uri.split(":")[1]))
                .build();
    }

    public static String endpointToUri(BasicMetaProto.Endpoint endpoint) {
        return generateUri(endpoint);
    }

    public static String generateUri(BasicMetaProto.Endpoint endpoint) {
        if (null == endpoint) {
            return "";
        }
        return generateUri(endpoint.getIp(), endpoint.getPort());
    }

    public static String generateUri(String ip, int port) {
        return ip + ":" + port;
    }
}
