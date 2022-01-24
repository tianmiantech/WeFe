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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.serving.service.api.clientservice.QueryApi;
import com.welab.wefe.serving.service.api.clientservice.QueryListApi;
import com.welab.wefe.serving.service.api.clientservice.SaveApi;
import com.welab.wefe.serving.service.api.clientservice.UpdateApi;
import com.welab.wefe.serving.service.database.serving.entity.*;
import com.welab.wefe.serving.service.database.serving.repository.*;
import com.welab.wefe.serving.service.dto.PagingOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
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
            model.setClientName(input.getClientName());
            model.setServiceName(input.getServiceName());

            // 保存服务类型
            ServiceMySqlModel serviceMySqlModel = serviceRepository.findOne("id", input.getServiceId(), ServiceMySqlModel.class);
            model.setServiceType(serviceMySqlModel.getServiceType());
            model.setUrl(serviceMySqlModel.getUrl());

            // 保存客户相关信息
            ClientMysqlModel clientMysqlModel = clientRepository.findOne("id", input.getClientId(), ClientMysqlModel.class);
            model.setIpAdd(clientMysqlModel.getIpAdd());

            // 保存计费规则相关信息
            model.setPayType(input.getPayType());
            model.setUnitPrice(input.getUnitPrice());

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


//    public PagingOutput<ClientServiceOutputModel> queryList(QueryListApi.Input input) {
//        List<ClientServiceOutputModel> list = clientServiceQueryRepository.queryClientServiceList(input.getServiceName(),
//                input.getClientName(), input.getStatus(), input.getPageIndex() * input.getPageSize(), input.getPageSize());
//        Integer total = clientServiceQueryRepository.count(input.getServiceName(), input.getClientName(), input.getStatus());
//
//        return PagingOutput.of(total, list);
//    }

    public PagingOutput<ClientServiceMysqlModel> queryList(QueryListApi.Input input) {

//        List<ClientServiceOutputModel> result = new ArrayList<>();


        // 根据service name 模糊查询
        Specification<ClientServiceMysqlModel> where = Where.create()
                .contains("serviceName", input.getServiceName())
                .contains("clientName", input.getClientName())
                .equal("status", input.getStatus())
                .build(ClientServiceMysqlModel.class);

        return clientServiceRepository.paging(where, input);

//        List<ClientServiceMysqlModel> list = paging.getList();
//
//        for (ClientServiceMysqlModel clientServiceMysqlModel : list) {
//            ClientServiceOutputModel clientServiceOutputModel = new ClientServiceOutputModel();
//            clientServiceOutputModel.setStatus(clientServiceMysqlModel.getStatus());
//
//            // 保存服务相关信息
//            ServiceMySqlModel serviceMySqlModel = serviceRepository.findOne("id", clientServiceMysqlModel.getServiceId(), ServiceMySqlModel.class);
//            clientServiceOutputModel.setServiceId(serviceMySqlModel.getId());
//            clientServiceOutputModel.setServiceName(serviceMySqlModel.getName());
//            clientServiceOutputModel.setServiceType(serviceMySqlModel.getServiceType());
//            clientServiceOutputModel.setUrl(serviceMySqlModel.getUrl());
//
//            // 保存计费规则相关
//            Specification<FeeConfigMysqlModel> feeConfigMysqlModelSpecification = Where.create()
//                    .equal("serviceId", clientServiceMysqlModel.getServiceId())
//                    .equal("clientId", clientServiceMysqlModel.getClientId())
//                    .orderBy("createdTime", OrderBy.desc)
//                    .build(FeeConfigMysqlModel.class);
//            FeeConfigMysqlModel feeConfigRepositoryOne = feeConfigRepository.findAll(feeConfigMysqlModelSpecification).get(0);
//            clientServiceOutputModel.setPayType(feeConfigRepositoryOne.getPayType());
//            clientServiceOutputModel.setUnitPrice(feeConfigRepositoryOne.getUnitPrice());
//
//            // 保存客户相关信息
//            ClientMysqlModel clientMysqlModel = clientRepository.findOne("id", clientServiceMysqlModel.getClientId(), ClientMysqlModel.class);
//            clientServiceOutputModel.setIpAdd(clientMysqlModel.getIpAdd());
//            clientServiceOutputModel.setClientId(clientMysqlModel.getId());
//            clientServiceOutputModel.setClientName(clientMysqlModel.getName());
//
//            result.add(clientServiceOutputModel);
//        }


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
            model.setUpdatedBy("");
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
}
