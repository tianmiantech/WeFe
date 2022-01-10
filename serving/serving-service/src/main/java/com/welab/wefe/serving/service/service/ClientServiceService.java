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

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.serving.service.api.clientservice.QueryApi;
import com.welab.wefe.serving.service.api.clientservice.QueryListApi;
import com.welab.wefe.serving.service.api.clientservice.SaveApi;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceOutputModel;
import com.welab.wefe.serving.service.database.serving.entity.FeeConfigMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceQueryRepository;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceRepository;
import com.welab.wefe.serving.service.database.serving.repository.FeeConfigRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void save(SaveApi.Input input) {

        Specification<ClientServiceMysqlModel> where = Where.create()
                .equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId())
                .build(ClientServiceMysqlModel.class);

        // check the client-service by ids
        Optional<ClientServiceMysqlModel> clientServiceMysqlModel = clientServiceRepository.findOne(where);
        ClientServiceMysqlModel model = clientServiceMysqlModel.orElse(new ClientServiceMysqlModel());

        if (null != input.getStatus()) {
            model.setStatus(input.getStatus());
        }

        if (!clientServiceMysqlModel.isPresent()) {
            model = new ClientServiceMysqlModel();
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
            // exist, then update client-service
            model = clientServiceMysqlModel.get();
            model.setUpdatedTime(new Date());
            clientServiceRepository.updateByParam(model.getServiceId(), model.getClientId(), model.getStatus(),
                    model.getUpdatedBy(), model.getUpdatedTime());


            Specification<FeeConfigMysqlModel> feeWhere = Where.create()
                    .equal("serviceId", input.getServiceId())
                    .equal("clientId", input.getClientId())
                    .build(FeeConfigMysqlModel.class);

            Optional<FeeConfigMysqlModel> one = feeConfigRepository.findOne(feeWhere);
            if (one.isPresent()) {
                FeeConfigMysqlModel feeConfigMysqlModel = one.get();
                feeConfigMysqlModel.setUnitPrice(input.getUnitPrice());
                feeConfigMysqlModel.setPayType(input.getPayType());
                feeConfigMysqlModel.setUpdatedTime(new Date());
                feeConfigRepository.save(feeConfigMysqlModel);
            }

        }


    }


    public PagingOutput<ClientServiceOutputModel> queryList(QueryListApi.Input input) {
        List<ClientServiceOutputModel> list = clientServiceQueryRepository.queryClientServiceList(input.getServiceName(), input.getClientName(), input.getStatus());
        return PagingOutput.of(list.size(), list);
    }

    public ClientServiceOutputModel queryOne(QueryApi.Input input) {
        return clientServiceQueryRepository.queryOne(input.getId());

    }


}
