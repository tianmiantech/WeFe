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
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.serving.service.api.clientservice.QueryApi;
import com.welab.wefe.serving.service.api.clientservice.QueryListApi;
import com.welab.wefe.serving.service.api.clientservice.SaveApi;
import com.welab.wefe.serving.service.api.clientservice.UpdateApi;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceOutputModel;
import com.welab.wefe.serving.service.database.serving.entity.FeeConfigMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceQueryRepository;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceRepository;
import com.welab.wefe.serving.service.database.serving.repository.FeeConfigRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
    private FeeConfigRepository feeConfigRepository;

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

            clientServiceRepository.save(model);
            FeeConfigMysqlModel feeConfigMysqlModel = new FeeConfigMysqlModel();
            feeConfigMysqlModel.setServiceId(input.getServiceId());
            feeConfigMysqlModel.setPayType(input.getPayType());
            feeConfigMysqlModel.setClientId(input.getClientId());
            feeConfigMysqlModel.setUnitPrice(input.getUnitPrice());
            feeConfigRepository.save(feeConfigMysqlModel);

        } else {
            throw new StatusCodeWithException("该客户服务已被创建", StatusCode.CLIENT_SERVICE_ALREADY_HAS);
        }
    }


    public PagingOutput<ClientServiceOutputModel> queryList(QueryListApi.Input input) {
        List<ClientServiceOutputModel> list = clientServiceQueryRepository.queryClientServiceList(input.getServiceName(),
                input.getClientName(), input.getStatus(), input.getPageIndex() * input.getPageSize(), input.getPageSize());
        Integer total = clientServiceQueryRepository.count(input.getServiceName(), input.getClientName(), input.getStatus());

        return PagingOutput.of(total, list);
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
            clientServiceRepository.updateByParam(input.getServiceId(), input.getClientId(), input.getStatus(),
                    "", new Date());


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
