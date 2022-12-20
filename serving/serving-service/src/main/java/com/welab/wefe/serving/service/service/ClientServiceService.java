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
import com.welab.wefe.serving.sdk.dto.ProviderParams;
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
import java.util.stream.Collectors;

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
    private BaseServiceRepository<BaseServiceMySqlModel> serviceRepository;

    @Autowired
    private FeeConfigRepository feeConfigRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PartnerService partnerService;

    public void add(SaveApi.Input input) throws StatusCodeWithException {
        // 激活服务不需要用户填写serviceId
        if (input.getType() == ServiceClientTypeEnum.ACTIVATE.getValue()) {
            String tmp = input.getUrl().replaceAll("[^a-zA-Z0-9]", "");
            input.setServiceId(tmp.substring(0, Integer.min(256, tmp.length())));
            input.setClientId(tmp.substring(0, Integer.min(32, tmp.length())));
        }
        Specification<ClientServiceMysqlModel> where = Where.create().equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId()).build(ClientServiceMysqlModel.class);

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
                BaseServiceMySqlModel serviceMySqlModel = serviceRepository.findOne("serviceId", input.getServiceId(),
                        BaseServiceMySqlModel.class);
                if (serviceMySqlModel != null) {
                    model.setServiceType(serviceMySqlModel.getServiceType());
                    model.setUrl(serviceMySqlModel.getUrl());
                    model.setServiceName(serviceMySqlModel.getName());
                } else {
                    model.setServiceType(input.getServiceType());
                }
                model.setClientName(partnerMysqlModel.getName());
                if (model.getUnitPrice() < 0) {
                    StatusCode.PARAMETER_VALUE_INVALID.throwException("单价不能为负数：" + model.getUnitPrice());
                }
            } else {// 激活

                model.setIpAdd("-");
                model.setPayType(-1);
                model.setStatus(ServiceStatusEnum.USED.getCode());
                model.setServiceType(-1);
                model.setCode(input.getCode());
                model.setServiceName(input.getServiceName());
                if (StringUtils.isNotBlank(input.getPublicKey()) && StringUtils.isBlank(input.getPrivateKey())
                        && input.getPublicKey().equalsIgnoreCase(CacheObjects.getRsaPublicKey())) {
                    model.setPrivateKey(CacheObjects.getRsaPrivateKey());
                    model.setPublicKey(CacheObjects.getRsaPublicKey());
                } else if (StringUtils.isBlank(input.getPublicKey())) {
                    model.setPrivateKey(input.getPrivateKey());
                    model.setPublicKey(input.getPublicKey());
                }
                if (StringUtils.isNotBlank(input.getUrl()) && input.getUrl().endsWith("/")) {
                    input.setUrl(input.getUrl().substring(0, input.getUrl().length() - 1));
                }
                model.setUrl(input.getUrl());
            }
//            model.setType(input.getType());
            model.setCreatedTime(new Date());
            model.setUpdatedBy(model.getCreatedBy());
            model.setUpdatedTime(new Date());
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

        PagingOutput<ClientServiceMysqlModel> page = clientServiceRepository.paging(specification, input);
        List<QueryListApi.Output> list = new ArrayList<>();

        page.getList().forEach(x -> {
            QueryListApi.Output output = ModelMapper.map(x, QueryListApi.Output.class);
            output.setServiceType(ServiceTypeEnum.getValue(x.getServiceType()));
            output.setPayType(PayTypeEnum.getValueByCode(x.getPayType()));
            output.setStatus(ServiceStatusEnum.getValueByCode(x.getStatus()));
            if (x.getType() == ServiceClientTypeEnum.ACTIVATE.getValue()) {
                output.setPayType("-");
                output.setUnitPrice("-");
            } else {
                output.setPayType(PayTypeEnum.getValueByCode(x.getPayType()));
                output.setUnitPrice(x.getUnitPrice() + "");
                output.setUrl(ServiceService.SERVICE_PRE_URL + x.getUrl());
            }
            list.add(output);
        });
        return PagingOutput.of(page.getTotal(), list);
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

    public void deleteActivate(UpdateApi.Input input) throws StatusCodeWithException {
        Specification<ClientServiceMysqlModel> where = Where.create().equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId()).build(ClientServiceMysqlModel.class);
        Optional<ClientServiceMysqlModel> optional = clientServiceRepository.findOne(where);
        /*if (!CurrentAccount.isAdmin() || !CurrentAccount.isSuperAdmin()) {
            throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
        }*/
        if (optional.isPresent()) {
            ClientServiceMysqlModel model = optional.get();
            if (model.getType().equals(ServiceClientTypeEnum.ACTIVATE.getValue())) {
                clientServiceRepository.delete(model);
            } else {
                throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
            }
        } else {
            StatusCode.DATA_NOT_FOUND.throwException();
        }
    }

    public void update(UpdateApi.Input input) throws StatusCodeWithException {
        Specification<ClientServiceMysqlModel> where = Where.create().equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId()).build(ClientServiceMysqlModel.class);

        Optional<ClientServiceMysqlModel> optional = clientServiceRepository.findOne(where);
        if (optional.isPresent()) {
            ClientServiceMysqlModel model = optional.get();
            model.setUpdatedBy(input.getUpdatedBy());
            model.setUpdatedTime(new Date());
            model.setUnitPrice(input.getUnitPrice());
            model.setPayType(input.getPayType());
            model.setIpAdd(input.getIpAdd());
            // 保存服务类型
            BaseServiceMySqlModel serviceMySqlModel = serviceRepository.findOne("serviceId", input.getServiceId(),
                    BaseServiceMySqlModel.class);
            // 开通
            if (model.getType() == ServiceClientTypeEnum.OPEN.getValue()) {
                model.setUrl(serviceMySqlModel.getUrl());
                model.setServiceName(serviceMySqlModel.getName());
                if (StringUtils.isBlank(input.getPublicKey()) || !input.getPublicKey().contains("******")) {
                    model.setPublicKey(input.getPublicKey());
                }
                if (model.getUnitPrice() < 0) {
                    StatusCode.PARAMETER_VALUE_INVALID.throwException("单价不能为负数：" + model.getUnitPrice());
                }
            } else { // 激活
                model.setUnitPrice(0.0);
                model.setIpAdd("-");
                model.setPayType(-1);
                model.setUrl(input.getUrl());
                model.setServiceType(-1);
                model.setCode(input.getCode());
                model.setClientName(input.getClientName());
                model.setServiceName(input.getServiceName());
                if (StringUtils.isNotBlank(input.getPublicKey()) && StringUtils.isNotBlank(input.getPrivateKey())
                        && !input.getPublicKey().contains("******") && !input.getPrivateKey().contains("******")) { // 自己填写
                    model.setPrivateKey(input.getPrivateKey());
                    model.setPublicKey(input.getPublicKey());
                } else if (StringUtils.isBlank(input.getPrivateKey()) && StringUtils.isBlank(input.getPublicKey())) {
                    model.setPrivateKey("");
                    model.setPublicKey("");
                }
            }
            clientServiceRepository.save(model);

            if (model.getType() == ServiceClientTypeEnum.OPEN.getValue()) {
                // 修改计费规则，新增一条计费规则记录
                FeeConfigMysqlModel feeConfigMysqlModel = new FeeConfigMysqlModel();
                feeConfigMysqlModel.setClientId(model.getClientId());
                feeConfigMysqlModel.setServiceId(model.getServiceId());
                feeConfigMysqlModel.setPayType(input.getPayType());
                feeConfigMysqlModel.setUnitPrice(input.getUnitPrice());
                feeConfigRepository.save(feeConfigMysqlModel);
            }
        } else {
            StatusCode.DATA_NOT_FOUND.throwException();
        }
    }

    public void updateStatus(UpdateStatusApi.Input input) throws StatusCodeWithException {
        Specification<ClientServiceMysqlModel> where = Where.create().equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId()).build(ClientServiceMysqlModel.class);

        Optional<ClientServiceMysqlModel> optional = clientServiceRepository.findOne(where);
        if (optional.isPresent()) {
            ClientServiceMysqlModel model = optional.get();
            if (model.getType() == ServiceClientTypeEnum.ACTIVATE.getValue()) {
                throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
            }
            if (model.getStatus() == input.getStatus()) {
                throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
            }
            model.setStatus(input.getStatus());
            model.setUpdatedBy(input.getUpdatedBy());
            model.setUpdatedTime(new Date());
            clientServiceRepository.save(model);
        } else {
            StatusCode.DATA_NOT_FOUND.throwException();
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
        Specification<ClientServiceMysqlModel> where = Where.create().equal("serviceId", serviceId)
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
    public void openService(String serviceId, String serviceName, String url, String clientId, String publicKey,
            ServiceTypeEnum serviceType) throws StatusCodeWithException {
        SaveApi.Input clientService = new SaveApi.Input();
        clientService.setServiceType(serviceType.getCode());
        clientService.setClientId(clientId);
        clientService.setServiceId(serviceId);
        clientService.setPublicKey(publicKey);
        clientService.setCode(clientId);
        clientService.setType(ServiceClientTypeEnum.OPEN.getValue());
        clientService.setStatus(ServiceStatusEnum.UNUSED.getCode());
        clientService.setCreatedBy(CacheObjects.getMemberName());
        clientService.setServiceName(serviceName);
        clientService.setUrl(url);
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
    public void activateService(String serviceId, String serviceName, String clientId, String privateKey,
            String publicKey, String url, ServiceTypeEnum serviceType) throws StatusCodeWithException {
        ClientServiceMysqlModel clientService = clientServiceRepository.findOne("serviceId", serviceId,
                ClientServiceMysqlModel.class);
        if (clientService == null) {
            clientService = new ClientServiceMysqlModel();
        }
        clientService.setClientId(clientId);
        clientService.setClientName(CacheObjects.getPartnerName(clientId));
        clientService.setServiceId(serviceId);
        clientService.setServiceName(serviceName);
        clientService.setServiceType(serviceType.getCode());
        clientService.setPrivateKey(privateKey);
        clientService.setPublicKey(publicKey);
        clientService.setUrl(url);
        clientService.setType(ServiceClientTypeEnum.ACTIVATE.getValue());
        clientService.setStatus(ServiceStatusEnum.UNUSED.getCode());
        clientService.setCreatedBy(CacheObjects.getMemberName());
        clientService.setIpAdd("-");
        clientService.setPayType(-1);
        clientService.setServiceType(-1);

        clientServiceRepository.save(clientService);
    }

    public int serviceUrlTest(Input input) throws Exception {
        String url = input.getUrl();
        if (url.matches("^((http|https)://)([\\w-]+\\.)+[\\w$]+(\\/[\\w-?=&./]*)?$")) {
            HttpResponse response = HttpRequest.create(url).get();
            if (response.getCode() == -1) {
                throw response.getError();
            }
            return response.getCode();
        } else {
            throw StatusCode.PARAMETER_VALUE_INVALID.throwException("非法的URL地址");
        }
    }

    public List<ClientServiceMysqlModel> queryActivateListByServiceId(String serviceId) {

        Specification<ClientServiceMysqlModel> where = Where.create().equal("serviceId", serviceId)
                .equal("type", ServiceClientTypeEnum.ACTIVATE.getValue()).build(ClientServiceMysqlModel.class);

        return clientServiceRepository.findAll(where);
    }

    public List<ProviderParams> findProviderList(String serviceId) {
        return queryActivateListByServiceId(serviceId).stream()
                // TODO 地址获取修改
                .map(x -> ProviderParams.of(x.getClientId(),
                        partnerService.findModelServiceUrl(x.getClientId()) + x.getUrl()))
                .collect(Collectors.toList());
    }
}
