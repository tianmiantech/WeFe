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
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.api.serviceorder.QueryListApi;
import com.welab.wefe.serving.service.api.serviceorder.SaveApi;
import com.welab.wefe.serving.service.database.serving.entity.ServiceOrderMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ServiceOrderRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.dto.ServiceOrderInput;
import com.welab.wefe.serving.service.enums.ServiceOrderEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ivenn.zheng
 * @date 2022/4/27
 */
@Service
public class ServiceOrderService {


    @Autowired
    ServiceOrderRepository serviceOrderRepository;

    public void save(SaveApi.Input input) {

        ServiceOrderMysqlModel model = serviceOrderRepository.findOne("id", input.getId(), ServiceOrderMysqlModel.class);
        if (null == model) {
            model = new ServiceOrderMysqlModel();
            input.setId(model.getId());
        }
        model = ModelMapper.map(input, ServiceOrderMysqlModel.class);
        model.setUpdatedBy(input.getUpdatedBy());
        model.setUpdatedTime(new Date());

        serviceOrderRepository.save(model);
    }

    public PagingOutput<QueryListApi.Output> queryList(QueryListApi.Input input) {

        Specification<ServiceOrderMysqlModel> where = Where.create()
                .equal("serviceId", input.getServiceId())
                .contains("serviceName", input.getServiceName())
                .equal("serviceType", input.getServiceType())
                .equal("orderType", input.getOrderType())
                .equal("status", input.getStatus())
                .contains("requestPartnerName", input.getRequestPartnerName())
                .contains("responsePartnerName", input.getResponsePartnerName())
                .betweenAndDate("createdTime", input.getStartTime().getTime(), input.getEndTime().getTime())
                .build(ServiceOrderMysqlModel.class);

        PagingOutput<ServiceOrderMysqlModel> models = serviceOrderRepository.paging(where, input);
        List<QueryListApi.Output> list = new ArrayList<>();
        models.getList().forEach(x -> {
            QueryListApi.Output output = ModelMapper.map(x, QueryListApi.Output.class);
            list.add(output);
        });
        return PagingOutput.of(list.size(), list);

    }

    /**
     * 根据参数获取列表, 且不查询进行中的订单
     *
     * @param input
     * @return
     */
    public List<ServiceOrderMysqlModel> getByParams(ServiceOrderInput input) {
        Specification<ServiceOrderMysqlModel> where = Where.create()
                .equal("serviceId", input.getServiceId())
                .contains("serviceName", input.getServiceName())
                .equal("status", input.getStatus())
                .equal("requestPartnerId", input.getRequestPartnerId())
                .contains("requestPartnerName", input.getRequestPartnerName())
                .equal("responsePartnerId", input.getResponsePartnerId())
                .contains("responsePartnerName", input.getResponsePartnerName())
                .notEqual("status", input.getStatus())
                .equal("orderType", input.getOrderType())
                .betweenAndDate("createdTime", input.getCreatedStartTime().getTime(), input.getCreatedEndTime().getTime())
                .betweenAndDate("updatedTime", input.getUpdatedStartTime().getTime(), input.getUpdatedEndTime().getTime())
                .build(ServiceOrderMysqlModel.class);

        return serviceOrderRepository.findAll(where);

    }


}
