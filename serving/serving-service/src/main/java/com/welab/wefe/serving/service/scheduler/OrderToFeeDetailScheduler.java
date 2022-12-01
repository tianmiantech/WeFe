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
package com.welab.wefe.serving.service.scheduler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.HostUtil;
import com.welab.wefe.serving.service.database.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.entity.FeeConfigMysqlModel;
import com.welab.wefe.serving.service.database.entity.FeeDetailMysqlModel;
import com.welab.wefe.serving.service.database.entity.PartnerMysqlModel;
import com.welab.wefe.serving.service.database.entity.ServiceOrderMysqlModel;
import com.welab.wefe.serving.service.dto.ServiceOrderInput;
import com.welab.wefe.serving.service.enums.CallByMeEnum;
import com.welab.wefe.serving.service.enums.ServiceOrderEnum;
import com.welab.wefe.serving.service.service.ClientServiceService;
import com.welab.wefe.serving.service.service.FeeConfigService;
import com.welab.wefe.serving.service.service.FeeDetailService;
import com.welab.wefe.serving.service.service.PartnerService;
import com.welab.wefe.serving.service.service.ServiceOrderService;

/**
 * 用于定时将订单信息转化为费用记录
 *
 * @author ivenn.zheng
 * @date 2021/12/24
 */
@Component
public class OrderToFeeDetailScheduler {

    private Logger logger = LoggerFactory.getLogger(OrderToFeeDetailScheduler.class);

    @Autowired
    private FeeDetailService feeDetailService;

    @Autowired
    private FeeConfigService feeConfigService;

    @Autowired
    private ClientServiceService clientServiceService;

    @Autowired
    private ServiceOrderService serviceOrderService;
    
    @Autowired
    private PartnerService partnerService;

    @Scheduled(cron = "0 0-59 * * * ?")
    public void feeRecord() {

        try {
            logger.info("ApiRequestToFeeDetailScheduler start in: " + DateUtil.getCurrentDate());

            Date endTime = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            calendar.set(Calendar.SECOND, 0);
            calendar.setTime(endTime);
            // 统计前1分钟
            calendar.add(Calendar.MINUTE, -1);
            Date startTime = calendar.getTime();

            ServiceOrderInput input = new ServiceOrderInput();
            // 不统计正在进行时的订单
            input.setStatus(ServiceOrderEnum.ORDERING.getValue());
            input.setUpdatedStartTime(startTime);
            input.setUpdatedEndTime(endTime);
            // 非我方发起的订单
            input.setOrderType(CallByMeEnum.NO.getCode());

            List<ServiceOrderMysqlModel> orders = serviceOrderService.getByParams(input);
            Map<String, List<ServiceOrderMysqlModel>> collect = orders.stream()
                    .collect(Collectors.toMap(k -> k.getServiceId() + "," + k.getRequestPartnerId(), v -> {
                        List<ServiceOrderMysqlModel> list = new ArrayList<>();
                        list.add(v);
                        return list;
                    }, (v1, v2) -> {
                        v1.addAll(v2);
                        return v1;
                    }));

            collect.forEach((k, groupOrders) -> {
                if (!groupOrders.isEmpty()) {
                    ServiceOrderMysqlModel order = groupOrders.get(0);
                    FeeConfigMysqlModel feeConfigMysqlModel = feeConfigService.queryOne(order.getServiceId(),
                            order.getRequestPartnerId());
                    PartnerMysqlModel partnerMysqlModel = partnerService.queryByCode(order.getRequestPartnerId());
                    ClientServiceMysqlModel clientServiceMysqlModel = clientServiceService
                            .queryByIdAndServiceId(partnerMysqlModel.getId(), order.getServiceId());
                    if (feeConfigMysqlModel == null) {
                        logger.error("client service cannot set fee config, serviceId: "
                                + clientServiceMysqlModel.getServiceId() + ", clientId: "
                                + clientServiceMysqlModel.getClientId());
                        return;
                    }

                    Double unitPrice = feeConfigMysqlModel.getUnitPrice();

                    // cal total fee
                    BigDecimal totalFee = BigDecimal.valueOf(groupOrders.size() * unitPrice);

                    // save fee detail
                    FeeDetailMysqlModel feeDetailMysqlModel = new FeeDetailMysqlModel();
                    feeDetailMysqlModel.setTotalRequestTimes((long) groupOrders.size());
                    feeDetailMysqlModel.setTotalFee(totalFee);
                    feeDetailMysqlModel.setClientId(feeConfigMysqlModel.getClientId());
                    feeDetailMysqlModel.setServiceId(feeConfigMysqlModel.getServiceId());
                    feeDetailMysqlModel.setUnitPrice(unitPrice);
                    feeDetailMysqlModel.setFeeConfigId(feeConfigMysqlModel.getId());
                    feeDetailMysqlModel.setPayType(feeConfigMysqlModel.getPayType());
                    feeDetailMysqlModel.setCreatedTime(endTime);
                    // 其他信息
                    feeDetailMysqlModel.setClientName(clientServiceMysqlModel.getClientName());
                    feeDetailMysqlModel.setServiceType(clientServiceMysqlModel.getServiceType());
                    feeDetailMysqlModel.setServiceName(clientServiceMysqlModel.getServiceName());
                    feeDetailMysqlModel.setSaveIp(HostUtil.getLocalIp());

                    feeDetailService.save(feeDetailMysqlModel);
                    logger.info("save fee detail by the scheduler in: " + DateUtil.getCurrentDate() + ", service id: "
                            + clientServiceMysqlModel.getServiceId() + ", request partner id: "
                            + clientServiceMysqlModel.getClientId() + ", startTime: "
                            + DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(startTime) + ", endTime: "
                            + DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(endTime) + ", saveIp: " + HostUtil.getLocalIp());
                }

            });
            logger.info("OrderToFeeDetailScheduler end.");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("error occur when fee details scheduler execute, happen in " + DateUtil.getCurrentDate());
        }

    }
}
