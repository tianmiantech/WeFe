/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.gateway.sdk;

import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.cache.MemberCache;
import com.welab.wefe.gateway.config.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Union service tool class
 *
 * @author aaron.li
 **/
public class UnionHelper {
    private static final Logger LOG = LoggerFactory.getLogger(UnionHelper.class);

    private final static String RESP_CODE_SUCCESS = "0";

    /**
     * Query all member addresses
     */
    private static String QUERY_ALL_URL;

    public static String BASE_URL;


    static {
        try {
            ConfigProperties configProperties = GatewayServer.CONTEXT.getBean(ConfigProperties.class);
            BASE_URL = configProperties.getWefeUnionBaseUrl();
            BASE_URL = (BASE_URL.endsWith("/") ? BASE_URL : BASE_URL + "/");
            QUERY_ALL_URL = BASE_URL + "member/query_all";
        } catch (Exception e) {
            LOG.error("Failed to initialize private key：", e);
        }

    }


    /**
     * Send a request to the cloud to obtain all member data
     *
     * @param memberId Specify the member ID of the query. If it is blank, all member information will be queried
     */
    public static String getMembers(String memberId) throws Exception {
        try {
            JObject data = JObject.create();
            if (StringUtil.isNotEmpty(memberId)) {
                data.append("id", memberId);
            }
            data.append("includeLogo", false);
            String verifyMemberId = MemberCache.getInstance().getSelfMember().getId();
            HttpResponse httpResponse = HttpRequest.create(QUERY_ALL_URL)
                    .appendParameters(generateReqParam(verifyMemberId, data.toString()))
                    .closeLog()
                    .postJson();
            String responseBodyStr = httpResponse.getBodyAsString();
            if (StringUtil.isEmpty(responseBodyStr)) {
                throw new Exception("查询成员信息失败，httpCode：" + httpResponse.getCode() + ", httpMessage：" + httpResponse.getMessage());
            }
            JObject responseBody = JObject.create(responseBodyStr);
            String code = responseBody.getString("code");
            if (!RESP_CODE_SUCCESS.equals(code)) {
                throw new Exception("查询成员信息失败，code：" + code);
            }
            String dataStr = responseBody.getStringByPath("data.list");
            if (StringUtil.isEmpty(dataStr)) {
                throw new Exception("查询成员信息失败，业务数据为空");
            }

            return dataStr;
        } catch (Exception e) {
            LOG.error("Failed to query member information：", e);
            throw e;
        }
    }


    /**
     * Generate request parameters
     */
    private static JObject generateReqParam(String verifyMemberId, String data) throws Exception {
        return JObject.create()
                .append("sign", RSAUtil.sign(data, MemberCache.getInstance().getSelfMember().getPrivateKey(), "UTF-8"))
                .append("memberId", verifyMemberId)
                .append("data", data);
    }
}
