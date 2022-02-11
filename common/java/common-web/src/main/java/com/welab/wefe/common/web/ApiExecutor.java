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

package com.welab.wefe.common.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.SamplingLogger;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fastjson.LoggerSerializeConfig;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api.base.FlowLimitByIp;
import com.welab.wefe.common.web.api.base.FlowLimitByMobile;
import com.welab.wefe.common.web.dto.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * The entry class that calls the API
 *
 * @author Zane
 */
public class ApiExecutor {
    protected static final Logger LOG = LoggerFactory.getLogger(ApiExecutor.class);
    protected static final SamplingLogger SLOG;

    private static final String REQUEST_FROM_REFRESH = "request-from-refresh";

    static {
        SLOG = new SamplingLogger(LOG, 1000, TimeSpan.MINUTE);
    }

    /**
     * Implement the API
     */
    public static ApiResult<?> execute(HttpServletRequest httpServletRequest, long start, String apiName, JSONObject params, MultiValueMap<String, MultipartFile> files) {

        MDC.put("requestId", start + "");

        AbstractApi<?, ?> api;
        String apiPath = apiName.toLowerCase();


        try {
            api = Launcher.CONTEXT.getBean(apiPath, AbstractApi.class);

        } catch (BeansException ex) {
            return ApiResult.ofErrorWithStatusCode(StatusCode.REQUEST_API_NOT_FOUND, "接口不存在：" + apiPath);
        }

        Api annotation = api.getClass().getAnnotation(Api.class);
        switch (annotation.logLevel()) {
            case "debug":
                LOG.debug("request({}):{}", apiPath, params.toString());
                break;
            default:
                LOG.info("request({}):{}", apiPath, params);
        }
        ApiResult<?> result = null;
        try {

            // Checking login Status
            checkSessionToken(api, annotation);
            // Check the permissions
            checkApiPermission(api, annotation, params);

            // IP flow control check
            checkFlowLimitByIp(httpServletRequest, api, params);
            // Check mobile phone number flow control
            checkFlowLimitByMobile(httpServletRequest, api, params);

            // Doing things before the API is executed
            if (Launcher.BEFORE_API_EXECUTE_FUNCTION != null) {
                Launcher.BEFORE_API_EXECUTE_FUNCTION.action(api, params);
            }
            // Implement the API
            result = api.execute(httpServletRequest.getMethod(), params, httpServletRequest, files);

        } catch (StatusCodeWithException e) {
            result = api.fail(e.getStatusCode().getCode(), e.getMessage());
        } catch (Exception e) {
            result = api.fail(StatusCode.SYSTEM_ERROR.getCode(), e.getMessage());
        } finally {
            // Doing things after the API is executed
            if (Launcher.AFTER_API_EXECUTE_FUNCTION != null) {
                Launcher.AFTER_API_EXECUTE_FUNCTION.action(httpServletRequest, start, api, params, result);
            }

            if (result == null) {
                result = api.fail(StatusCode.SYSTEM_ERROR.getCode(), "响应失败，疑似程序中发生了死循环。");
            }
            result.spend = System.currentTimeMillis() - start;

            logResponse(annotation, result);

            MDC.clear();
        }

        result.spend = System.currentTimeMillis() - start;

        return result;
    }

    private static void logResponse(Api annotation, ApiResult<?> result) {

        String content = "";
        /**
         * 警告 ⚠️:
         * 当响应内容为 ResponseEntity<FileSystemResource> 时
         * JSON.toJSONString(result) 序列化时会导致文件被置空
         * 所以这里写日志时需要进行检查，避免对 FileSystemResource 进行 json 序列化。
         */
        if (result.data instanceof ResponseEntity) {
            Object body = ((ResponseEntity) result.data).getBody();
            if (body instanceof FileSystemResource) {
                FileSystemResource fileSystemResource = (FileSystemResource) body;
                content = "File:" + fileSystemResource.getPath();
            }
        } else if (result.data instanceof byte[]) {
            byte[] bytes = (byte[]) result.data;
            content = "bytes(length " + bytes.length + ")";
        } else {
            content = JSON.toJSONString(result, LoggerSerializeConfig.instance());
        }


        switch (annotation.logLevel()) {
            case "debug":
                LOG.debug("response({}):{}", annotation.path(), content);
                break;
            default:
                LOG.info("response({}):{}", annotation.path(), content);

        }
    }

    /**
     * Check API access permissions
     */
    private static void checkApiPermission(AbstractApi<?, ?> api, Api annotation, JSONObject params) throws Exception {
        // If the permission check method is not set, the permission check is not performed.
        if (Launcher.API_PERMISSION_POLICY == null) {
            return;
        }

        Launcher.API_PERMISSION_POLICY.check(api, annotation, params);
    }

    /**
     * Checking login Status
     */
    private static void checkSessionToken(AbstractApi<?, ?> api, Api annotation) throws StatusCodeWithException {

        // Skip interfaces that do not require login
        if (!annotation.login()) {
            return;
        }

        // If the login status check method is not set, the check is not performed.
        if (Launcher.CHECK_SESSION_TOKEN_FUNCTION == null) {
            return;
        }

        // Checking token Validity
        String token = CurrentAccount.token();
        if (StringUtil.isEmpty(token) || !Launcher.CHECK_SESSION_TOKEN_FUNCTION.check(api, annotation, token)) {
            throw new StatusCodeWithException("请登录后访问", StatusCode.LOGIN_REQUIRED);
        }
    }

    /**
     * IP flow control check
     */
    private static void checkFlowLimitByIp(HttpServletRequest httpServletRequest, AbstractApi<?, ?> api, JSONObject params) throws Exception {
        if (null == Launcher.FLOW_LIMIT_BY_IP_FUNCTION) {
            return;
        }
        FlowLimitByIp flowLimitByIp = api.getClass().getAnnotation(FlowLimitByIp.class);
        if (null == flowLimitByIp || flowLimitByIp.count() < 0 || flowLimitByIp.second() < 0) {
            return;
        }
        Launcher.FLOW_LIMIT_BY_IP_FUNCTION.check(httpServletRequest, api, params);
    }

    /**
     * Check mobile phone number flow control
     */
    private static void checkFlowLimitByMobile(HttpServletRequest httpServletRequest, AbstractApi<?, ?> api, JSONObject params) throws Exception {
        if (null == Launcher.FLOW_LIMIT_BY_MOBILE_FUNCTION || null == params || StringUtil.isEmpty(params.getString("mobile"))) {
            return;
        }
        FlowLimitByMobile flowLimitByMobile = api.getClass().getAnnotation(FlowLimitByMobile.class);
        if (null == flowLimitByMobile || flowLimitByMobile.count() < 0 || flowLimitByMobile.second() < 0) {
            return;
        }
        Launcher.FLOW_LIMIT_BY_MOBILE_FUNCTION.check(httpServletRequest, api, params);
    }
}
