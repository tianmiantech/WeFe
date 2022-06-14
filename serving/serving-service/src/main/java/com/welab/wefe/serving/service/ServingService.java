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

package com.welab.wefe.serving.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import com.welab.wefe.common.web.dto.SignedApiInput;
import com.welab.wefe.serving.sdk.manager.ModelProcessorManager;
import com.welab.wefe.serving.service.database.entity.*;
import com.welab.wefe.serving.service.database.repository.ModelRepository;
import com.welab.wefe.serving.service.database.repository.ServiceRepository;
import com.welab.wefe.serving.service.feature.CodeFeatureDataHandler;
import com.welab.wefe.serving.service.operation.ServingApiLogger;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.service.ClientServiceService;
import com.welab.wefe.serving.service.service.MemberService;
import com.welab.wefe.serving.service.service.PartnerService;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hunter.zhao
 */
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(nameGenerator = ApiBeanNameGenerator.class,
        basePackageClasses = {Launcher.class, ServingService.class})
public class ServingService implements ApplicationContextAware {

    public static void main(String[] args) {
        Launcher
                .instance()
                .apiPackageClass(ServingService.class)
                .apiLogger(new ServingApiLogger())
                // Login status check method
//                .checkSessionTokenFunction((api, annotation, token) -> CurrentAccount.get() != null)
                .apiPermissionPolicy((request, annotation, params) -> {

                    if (!annotation.rsaVerify()) {
                        return;
                    }

                    switch (annotation.domain()) {
                        case Member:
                        case Board:
                            rsaVerifyBoard(params);
                            break;
                        case Customer:
                            rsaVerifyCustomer(request, params);
                            break;
                        default:
                            throw new RuntimeException("Unexpected enumeration value");
                    }
                })
                .launch(ServingService.class, args);

        //Initialize model processor
        ModelProcessorManager.init();

        //Initialize feature processor
        CodeFeatureDataHandler.init();
    }

    /**
     * rsa Signature check
     * <p>
     * customer
     * </p>
     */
    private static void rsaVerifyCustomer(HttpServletRequest request, JSONObject params) throws Exception {

        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);
        if (StringUtil.isNotEmpty(signedApiInput.getMemberId())) {
            signedApiInput.setPartnerCode(signedApiInput.getMemberId());
        }
        if (StringUtil.isNotEmpty(signedApiInput.getCustomerId())) {
            signedApiInput.setPartnerCode(signedApiInput.getCustomerId());
        }

        String serviceId = extractServiceId(request, signedApiInput);

        String partnerId = findPartner(signedApiInput.getPartnerCode());

        String partnerRsaKey = findPartnerRsaKey(partnerId, serviceId);

        verify(signedApiInput, partnerRsaKey);

        buildParams(request, params, signedApiInput, serviceId);
    }

    private static void buildParams(HttpServletRequest request, JSONObject params, SignedApiInput signedApiInput, String serviceId) {
        params.putAll(JSONObject.parseObject(signedApiInput.getData()));
        params.put("customer_id", signedApiInput.getCustomerId());
        params.put("partnerCode", signedApiInput.getPartnerCode());
        params.put("service_id", serviceId);
        params.put("isModelService", isModelService(request));
    }

    private static void verify(SignedApiInput signedApiInput, String partnerRsaKey) throws Exception {
        boolean verified = RSAUtil.verify(signedApiInput.getData().getBytes(),
                RSAUtil.getPublicKey(partnerRsaKey), signedApiInput.getSign());
        if (!verified) {
            throw new StatusCodeWithException("Wrong signature", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    private static String findPartnerRsaKey(String partnerId, String serviceId) throws StatusCodeWithException {

        ClientServiceService clientServiceService = Launcher.CONTEXT.getBean(ClientServiceService.class);
        ClientServiceMysqlModel clientServiceMysqlModel = clientServiceService.queryByIdAndServiceId(partnerId, serviceId);
        if (clientServiceMysqlModel == null) {
            throw new StatusCodeWithException("未查询到该合作方到开通记录", StatusCode.PARAMETER_VALUE_INVALID);
        }
        return clientServiceMysqlModel.getPublicKey();
    }

    private static String findPartner(String partnerCode) throws StatusCodeWithException {
        PartnerService partnerService = Launcher.CONTEXT.getBean(PartnerService.class);
        PartnerMysqlModel partnerMysqlModel = partnerService.queryByCode(partnerCode);
        if (partnerMysqlModel == null) {
            throw new StatusCodeWithException("未查询到该合作方：" + partnerCode,
                    StatusCode.PARAMETER_VALUE_INVALID);
        }
        return partnerMysqlModel.getId();
    }

    private static String extractServiceId(HttpServletRequest request, SignedApiInput signedApiInput) throws StatusCodeWithException {
        String serviceUrl = extractServiceUrl(request);
        if (isModelService(request)) {
            JSONObject param = JSONObject.parseObject(signedApiInput.getData());
            ModelRepository modelRepository = Launcher.CONTEXT.getBean(ModelRepository.class);
            ModelMySqlModel model = modelRepository.findOne("modelId", param.getString("model_id"), ModelMySqlModel.class);
            if (model == null) {
                throw new StatusCodeWithException("未查找到该模型服务！", StatusCode.PARAMETER_VALUE_INVALID);
            }
            return model.getModelId();
        }

        ServiceRepository serviceRepository = Launcher.CONTEXT.getBean(ServiceRepository.class);
        ServiceMySqlModel service = serviceRepository.findOne("url", serviceUrl, ServiceMySqlModel.class);
        if (service == null) {
            throw new StatusCodeWithException("未查找到该服务！", StatusCode.PARAMETER_VALUE_INVALID);
        }
        return service.getId();
    }

    private static String extractServiceUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.substring(uri.lastIndexOf("api/") + 4);
    }

    private static boolean isModelService(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String serviceUrl = uri.substring(uri.lastIndexOf("api/") + 4);
        return serviceUrl.contains("predict");
    }

    /**
     * rsa Signature check
     * <p>
     * Federal member
     * </p>
     */
    private static void rsaVerifyMember(JSONObject params) throws Exception {


        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);

        /**
         * Find signature information
         */
        MemberService memberService = Launcher.CONTEXT.getBean(MemberService.class);
        MemberMySqlModel member = memberService.findOne(signedApiInput.getMemberId());

        if (member == null) {
            throw new StatusCodeWithException("Invalid member_id：" + signedApiInput.getMemberId(), StatusCode.PARAMETER_VALUE_INVALID);
        }


        boolean verified = RSAUtil.verify(signedApiInput.getData().getBytes(), RSAUtil.getPublicKey(member.getPublicKey()), signedApiInput.getSign());
        if (!verified) {
            throw new StatusCodeWithException("Wrong signature", StatusCode.PARAMETER_VALUE_INVALID);
        }

        params.putAll(JSONObject.parseObject(signedApiInput.getData()));
    }

    /**
     * rsa Signature check
     * <p>
     * board
     * </p>
     */
    private static void rsaVerifyBoard(JSONObject params) throws Exception {
        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);

        if (!CacheObjects.getMemberId().equals(signedApiInput.getMemberId())) {
            throw new StatusCodeWithException("board校验失败：" + signedApiInput.getMemberId(), StatusCode.PARAMETER_VALUE_INVALID);
        }

        boolean verified = RSAUtil.verify(signedApiInput.getData().getBytes(), RSAUtil.getPublicKey(CacheObjects.getRsaPublicKey()), signedApiInput.getSign());
        if (!verified) {
            throw new StatusCodeWithException("错误的签名", StatusCode.PARAMETER_VALUE_INVALID);
        }

        params.putAll(JSONObject.parseObject(signedApiInput.getData()));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Launcher.CONTEXT = applicationContext;
    }
}
