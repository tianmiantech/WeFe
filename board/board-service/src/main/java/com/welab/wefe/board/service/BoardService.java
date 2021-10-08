/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.base.OnlineDemoApi;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.operation.OperationLogAfterApiExecute;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.storage.StorageManager;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.SignedApiInput;
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
        basePackageClasses = {BoardService.class, Launcher.class, StorageManager.class}
)
public class BoardService implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(BoardService.class);

    public static void main(String[] args) {
        Launcher
                .instance()
                .apiPackageClass(BoardService.class)
                // Login status check method
                .checkSessionTokenFunction((api, annotation, token) -> CurrentAccount.get() != null)
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
                        Config config = Launcher.CONTEXT.getBean(Config.class);
                        if (!config.isOnlineDemo()) {
                            throw new StatusCodeWithException("The current environment does not allow this API to be called", StatusCode.SYSTEM_ERROR);
                        }
                    }

                    if (annotation.rsaVerify()) {
                        rsaVerify(params);
                    }
                })
                .launch(BoardService.class, args);

        Launcher
                .instance()
                .afterApiExecuteFunction(Launcher.CONTEXT.getBean(OperationLogAfterApiExecute.class));
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Launcher.CONTEXT = applicationContext;
    }


    /**
     * rsa signature check
     */
    private static void rsaVerify(JSONObject params) throws Exception {
        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);

        // At present, the board service only serves the application services of its own wefe system,
        // such as gateway and flow, so the same set of public and private keys are used for rsa signatures.
        String publicKey = CacheObjects.getRsaPublicKey();

        boolean verified = RSAUtil.verify(
                signedApiInput.getData().getBytes(StandardCharsets.UTF_8),
                RSAUtil.getPublicKey(publicKey),
                signedApiInput.getSign()
        );
        if (!verified) {
            throw new StatusCodeWithException("错误的签名", StatusCode.PARAMETER_VALUE_INVALID);
        }

        params.clear();

        JSONObject data = JSONObject.parseObject(signedApiInput.getData());

        params.putAll(data);
    }
}
