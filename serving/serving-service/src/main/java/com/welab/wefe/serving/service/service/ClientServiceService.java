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

package com.welab.wefe.serving.service.service;

import cn.hutool.core.lang.UUID;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.api.clientservice.*;
import com.welab.wefe.serving.service.api.clientservice.ServiceUrlTestApi.Input;
import com.welab.wefe.serving.service.database.entity.*;
import com.welab.wefe.serving.service.database.repository.*;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.PayTypeEnum;
import com.welab.wefe.serving.service.enums.ServiceClientTypeEnum;
import com.welab.wefe.serving.service.enums.ServiceStatusEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.utils.ServiceUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * @author ivenn.zheng
 */
@Service
public class ClientServiceService {


    @Autowired
    private ClientServiceRepository clientServiceRepository;

    @Autowired
    private ClientServiceQueryRepository clientServiceQueryRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private FeeConfigRepository feeConfigRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    public void add(SaveApi.Input input) throws StatusCodeWithException {

        Specification<ClientServiceMysqlModel> where = Where.create()
                .equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId())
                .build(ClientServiceMysqlModel.class);

        // check the client-service by ids
        Optional<ClientServiceMysqlModel> clientServiceMysqlModel = clientServiceRepository.findOne(where);

        if (!clientServiceMysqlModel.isPresent()) {
            ClientServiceMysqlModel model = ModelMapper.map(input, ClientServiceMysqlModel.class);
            model.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            if (input.getType() == ServiceClientTypeEnum.OPEN.getValue()) {
                // 客户相关信息
                PartnerMysqlModel partnerMysqlModel = partnerRepository.findOne("id", input.getClientId(),
                        PartnerMysqlModel.class);

                // 保存服务类型
                ServiceMySqlModel serviceMySqlModel = serviceRepository.findOne("id", input.getServiceId(),
                        ServiceMySqlModel.class);
                if (serviceMySqlModel != null) {
                    model.setServiceType(serviceMySqlModel.getServiceType());
                    model.setUrl((StringUtils.isNotBlank(partnerMysqlModel.getServingBaseUrl())
                            ? (partnerMysqlModel.getServingBaseUrl().endsWith("/") ? partnerMysqlModel.getServingBaseUrl()
                            : (partnerMysqlModel.getServingBaseUrl() + "/"))
                            : "") + "api/" + serviceMySqlModel.getUrl());
                    model.setServiceName(serviceMySqlModel.getName());
                } else {
                    model.setServiceType(input.getServiceType());
                }
                model.setClientName(partnerMysqlModel.getName());
            } else {// 激活
                model.setUnitPrice(0.0);
                model.setIpAdd("-");
                model.setStatus(ServiceStatusEnum.USED.getCode());
                model.setServiceType(-1);
                model.setCode(input.getCode());
                if (StringUtils.isBlank(input.getPrivateKey()) || !input.getPrivateKey().contains("******")) {
                    model.setPrivateKey(input.getPrivateKey());
                    model.setPublicKey(input.getPublicKey());
                } else if (input.getPrivateKey().contains("******")) {
                    model.setPrivateKey(CacheObjects.getRsaPrivateKey());
                    model.setPublicKey(CacheObjects.getRsaPublicKey());
                }
            }
            // 保存计费规则相关信息
            clientServiceRepository.save(model);
            if (input.getType() == ServiceClientTypeEnum.OPEN.getValue()) {
                FeeConfigMysqlModel feeConfigMysqlModel = new FeeConfigMysqlModel();
                feeConfigMysqlModel.setServiceId(input.getServiceId());
                feeConfigMysqlModel.setPayType(input.getPayType());
                feeConfigMysqlModel.setClientId(input.getClientId());
                feeConfigMysqlModel.setUnitPrice(input.getUnitPrice());
                feeConfigRepository.save(feeConfigMysqlModel);
            }
        } else {
            throw new StatusCodeWithException(StatusCode.CLIENT_SERVICE_EXIST);
        }
    }

    public PagingOutput<QueryListApi.Output> queryList(QueryListApi.Input input) {
        Where where = Where.create();
        if (StringUtils.isNotBlank(input.getServiceName())) {
            where.contains("serviceName", input.getServiceName());
        }
        if (StringUtils.isNotBlank(input.getClientName())) {
            where.contains("clientName", input.getClientName());
        }
        if (input.getStatus() != null) {
            where.equal("status", input.getStatus());
        }
        if (input.getType() != null) {
            where.equal("type", input.getType());
        }
        Specification<ClientServiceMysqlModel> specification = where.orderBy("createdTime", OrderBy.desc)
                .build(ClientServiceMysqlModel.class);

        PagingOutput<ClientServiceMysqlModel> models = clientServiceRepository.paging(specification, input);
        List<QueryListApi.Output> list = new ArrayList<>();

        models.getList().forEach(x -> {
            QueryListApi.Output output = ModelMapper.map(x, QueryListApi.Output.class);
            output.setServiceType(ServiceTypeEnum.getValue(x.getServiceType()));
            output.setPayType(PayTypeEnum.getValueByCode(x.getPayType()));
            output.setStatus(ServiceStatusEnum.getValueByCode(x.getStatus()));
            list.add(output);
        });
        return PagingOutput.of(list.size(), list);
    }


    public ClientServiceOutputModel queryOne(QueryApi.Input input) {
        return clientServiceQueryRepository.queryOne(input.getId());
    }

    public ClientServiceMysqlModel findActivateClientServiceByUrl(String url) {
        Specification<ClientServiceMysqlModel> where = Where.create().equal("url", url)
                .build(ClientServiceMysqlModel.class);
        Optional<ClientServiceMysqlModel> optional = clientServiceRepository.findOne(where);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public ClientServiceMysqlModel queryByIdAndServiceId(String partnerId, String serviceId) {
        Specification<ClientServiceMysqlModel> where = Where.create().equal("serviceId", serviceId)
                .equal("clientId", partnerId).build(ClientServiceMysqlModel.class);
        Optional<ClientServiceMysqlModel> optional = clientServiceRepository.findOne(where);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public ClientServiceOutputModel detail(DetailApi.Input input) {
        Specification<ClientServiceMysqlModel> where = Where.create().equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId()).build(ClientServiceMysqlModel.class);
        Optional<ClientServiceMysqlModel> optional = clientServiceRepository.findOne(where);
        if (optional.isPresent()) {
            ClientServiceOutputModel output = ModelMapper.map(optional.get(), ClientServiceOutputModel.class);
            output.setPrivateKey(ServiceUtil.around(output.getPrivateKey(), 10, 10));
            output.setPublicKey(ServiceUtil.around(output.getPublicKey(), 10, 10));
            return output;
        }
        return null;
    }

    public void update(UpdateApi.Input input) throws StatusCodeWithException {
        Specification<ClientServiceMysqlModel> where = Where.create().equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId()).build(ClientServiceMysqlModel.class);

        Optional<ClientServiceMysqlModel> optional = clientServiceRepository.findOne(where);
        if (optional.isPresent()) {
            ClientServiceMysqlModel model = optional.get();
            model.setStatus(input.getStatus());
            model.setUpdatedBy(input.getUpdatedBy());
            model.setUpdatedTime(new Date());
            model.setUnitPrice(input.getUnitPrice());
            model.setPayType(input.getPayType());
            model.setIpAdd(input.getIpAdd());
            model.setPublicKey(input.getPublicKey());
            // 客户相关信息
            PartnerMysqlModel partnerMysqlModel = partnerRepository.findOne("id", input.getClientId(),
                    PartnerMysqlModel.class);
            // 保存服务类型
            ServiceMySqlModel serviceMySqlModel = serviceRepository.findOne("id", input.getServiceId(),
                    ServiceMySqlModel.class);

            if (model.getType() == ServiceClientTypeEnum.OPEN.getValue()) {
                model.setUrl((StringUtils.isNotBlank(partnerMysqlModel.getServingBaseUrl())
                        ? (partnerMysqlModel.getServingBaseUrl().endsWith("/") ? partnerMysqlModel.getServingBaseUrl()
                        : (partnerMysqlModel.getServingBaseUrl() + "/"))
                        : "") + "api/" + serviceMySqlModel.getUrl());
                model.setServiceName(serviceMySqlModel.getName());
            } else {
                model.setServiceName(input.getServiceName());
                model.setClientName(input.getClientName());
                model.setUnitPrice(0.0);
                model.setIpAdd("-");
                model.setUrl(input.getUrl());
                model.setStatus(ServiceStatusEnum.USED.getCode());
                model.setServiceType(-1);
                model.setCode(input.getCode());
                if (StringUtils.isBlank(input.getPrivateKey()) || !input.getPrivateKey().contains("******")) {
                    model.setPrivateKey(input.getPrivateKey());
                    model.setPublicKey(input.getPublicKey());
                } else if (input.getPrivateKey().contains("******")) {
                    model.setPrivateKey(CacheObjects.getRsaPrivateKey());
                    model.setPublicKey(CacheObjects.getRsaPublicKey());
                }
            }
            clientServiceRepository.save(model);

            if (model.getType() == ServiceClientTypeEnum.OPEN.getValue()) {
                // 修改计费规则，新增一条计费规则记录
                FeeConfigMysqlModel feeConfigMysqlModel = new FeeConfigMysqlModel();
                feeConfigMysqlModel.setClientId(input.getClientId());
                feeConfigMysqlModel.setServiceId(input.getServiceId());
                feeConfigMysqlModel.setPayType(input.getPayType());
                feeConfigMysqlModel.setUnitPrice(input.getUnitPrice());
                feeConfigRepository.save(feeConfigMysqlModel);
            }
        } else {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
        }
    }

    public void updateStatus(UpdateStatusApi.Input input) throws StatusCodeWithException {
        Specification<ClientServiceMysqlModel> where = Where.create().equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId()).build(ClientServiceMysqlModel.class);

        Optional<ClientServiceMysqlModel> optional = clientServiceRepository.findOne(where);
        if (optional.isPresent()) {
            ClientServiceMysqlModel model = optional.get();
            if (model.getStatus() == input.getStatus()) {
                throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
            }
            model.setStatus(input.getStatus());
            model.setUpdatedBy(input.getUpdatedBy());
            model.setUpdatedTime(new Date());
            clientServiceRepository.save(model);
        } else {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
        }
    }

    /**
     * 根据 serviceId 更新所有相关的字段
     *
     * @param serviceId
     * @param serviceName
     * @param url
     * @param serviceType
     */
    public void updateAllByServiceId(String serviceId, String serviceName, String url, Integer serviceType) {
        Specification<ClientServiceMysqlModel> where = Where.create()
                .equal("serviceId", serviceId)
                .build(ClientServiceMysqlModel.class);

        List<ClientServiceMysqlModel> mysqlModels = clientServiceRepository.findAll(where);
        List<ClientServiceMysqlModel> newModels = new ArrayList<>();
        for (ClientServiceMysqlModel model : mysqlModels) {
            model.setServiceName(serviceName);
            model.setServiceType(serviceType);
            model.setUrl(url);
            newModels.add(model);
        }
        clientServiceRepository.saveAll(newModels);
    }

    public List<ClientServiceMysqlModel> getAll() {
        return clientServiceRepository.findAll();
    }


    /**
     * save clientService
     *
     * @param serviceId
     * @param clientId
     * @param publicKey
     * @throws StatusCodeWithException
     */
    public void openService(String serviceId,
                            String clientId,
                            String publicKey,
                            ServiceTypeEnum serviceType) throws StatusCodeWithException {
        SaveApi.Input clientService = new SaveApi.Input();
        clientService.setServiceType(serviceType.getCode());
        clientService.setClientId(clientId);
        clientService.setServiceId(serviceId);
        clientService.setPublicKey(publicKey);
        clientService.setType(ServiceClientTypeEnum.OPEN.getValue());
        clientService.setStatus(ServiceStatusEnum.UNUSED.getCode());
        add(clientService);
    }

    /**
     * save clientService
     *
     * @param serviceId
     * @param clientId
     * @param privateKey
     * @param publicKey
     * @throws StatusCodeWithException
     */
    public void activateService(String serviceId,
                                String clientId,
                                String privateKey,
                                String publicKey,
                                String url,
                                ServiceTypeEnum serviceType) throws StatusCodeWithException {
        SaveApi.Input clientService = new SaveApi.Input();
        clientService.setClientId(clientId);
        clientService.setServiceId(serviceId);
        clientService.setServiceType(serviceType.getCode());
        clientService.setPrivateKey(privateKey);
        clientService.setPublicKey(publicKey);
        clientService.setUrl(url);
        clientService.setType(ServiceClientTypeEnum.ACTIVATE.getValue());
        clientService.setStatus(ServiceStatusEnum.UNUSED.getCode());
        add(clientService);
    }

    public int serviceUrlTest(Input input) throws Exception {
        String url = input.getUrl();
        HttpResponse response = HttpRequest.create(url).get();
        if (response.getCode() == -1) {
            throw response.getError();
        }
        return response.getCode();
    }


    public List<ClientServiceMysqlModel> queryActivateListByServiceId(String serviceId) {

        Specification<ClientServiceMysqlModel> where = Where.create()
                .equal("serviceId", serviceId)
                .equal("type", ServiceClientTypeEnum.ACTIVATE.getValue())
                .build(ClientServiceMysqlModel.class);

        return clientServiceRepository.findAll(where);
    }
}
