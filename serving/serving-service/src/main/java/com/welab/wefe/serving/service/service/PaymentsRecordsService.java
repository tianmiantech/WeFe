/*
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
package com.welab.wefe.serving.service.service;

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.OrderBy;
import com.welab.wefe.serving.service.api.paymentsrecords.QueryListApi;
import com.welab.wefe.serving.service.api.paymentsrecords.SaveApi;
import com.welab.wefe.serving.service.database.serving.entity.ClientMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.PaymentsRecordsMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ServiceMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientRepository;
import com.welab.wefe.serving.service.database.serving.repository.PaymentsRecordsRepository;
import com.welab.wefe.serving.service.database.serving.repository.ServiceRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.PaymentsTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * @author ivenn.zheng
 * @date 2022/1/14
 */
@Service
public class PaymentsRecordsService {


    @Autowired
    private PaymentsRecordsRepository paymentsRecordsRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ClientRepository clientRepository;


    private Logger log = LoggerFactory.getLogger(PaymentsRecordsService.class);

    public PagingOutput<PaymentsRecordsMysqlModel> queryList(QueryListApi.Input input) {

        Specification<PaymentsRecordsMysqlModel> where = Where.create()
                .betweenAndDate("createdTime", input.getStartTime(), input.getEndTime())
                .contains("clientName", input.getClientName())
                .contains("serviceName", input.getServiceName())
                .equal("payType", input.getPayType())
                .equal("serviceType", input.getServiceType())
                .orderBy("createdTime", OrderBy.desc)
                .build(PaymentsRecordsMysqlModel.class);

        return paymentsRecordsRepository.paging(where, input);
    }

    public void save(SaveApi.Input input) {

        PaymentsRecordsMysqlModel model = new PaymentsRecordsMysqlModel();
        model.setRemark(input.getRemark());
        model.setAmount(input.getAmount());
        model.setPayType(input.getPayType());
        model.setStatus(input.getStatus());

        // get service by id
        Optional<ServiceMySqlModel> serviceMySqlModel = serviceRepository.findById(input.getServiceId());
        if (serviceMySqlModel.isPresent()) {
            ServiceMySqlModel service = serviceMySqlModel.get();
            model.setServiceId(service.getId());
            model.setServiceName(service.getName());
            model.setServiceType(service.getServiceType());
        }


        // get client
        Optional<ClientMysqlModel> clientMysqlModel = clientRepository.findById(input.getClientId());
        if (clientMysqlModel.isPresent()) {
            ClientMysqlModel client = clientMysqlModel.get();
            model.setClientId(client.getId());
            model.setClientName(client.getName());
        }


        Specification<PaymentsRecordsMysqlModel> where = Where.create()
                .equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId())
                .build(PaymentsRecordsMysqlModel.class);
        Optional<PaymentsRecordsMysqlModel> one = paymentsRecordsRepository.findOne(where);

        if (one.isPresent()) {
            PaymentsRecordsMysqlModel paymentsRecordsMysqlModel = one.get();
            if (input.getPayType() == PaymentsTypeEnum.RECHARGE.getValue()) {
                // 充值，余额增加
                model.setBalance(paymentsRecordsMysqlModel.getBalance().add(input.getAmount()));
            } else if (input.getPayType() == PaymentsTypeEnum.PAID.getValue()) {
                // 支出，余额减少
                model.setBalance(paymentsRecordsMysqlModel.getBalance().subtract(input.getAmount()));
            }
        } else {
            if (input.getPayType() == PaymentsTypeEnum.RECHARGE.getValue()) {
                // 充值，余额增加
                model.setBalance(new BigDecimal("0.0").add(input.getAmount()));
            } else if (input.getPayType() == PaymentsTypeEnum.PAID.getValue()) {
                // 支出，余额减少
                model.setBalance(new BigDecimal("0.0").subtract(input.getAmount()));
            }

        }


        paymentsRecordsRepository.save(model);

    }

}
