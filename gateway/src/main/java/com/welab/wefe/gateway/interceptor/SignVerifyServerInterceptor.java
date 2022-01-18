/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.gateway.interceptor;

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.common.GrpcConstant;
import com.welab.wefe.gateway.entity.MemberEntity;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server signature verification interceptor
 *
 * @author aaron.li
 **/
public class SignVerifyServerInterceptor extends AbstractServerInterceptor {
    private final Logger LOG = LoggerFactory.getLogger(SignVerifyServerInterceptor.class);
    /**
     * Valid duration of signature, unit: minutes
     */
    private static final long SIGN_VALID_DURATION = 5 * 60 * 1000L;
    /**
     * Anti replay attack UUID request record cache,Key：UUID,Value：Timestamp
     */
    private static final ConcurrentHashMap<String, Long> UUID_CACHE = new ConcurrentHashMap<>();


    @Override
    protected <ReqT, RespT> ServerCall.Listener<ReqT> intercept(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        // Remote IP address
        String remoteIpAddr = getClientIpAddr(call);
        // Signature information submitted by the client
        String signInfo = headers.get(GrpcConstant.SIGN_HEADER_KEY);
        if (!signVerify(signInfo)) {
            LOG.error("SignVerifyServerInterceptor called, Client IP: {}, sign info: {}, sign verify fail，close the connection.", remoteIpAddr, signInfo);
            saveSignVerifyFailInfo(remoteIpAddr);
            call.close(Status.UNAUTHENTICATED, headers);
            setReqInvalid(headers);
        }

        return next.startCall(call, headers);
    }


    /**
     * Verify signature
     *
     * @param signInfo Signature information submitted by the client
     * @return false：Signature verification failed; true：Signature verification successful
     */
    private boolean signVerify(String signInfo) {
        if (StringUtil.isEmpty(signInfo)) {
            LOG.error("No signature information was received from the client. Signature verification failed. If the server uses nginx for forwarding, please make sure nginx option [underscores_in_headers] value is [on]");
            return false;
        }
        JObject signInfoObj = null;
        JObject dataObj = null;
        String sign = null;
        try {
            signInfoObj = JObject.create(signInfo);
            // Received with escape characters\
            dataObj = JObject.create(signInfoObj.getString(GrpcConstant.SIGN_KEY_DATA).replace("\\", ""));
            sign = signInfoObj.getString(GrpcConstant.SIGN_KEY_SIGN);
        } catch (Exception e) {
            LOG.error("The client's signature information conversion JSON exception, signature verification failed：", e);
            return false;
        }
        if (null == dataObj || dataObj.isEmpty() || StringUtil.isEmpty(sign)) {
            LOG.error("The signature field of the client's signature information is invalid. Signature verification failed");
            return false;
        }

        String memberId = dataObj.getString(GrpcConstant.SIGN_KEY_MEMBER_ID);
        String timestampStr = dataObj.getString(GrpcConstant.SIGN_KEY_TIMESTAMP);
        String uuid = dataObj.getString(GrpcConstant.SIGN_KEY_UUID);
        if (StringUtil.isEmpty(memberId) || StringUtil.isEmpty(timestampStr) || StringUtil.isEmpty(uuid)) {
            LOG.error("The signature field of the client's signature information is invalid. Signature verification failed");
            return false;
        }

        MemberEntity memberEntity = MemberCache.getInstance().get(memberId);
        if (null == memberEntity) {
            LOG.error("Invalid member ID, signature verification failed");
            return false;
        }

        long timestamp = NumberUtils.toLong(timestampStr, 0);
        if ((System.currentTimeMillis() - timestamp) > SIGN_VALID_DURATION) {
            LOG.error("The signature information of the client has expired. Signature verification failed");
            return false;
        }

        // If it is not empty, it is considered a replay attack
        Long uuidTimestamp = UUID_CACHE.get(uuid);
        if (null != uuidTimestamp) {
            LOG.error("The data repeatedly submitted by the client is received, and the signature verification fails, member id: " + memberId);
            return false;
        }
        // Add new request records to the cache and clean up expired request records
        updateExpireUUIDCache(uuid);

        try {
            byte[] data = signInfoObj.getString(GrpcConstant.SIGN_KEY_DATA).getBytes(StandardCharsets.UTF_8.toString());
            return SignUtil.verify(data, memberEntity.getPublicKey(), sign, memberEntity.getSecretKeyType());
        } catch (Exception e) {
            LOG.error("Signature verification exception：", e);
        }
        return false;
    }

    private void updateExpireUUIDCache(String uuid) {
        UUID_CACHE.entrySet().removeIf(entry -> (System.currentTimeMillis() - entry.getValue()) > SIGN_VALID_DURATION);
        UUID_CACHE.put(uuid, System.currentTimeMillis());
    }
}
