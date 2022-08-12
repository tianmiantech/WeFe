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
package com.welab.wefe.serving.service.utils.sign;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.dto.SignedApiInput;
import com.welab.wefe.serving.service.database.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.entity.PartnerMysqlModel;
import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;
import com.welab.wefe.serving.service.database.repository.TableModelRepository;
import com.welab.wefe.serving.service.database.repository.TableServiceRepository;
import com.welab.wefe.serving.service.service.ClientServiceService;
import com.welab.wefe.serving.service.service.PartnerService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hunter.zhao
 * @date 2022/6/27
 */
public class PartnerVerifySignFunction extends AbstractVerifySignFunction {

    @Override
    protected void rsaVerify(HttpServletRequest request, JSONObject params) throws Exception {
        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);
        if (StringUtil.isNotEmpty(signedApiInput.getMemberId())) {
            signedApiInput.setPartnerCode(signedApiInput.getMemberId());
        }
        if (StringUtil.isNotEmpty(signedApiInput.getCustomerId())) {
            signedApiInput.setPartnerCode(signedApiInput.getCustomerId());
        }

        String serviceId = extractServiceId(request, JSONObject.parseObject(signedApiInput.getData()));

        String partnerId = findPartner(signedApiInput.getPartnerCode());

        String partnerRsaKey = findPartnerRsaKey(partnerId, serviceId);

        verify(signedApiInput, partnerRsaKey);

        buildParams(request, params, signedApiInput, serviceId);
    }

    private void buildParams(HttpServletRequest request, JSONObject params, SignedApiInput signedApiInput, String serviceId) {
        params.putAll(JSONObject.parseObject(signedApiInput.getData()));
        params.put("customer_id", signedApiInput.getCustomerId());
        params.put("partnerCode", signedApiInput.getPartnerCode());
        params.put("service_id", serviceId);
        params.put("isModelService", isModelService(request));
    }

    private void verify(SignedApiInput signedApiInput, String partnerRsaKey) throws Exception {
        boolean verified = RSAUtil.verify(signedApiInput.getData().getBytes(),
                RSAUtil.getPublicKey(partnerRsaKey), signedApiInput.getSign());
        if (!verified) {
            throw new StatusCodeWithException("Wrong signature", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    private String findPartnerRsaKey(String partnerId, String serviceId) throws StatusCodeWithException {

        ClientServiceService clientServiceService = Launcher.CONTEXT.getBean(ClientServiceService.class);
        ClientServiceMysqlModel clientServiceMysqlModel = clientServiceService.queryByIdAndServiceId(partnerId, serviceId);
        if (clientServiceMysqlModel == null) {
            throw new StatusCodeWithException("未查询到该合作方到开通记录", StatusCode.PARAMETER_VALUE_INVALID);
        }
        return clientServiceMysqlModel.getPublicKey();
    }

    private String findPartner(String customerId) throws StatusCodeWithException {
        PartnerService partnerService = Launcher.CONTEXT.getBean(PartnerService.class);
        PartnerMysqlModel partnerMysqlModel = partnerService.queryByCode(customerId);
        if (partnerMysqlModel == null) {
            throw new StatusCodeWithException("未查询到该合作方：" + customerId,
                    StatusCode.PARAMETER_VALUE_INVALID);
        }
        return partnerMysqlModel.getId();
    }

    private String extractServiceId(HttpServletRequest request, JSONObject params) throws StatusCodeWithException {
        String serviceUrl = extractServiceUrl(request);

        if (isModelProviderService(request)) {
            return params.getString("serviceId");
        }

        if (isModelService(request)) {
            TableModelRepository tableModelRepository = Launcher.CONTEXT.getBean(TableModelRepository.class);
            TableModelMySqlModel model = tableModelRepository.findOne("url", serviceUrl, TableModelMySqlModel.class);
            if (model == null) {
                throw new StatusCodeWithException("未查找到该模型服务！", StatusCode.PARAMETER_VALUE_INVALID);
            }
            return model.getServiceId();
        }

        TableServiceRepository serviceRepository = Launcher.CONTEXT.getBean(TableServiceRepository.class);
        TableServiceMySqlModel service = serviceRepository.findOne("url", serviceUrl, TableServiceMySqlModel.class);
        if (service == null) {
            throw new StatusCodeWithException("未查找到该服务！", StatusCode.PARAMETER_VALUE_INVALID);
        }
        return service.getId();
    }

    private String extractServiceUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.substring(uri.lastIndexOf("api/") + 4);
    }

    private boolean isModelService(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String serviceUrl = uri.substring(uri.lastIndexOf("api/") + 4);
        return serviceUrl.contains("predict");
    }

    private boolean isModelProviderService(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("model/provider/status/check");
    }
}
