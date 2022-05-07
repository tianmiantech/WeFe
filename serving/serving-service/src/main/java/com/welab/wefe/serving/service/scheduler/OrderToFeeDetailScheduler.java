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


import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.serving.service.database.serving.entity.*;
import com.welab.wefe.serving.service.dto.ServiceOrderInput;
import com.welab.wefe.serving.service.enums.ServiceOrderEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * 用于定时将订单信息转化为费用记录
 *
 * @author ivenn.zheng
 * @date 2021/12/24
 */
//@Component
public class OrderToFeeDetailScheduler {

    private Logger logger = LoggerFactory.getLogger(OrderToFeeDetailScheduler.class);

    @Autowired
    private FeeDetailService feeDetailService;

    @Autowired
    private ApiRequestRecordService apiRequestRecordService;

    @Autowired
    private FeeConfigService feeConfigService;

    @Autowired
    private ClientServiceService clientServiceService;

    @Autowired
    private ServiceOrderService serviceOrderService;

//    @Scheduled(cron = "0 0 0-23 * * ?")
    public void feeRecord() {

        try {
            logger.info("ApiRequestToFeeDetailScheduler start in: " + DateUtil.getCurrentDate());

            Date endTime = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.setTime(endTime);
            calendar.add(Calendar.HOUR, -1);
            Date startTime = calendar.getTime();

            List<ClientServiceMysqlModel> clientServiceMysqlModels = clientServiceService.getAll();

            for (ClientServiceMysqlModel model : clientServiceMysqlModels) {
                ServiceOrderInput input = new ServiceOrderInput();
                input.setStatus(ServiceOrderEnum.ORDERING.getValue());
                input.setServiceId(model.getServiceId());
                input.setRequestPartnerId(model.getClientId());
                input.setCreatedStartTime(startTime);
                input.setCreatedEndTime(endTime);
                // get request records
                List<ServiceOrderMysqlModel> list = serviceOrderService.getByParams(input);

                if (list.size() != 0) {

                    FeeConfigMysqlModel feeConfigMysqlModel = feeConfigService.queryOne(model.getServiceId(), model.getClientId());
                    if (feeConfigMysqlModel == null) {
                        logger.error("client service cannot set fee config, serviceId: " + model.getServiceId() + ", clientId: " + model.getClientId());
                        continue;
                    }

                    Double unitPrice = feeConfigMysqlModel.getUnitPrice();
                    if (unitPrice == 0) {
                        logger.warn("unit price is zero!");
                    }

                    // cal total fee
                    BigDecimal totalFee = BigDecimal.valueOf(list.size() * unitPrice);

                    // save fee detail
                    FeeDetailMysqlModel feeDetailMysqlModel = new FeeDetailMysqlModel();
                    feeDetailMysqlModel.setTotalRequestTimes((long) list.size());
                    feeDetailMysqlModel.setTotalFee(totalFee);
                    feeDetailMysqlModel.setClientId(feeConfigMysqlModel.getClientId());
                    feeDetailMysqlModel.setServiceId(feeConfigMysqlModel.getServiceId());
                    feeDetailMysqlModel.setUnitPrice(unitPrice);
                    feeDetailMysqlModel.setFeeConfigId(feeConfigMysqlModel.getId());
                    feeDetailMysqlModel.setPayType(feeConfigMysqlModel.getPayType());

                    // 创建时间为整点
                    feeDetailMysqlModel.setCreatedTime(endTime);
                    // 其他信息
                    feeDetailMysqlModel.setClientName(model.getClientName());
                    feeDetailMysqlModel.setServiceType(ServiceTypeEnum.getValue(model.getServiceType()));
                    feeDetailMysqlModel.setServiceName(model.getServiceName());

                    feeDetailService.save(feeDetailMysqlModel);

                    logger.info("save fee detail by the scheduler in: " + DateUtil.getCurrentDate() + ", service id: "
                            + model.getServiceId() + ", request partner id: " + model.getClientId()
                            + ", startTime: " + DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(startTime)
                            + ", endTime: " + DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(endTime));
                    logger.info("OrderToFeeDetailScheduler end.");
                } else {
                    logger.info("there is no request record between startTime: " + startTime + " and endTime: " + endTime);
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("error occur when fee details scheduler execute, happen in " + DateUtil.getCurrentDate());
        }


    }


}
