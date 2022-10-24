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

package com.welab.wefe.gateway.cache;

import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.common.EndpointBuilder;
import com.welab.wefe.gateway.util.GrpcUtil;
import io.grpc.ManagedChannel;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import javax.net.ssl.SSLException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * Grpc channel cache
 */
public class GrpcChannelCache {
    private final static Object LOCK = new Object();
    private static GrpcChannelCache CHANNEL_CACHE = new GrpcChannelCache();
    /**
     * KEY:网关URI，格式为 host:port
     */
    private ExpiringMap<String, ChannelInfo> data = ExpiringMap
            .builder()
            .expirationListener(new MyExpirationListener())
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(24, TimeUnit.HOURS)
            .build();

    private GrpcChannelCache() {
    }

    public static GrpcChannelCache getInstance() {
        return CHANNEL_CACHE;
    }

    public ManagedChannel getNonNull(String key, boolean tlsEnable, X509Certificate[] x509Certificates) throws SSLException {
        if (StringUtil.isEmpty(key)) {
            return null;
        }
        ChannelInfo channelInfo = data.get(key);
        if (null != channelInfo && channelInfo.tlsEnable == tlsEnable) {
            return channelInfo.channel;
        }
        synchronized (LOCK) {
            channelInfo = data.get(key);
            if (null != channelInfo && channelInfo.tlsEnable == tlsEnable) {
                return channelInfo.channel;
            }
            // 删除旧的
            remove(key);
            ManagedChannel channel = null;
            if (!tlsEnable) {
                channel = GrpcUtil.getManagedChannel(EndpointBuilder.create(key));
            } else {
                channel = GrpcUtil.getSslManagedChannel(EndpointBuilder.create(key), x509Certificates);
            }
            channelInfo = new ChannelInfo();
            channelInfo.setChannel(channel);
            channelInfo.setTlsEnable(tlsEnable);
            data.put(key, channelInfo);
            return channelInfo.channel;
        }
    }

    public void remove(String key) {
        ChannelInfo channelInfo = data.remove(key);
        GrpcUtil.closeManagedChannel(null != channelInfo ? channelInfo.channel : null);
    }


    private static class MyExpirationListener implements ExpirationListener {

        @Override
        public void expired(Object key, Object value) {
            if (null == value) {
                return;
            }
            ChannelInfo channelInfo = (ChannelInfo) value;
            GrpcUtil.closeManagedChannel(channelInfo.channel);
        }
    }


    public static class ChannelInfo {
        private ManagedChannel channel;
        private boolean tlsEnable;

        public ManagedChannel getChannel() {
            return channel;
        }

        public void setChannel(ManagedChannel channel) {
            this.channel = channel;
        }

        public boolean isTlsEnable() {
            return tlsEnable;
        }

        public void setTlsEnable(boolean tlsEnable) {
            this.tlsEnable = tlsEnable;
        }
    }
}
