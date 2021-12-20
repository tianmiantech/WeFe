/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.sdk;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.union.MemberListApi;
import com.welab.wefe.board.service.api.union.MemberRealnameAuthApi;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.dto.globalconfig.MemberInfoModel;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.SmsBusinessType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpContentType;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.UrlUtil;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


/**
 * @author Zane
 */
public abstract class AbstractUnionService extends AbstractService {

    /**
     * cache
     */
    protected static final ExpiringMap<String, Object> CACHE_MAP = ExpiringMap
            .builder()
            .expiration(60, TimeUnit.SECONDS)
            .maxSize(500)
            .build();

    @Autowired
    protected Config config;

    @Autowired
    protected GlobalConfigService globalConfigService;

    /**
     * initialize wefe system
     */
    public void initializeSystem(MemberInfoModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", model.getMemberId())
                .put("member_id", model.getMemberId())
                .put("name", model.getMemberName())
                .put("mobile", model.getMemberMobile())
                .put("allow_open_data_set", model.getMemberAllowPublicDataSet())
                .put("public_key", model.getRsaPublicKey())
                .put("email", model.getMemberEmail())
                .put("gateway_uri", model.getMemberGatewayUri())
                .put("logo", model.getMemberLogo())
                .put("hidden", model.getMemberHidden());

        request("member/add", params, false);
    }

    /**
     * Report member information
     */
    public void uploadMemberInfo(MemberInfoModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", model.getMemberId())
                .put("name", model.getMemberName())
                .put("mobile", model.getMemberMobile())
                .put("allow_open_data_set", model.getMemberAllowPublicDataSet())
                .put("public_key", model.getRsaPublicKey())
                .put("email", model.getMemberEmail())
                .put("gateway_uri", model.getMemberGatewayUri())
                .put("logo", model.getMemberLogo())
                .put("hidden", model.getMemberHidden());

        request("member/update", params);
    }


    /**
     * Reset key
     */
    public void resetPublicKey(MemberInfoModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", model.getMemberId())
                .put("public_key", model.getRsaPublicKey());

        request("member/update_public_key", params);
    }

    /**
     * Update member information (not including logo)
     */
    public void uploadMemberInfoExcludeLogo(MemberInfoModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", model.getMemberId())
                .put("name", model.getMemberName())
                .put("mobile", model.getMemberMobile())
                .put("allow_open_data_set", model.getMemberAllowPublicDataSet())
                .put("public_key", model.getRsaPublicKey())
                .put("email", model.getMemberEmail())
                .put("gateway_uri", model.getMemberGatewayUri())
                .put("hidden", model.getMemberHidden());

        request("member/update_exclude_logo", params);
    }

    /**
     * Update member information logo
     */
    public void updateMemberLogo(MemberInfoModel model) throws StatusCodeWithException {
        JObject params = JObject
                .create()
                .put("id", model.getMemberId())
                .put("logo", model.getMemberLogo());

        request("member/update_logo", params);
    }

    /**
     * Pagination query member
     */
    public synchronized JSONObject queryMembers(MemberListApi.Input input) throws StatusCodeWithException {

        String key = "queryMembers" + JSON.toJSONString(input);
        if (CACHE_MAP.containsKey(key)) {
            return (JSONObject) CACHE_MAP.get(key);
        }

        JObject params = JObject
                .create()
                .put("page_index", input.getPageIndex())
                .put("page_size", input.getPageSize())
                .put("name", input.getName())
                .put("id", input.getId());

        JSONObject response = request("member/query", params);
        CACHE_MAP.put(key, response);
        return response;
    }

    public JSONObject queryMemberById(String id) throws StatusCodeWithException {
        return queryMember(id, "");
    }

    public JSONObject queryMember(String id, String name) throws StatusCodeWithException {
        return queryMemberByPage(0, 0, id, name);
    }

    public JSONObject queryMember(int pageIndex, int pageSize) throws StatusCodeWithException {
        return queryMemberByPage(pageIndex, pageSize, "", "");
    }

    public JSONObject queryMemberByPage(int pageIndex, int pageSize, String id, String name) throws StatusCodeWithException {
        JObject params = JObject.create()
                .put("page_index", pageIndex)
                .put("page_size", pageSize);

        if (StringUtil.isNotEmpty(id)) {
            params.put("id", id);
        }

        if (StringUtil.isNotEmpty(name)) {
            params.put("name", name);
        }

        return request("member/query", params);
    }

    public void sendVerificationCode(String mobile, SmsBusinessType smsBusinessType) throws StatusCodeWithException {
        if (!StringUtil.checkPhoneNumber(mobile)) {
            throw new StatusCodeWithException("非法的手机号", StatusCode.PARAMETER_VALUE_INVALID);
        }
        JObject params = JObject.create()
                .append("mobile", mobile)
                .append("smsBusinessType", smsBusinessType);
        try {
            request("sms/send_verification_code", params, true);
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(getUnionOrigExceptionMsg(e), StatusCode.SYSTEM_ERROR);
        } catch (Exception e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * Check verification code
     */
    public void checkVerificationCode(String mobile, String code, SmsBusinessType smsBusinessType) throws StatusCodeWithException {
        JObject params = JObject.create()
                .append("mobile", mobile)
                .append("code", code)
                .append("smsBusinessType", smsBusinessType);
        try {
            request("sms/check_verification_code", params, true);
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(getUnionOrigExceptionMsg(e), StatusCode.SYSTEM_ERROR);
        } catch (Exception e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    private String getUnionOrigExceptionMsg(StatusCodeWithException e) {
        String errorMsg = e.getMessage();
        if (StringUtil.isNotEmpty(errorMsg)) {
            int index = errorMsg.indexOf("：");
            if (index != -1) {
                errorMsg = errorMsg.substring(index + 1);
            }
        }
        return errorMsg;
    }

    public JSONObject request(String api, JSONObject params) throws StatusCodeWithException {
        return request(api, params, true);
    }

    protected JSONObject request(String api, JSONObject params, boolean needSign) throws StatusCodeWithException {
        /**
         * Prevent the map from being out of order, causing the verification to fail.
         */
        params = new JSONObject(new TreeMap(params));

        String data = params.toJSONString();

        // rsa signature
        if (needSign) {
            String sign = null;
            try {
                sign = RSAUtil.sign(data, CacheObjects.getRsaPrivateKey(), "UTF-8");
            } catch (Exception e) {
                throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
            }


            JSONObject body = new JSONObject();
            body.put("member_id", CacheObjects.getMemberId());
            body.put("sign", sign);
            body.put("data", data);

            data = body.toJSONString();
        }

        HttpResponse response = HttpRequest
                .create(config.getUNION_BASE_URL() + "/" + api)
                .setBody(data)
                .postJson();

        if (!response.success()) {
            throw new StatusCodeWithException(response.getMessage(), StatusCode.RPC_ERROR);
        }

        JSONObject json;
        try {
            json = response.getBodyAsJson();
        } catch (JSONException e) {
            throw new StatusCodeWithException("union 响应失败：" + response.getBodyAsString(), StatusCode.RPC_ERROR);
        }

        if (json == null) {
            throw new StatusCodeWithException("union 响应失败：" + response.getBodyAsString(), StatusCode.RPC_ERROR);
        }

        Integer code = json.getInteger("code");
        if (code == null || !code.equals(0)) {
            throw new StatusCodeWithException("union 响应失败(" + code + ")：" + json.getString("message"), StatusCode.RPC_ERROR);
        }
        return json;
    }


    public JSONObject queryMemberAuthTypeList() throws StatusCodeWithException {
        return request("member/authtype/query", JObject.create(), true);
    }

    public JSONObject realnameAuth(MemberRealnameAuthApi.Input input) throws StatusCodeWithException {
        return request("member/realname/auth", JObject.create(input), true);
    }

    public JSONObject realnameAuthInfoQuery() throws StatusCodeWithException {
        return request("member/realname/authInfo/query", JObject.create(), true);
    }


    public JSONObject realnameAuthAgreementTemplateQuery() throws StatusCodeWithException {
        return request("realname/auth/agreement/template/query", JObject.create(), true);
    }

    public JSONObject uploadFile(MultiValueMap<String, MultipartFile> files, JObject params) throws StatusCodeWithException {

        return request("member/file/upload", params, files, true);
    }

    private JSONObject request(String api, JSONObject params, MultiValueMap<String, MultipartFile> files, boolean needSign) throws StatusCodeWithException {
        /**
         * Prevent the map from being out of order, causing the verification to fail.
         */
        params = new JSONObject(new TreeMap(params));

        String data = params.toJSONString();
        String sign = null;
        // rsa signature
        JSONObject body = new JSONObject();
        if (needSign) {
            try {
                sign = RSAUtil.sign(data, CacheObjects.getRsaPrivateKey(), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
                throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
            }


            body.put("member_id", CacheObjects.getMemberId());
            body.put("sign", sign);
            body.put("data", data);

            data = body.toJSONString();
        }
        HttpResponse response;
        String url = config.getUNION_BASE_URL() + "/" + api;
        // send http request without files
        if (files == null) {
            response = HttpRequest
                    .create(url)
                    .setBody(data)
                    .postJson();
        }
        // send http request with files
        else {
            url = UrlUtil.appendQueryParameters(url, body);
            HttpRequest request = HttpRequest
                    .create(url)
                    .setContentType(HttpContentType.MULTIPART);

            for (Map.Entry<String, MultipartFile> item : files.toSingleValueMap().entrySet()) {
                try {
                    MultipartFile file = item.getValue();
                    ContentType contentType = StringUtil.isEmpty(file.getContentType())
                            ? ContentType.DEFAULT_BINARY
                            : ContentType.create(file.getContentType());

                    InputStreamBody streamBody = new InputStreamBody(
                            file.getInputStream(),
                            contentType,
                            file.getOriginalFilename()
                    );


                    request.appendParameter(item.getKey(), streamBody);
                } catch (IOException e) {
                    StatusCode.FILE_IO_ERROR.throwException(e);
                }
            }

            response = request.post();
        }


        if (!response.success()) {
            throw new StatusCodeWithException(response.getMessage(), StatusCode.RPC_ERROR);
        }

        JSONObject json;
        try {
            json = response.getBodyAsJson();
        } catch (JSONException e) {
            throw new StatusCodeWithException("union 响应失败：" + response.getBodyAsString(), StatusCode.RPC_ERROR);
        }

        if (json == null) {
            throw new StatusCodeWithException("union 响应失败：" + response.getBodyAsString(), StatusCode.RPC_ERROR);
        }

        Integer code = json.getInteger("code");
        if (code == null || !code.equals(0)) {
            throw new StatusCodeWithException("union 响应失败(" + code + ")：" + json.getString("message"), StatusCode.RPC_ERROR);
        }
        return json;
    }

}
