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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.api.clientservice.QueryApi;
import com.welab.wefe.serving.service.api.clientservice.QueryListApi;
import com.welab.wefe.serving.service.api.clientservice.SaveApi;
import com.welab.wefe.serving.service.api.clientservice.UpdateApi;
import com.welab.wefe.serving.service.database.serving.entity.ClientMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceOutputModel;
import com.welab.wefe.serving.service.database.serving.entity.FeeConfigMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ServiceMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientRepository;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceQueryRepository;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceRepository;
import com.welab.wefe.serving.service.database.serving.repository.FeeConfigRepository;
import com.welab.wefe.serving.service.database.serving.repository.ServiceRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.PayTypeEnum;
import com.welab.wefe.serving.service.enums.ServiceClientTypeEnum;
import com.welab.wefe.serving.service.enums.ServiceStatusEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;


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
    private ClientRepository clientRepository;

    public void save(SaveApi.Input input) throws StatusCodeWithException {

        Specification<ClientServiceMysqlModel> where = Where.create()
                .equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId())
                .build(ClientServiceMysqlModel.class);

        // check the client-service by ids
        Optional<ClientServiceMysqlModel> clientServiceMysqlModel = clientServiceRepository.findOne(where);

        if (!clientServiceMysqlModel.isPresent()) {
            ClientServiceMysqlModel model = new ClientServiceMysqlModel();
            if (StringUtil.isNotEmpty(input.getClientId())) {
                model.setClientId(input.getClientId());
            }
            if (StringUtil.isNotEmpty(input.getServiceId())) {
                model.setServiceId(input.getServiceId());
            }
            model.setCreatedBy(input.getCreatedBy());
            // 保存服务类型
            ServiceMySqlModel serviceMySqlModel = serviceRepository.findOne("id", input.getServiceId(),
                    ServiceMySqlModel.class);
            model.setServiceType(serviceMySqlModel.getServiceType());
            if (input.getType() == ServiceClientTypeEnum.OPEN.getValue()) {
                model.setUrl(serviceMySqlModel.getUrl());
                model.setServiceName(serviceMySqlModel.getName());
            } else {
                model.setUrl(input.getUrl());
                model.setStatus(ServiceStatusEnum.USED.getCode());
            }
            // 保存客户相关信息
            ClientMysqlModel clientMysqlModel = clientRepository.findOne("id", input.getClientId(), ClientMysqlModel.class);
            model.setIpAdd(clientMysqlModel.getIpAdd());
            model.setClientName(clientMysqlModel.getName());

            // 保存计费规则相关信息
            model.setPayType(input.getPayType());
            model.setUnitPrice(input.getUnitPrice());
            model.setType(input.getType());
            model.setPublicKey(input.getPublicKey());
            clientServiceRepository.save(model);
            FeeConfigMysqlModel feeConfigMysqlModel = new FeeConfigMysqlModel();
            feeConfigMysqlModel.setServiceId(input.getServiceId());
            feeConfigMysqlModel.setPayType(input.getPayType());
            feeConfigMysqlModel.setClientId(input.getClientId());
            feeConfigMysqlModel.setUnitPrice(input.getUnitPrice());
            feeConfigRepository.save(feeConfigMysqlModel);

        } else {
            throw new StatusCodeWithException(StatusCode.CLIENT_SERVICE_EXIST);
        }
    }

    public PagingOutput<QueryListApi.Output> queryList(QueryListApi.Input input) {
        Specification<ClientServiceMysqlModel> where = Where.create().contains("serviceName", input.getServiceName())
                .contains("clientName", input.getClientName()).equal("status", input.getStatus())
                .equal("type", input.getType()).orderBy("createdTime", OrderBy.desc)
                .build(ClientServiceMysqlModel.class);

        PagingOutput<ClientServiceMysqlModel> models = clientServiceRepository.paging(where, input);
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

    public void update(UpdateApi.Input input) {
        Specification<ClientServiceMysqlModel> where = Where.create()
                .equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId())
                .build(ClientServiceMysqlModel.class);

        Optional<ClientServiceMysqlModel> optional = clientServiceRepository.findOne(where);
        if (optional.isPresent()) {
            ClientServiceMysqlModel model = optional.get();
            model.setStatus(input.getStatus());
            model.setUpdatedBy(input.getUpdatedBy());
            model.setUpdatedTime(new Date());
            model.setUnitPrice(input.getUnitPrice());
            model.setPayType(input.getPayType());
            clientServiceRepository.save(model);

            // 修改计费规则，新增一条计费规则记录
            FeeConfigMysqlModel feeConfigMysqlModel = new FeeConfigMysqlModel();
            feeConfigMysqlModel.setClientId(input.getClientId());
            feeConfigMysqlModel.setServiceId(input.getServiceId());
            feeConfigMysqlModel.setPayType(input.getPayType());
            feeConfigMysqlModel.setUnitPrice(input.getUnitPrice());
            feeConfigRepository.save(feeConfigMysqlModel);
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
}
