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

import com.alibaba.fastjson.JSON;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.api.servicecalllog.QueryListApi;
import com.welab.wefe.serving.service.database.entity.ServiceCallLogMysqlModel;
import com.welab.wefe.serving.service.database.repository.ServiceCallLogRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.dto.ServiceCallLogInput;
import com.welab.wefe.serving.service.utils.ServiceUtil;

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
public class ServiceCallLogService {

    @Autowired
    ServiceCallLogRepository serviceCallLogRepository;

    public PagingOutput<QueryListApi.Output> queryList(QueryListApi.Input input) {

        Specification<ServiceCallLogMysqlModel> where = Where.create()
                .equal("serviceId", input.getServiceId())
                .equal("orderId", input.getOrderId())
                .orderBy("createdTime", OrderBy.asc)
                .build(ServiceCallLogMysqlModel.class);


        PagingOutput<ServiceCallLogMysqlModel> paging = serviceCallLogRepository.paging(where, input);

        List<QueryListApi.Output> list = new ArrayList<>();
        paging.getList().forEach(x -> {
            QueryListApi.Output output = ModelMapper.map(x, QueryListApi.Output.class);
            list.add(output);
        });
        return PagingOutput.of(list.size(), list);

    }

    /**
     * 兼容 新增/更新
     *
     * @param input
     */
    public void save(ServiceCallLogMysqlModel input) {

        ServiceCallLogMysqlModel model = serviceCallLogRepository.findOne("id", input.getId(), ServiceCallLogMysqlModel.class);
        if (null == model) {
            model = new ServiceCallLogMysqlModel();
            input.setId(model.getId());
        }
        model = ModelMapper.map(input, ServiceCallLogMysqlModel.class);
        model.setUpdatedBy(input.getUpdatedBy());
        model.setUpdatedTime(new Date());

        serviceCallLogRepository.save(model);
    }

    public ServiceCallLogMysqlModel add(String orderId, int callByMe, String requestPartnerId,
            String requestPartnerName, String serviceId, String serviceName, Integer serviceType, String requestId,
            String requestData, String requestIp) {
        ServiceCallLogMysqlModel model = new ServiceCallLogMysqlModel();
        model.setOrderId(orderId);
        model.setCallByMe(callByMe);
        model.setRequestPartnerId(requestPartnerId);
        model.setRequestPartnerName(requestPartnerName);
        model.setServiceId(serviceId);
        model.setServiceName(serviceName);
        model.setServiceType(serviceType);
        model.setRequestId(requestId);
        model.setRequestData(requestData);
        model.setRequestIp(requestIp);
        model.setResponsePartnerId(CacheObjects.getMemberId());
        model.setResponsePartnerName(CacheObjects.getMemberName());
        model.setCreatedTime(new Date());
        model.setUpdatedTime(new Date());
        model = serviceCallLogRepository.save(model);
        return model;
    }
    
    public ServiceCallLogMysqlModel update(String callLogId, String responsePartnerId, String responsePartnerName,
            String responseId, String responseData, int responseCode, String responseStatus, Long spendTime) {
        ServiceCallLogMysqlModel model = serviceCallLogRepository.findOne("id", callLogId, ServiceCallLogMysqlModel.class);
        if (null != model) {
            model.setResponsePartnerId(responsePartnerId);
            model.setResponsePartnerName(responsePartnerName);
            model.setResponseId(responseId);
            model.setResponseData(ServiceUtil.abbreviate(responseData, 2500));
            model.setResponseCode(responseCode);
            model.setResponseStatus(responseStatus);
            model.setSpendTime(spendTime);
            model.setUpdatedTime(new Date());
            model = serviceCallLogRepository.save(model);
        }
        return model;
    }
    
    /**
     * 根据参数获取服务调用日志
     *
     * @param input
     * @return
     */
    public List<ServiceCallLogMysqlModel> getByParams(ServiceCallLogInput input) {
        Specification<ServiceCallLogMysqlModel> where = Where.create()
                .equal("orderId", input.getOrderId())
                .equal("callByMe", input.getCallByMe())
                .equal("requestPartnerId", input.getRequestPartnerId())
                .equal("responsePartnerId", input.getResponsePartnerId())
                .equal("serviceId", input.getServiceId())
                .equal("requestId", input.getRequestId())
                .equal("responseId", input.getResponseId())
                .betweenAndDate("createdTime", input.getStartTime() == null ? null : input.getStartTime().getTime(), input.getEndTime() == null ? null : input.getEndTime().getTime())
                .build(ServiceCallLogMysqlModel.class);

        return serviceCallLogRepository.findAll(where);
    }


}
