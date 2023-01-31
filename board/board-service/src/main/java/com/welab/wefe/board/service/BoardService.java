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

package com.welab.wefe.board.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.base.OnlineDemoApi;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.SignedApiInput;
import com.welab.wefe.common.web.service.flowlimit.FlowLimitByIpService;
import com.welab.wefe.common.web.service.flowlimit.FlowLimitByMobileService;
import com.welab.wefe.common.wefe.checkpoint.CheckpointManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.charset.StandardCharsets;

/**
 * @author hunter.zhao
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@ComponentScan(
        lazyInit = true,
        nameGenerator = ApiBeanNameGenerator.class,
        basePackageClasses = {
                BoardService.class,
                Launcher.class,
                CheckpointManager.class
        }
)
public class BoardService implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(BoardService.class);

    public static void main(String[] args) {
        Launcher
                .instance()
                //.apiLogger(new BoardApiLogger())
                .apiPackageClass(BoardService.class)
                // 禁止未登录且无验签的访问
                //.checkSessionTokenFunction((api, annotation, token) -> CurrentAccount.get() != null || annotation.allowAccessWithSign())
                .onApiExceptionFunction((api, e) -> {

                    // When an exception occurs in a node,
                    // the corresponding nodeId is told to the front end to facilitate friendly prompts by the front end.
                    if (e instanceof FlowNodeException) {
                        FlowNodeException flowNodeException = (FlowNodeException) e;
                        JObject info = JObject
                                .create()
                                .put("node_id", flowNodeException.getNode().getNodeId());

                        ApiResult<JObject> response = new ApiResult<>();
                        response.code = flowNodeException.getStatusCode().getCode();
                        response.message = e.getMessage();
                        response.data = info;
                        return response;

                    }

                    throw e;
                })
                .apiPermissionPolicy((api, annotation, params) -> {

                    // 在线体验版专用 api 权限检查
                    OnlineDemoApi onlineDemoApi = api.getClass().getAnnotation(OnlineDemoApi.class);
                    if (onlineDemoApi != null) {
                        Config config = Launcher.getBean(Config.class);
                        if (!config.isOnlineDemo()) {
                            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "The current environment does not allow this API to be called");
                        }
                    }

                    if (annotation.allowAccessWithSign()) {
                        rsaVerify(params);
                    }
                })
                .flowLimitByIpFunctionFunction((httpServletRequest, api, params) -> new FlowLimitByIpService(httpServletRequest, api, params).check())
                .flowLimitByMobileFunctionFunction((httpServletRequest, api, params) -> new FlowLimitByMobileService(httpServletRequest, api, params).check())
                .launch(BoardService.class, args);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Launcher.CONTEXT = applicationContext;
    }


    /**
     * rsa signature check
     * 需要验签的接口有两种情况：
     * 1. 请求来自前端，已登录状态，不需要验签，只检登录状态就好了。
     * 2. 未登录的情况下，请求来自其他子系统，需要进行验签。
     */
    private static void rsaVerify(JSONObject params) throws Exception {

        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);

        // At present, the board service only serves the application services of its own wefe system,
        // such as gateway and flow, so the same set of public and private keys are used for rsa signatures.
        String publicKey = CacheObjects.getRsaPublicKey();

        if (signedApiInput.getData() == null) {
            //throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "非法请求");
            return;
        }

        /*boolean verified = RSAUtil.verify(
                signedApiInput.getData().getBytes(StandardCharsets.UTF_8),
                RSAUtil.getPublicKey(publicKey),
                // 在 get 请求时，即便是对参数做了转义，也不能正确处理+号，加号总是被decode为空格，所以这里将空格还原为加号。
                signedApiInput.getSign().replace(" ", "+")
        );*/
        boolean verified = SignUtil.verify(signedApiInput.getData().getBytes(StandardCharsets.UTF_8), publicKey, signedApiInput.getSign().replace(" ", "+"), CacheObjects.getSecretKeyType());
        if (!verified) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "错误的签名");
        }

        params.clear();

        JSONObject data = JSONObject.parseObject(signedApiInput.getData());

        params.putAll(data);
    }
}
