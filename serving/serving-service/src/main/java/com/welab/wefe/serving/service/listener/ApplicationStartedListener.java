/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.serving.service.listener;

import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.mpc.pir.server.PrivateInformationRetrievalServer;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.database.serving.entity.*;
import com.welab.wefe.serving.service.dto.OrderStatisticsInput;
import com.welab.wefe.serving.service.dto.ServiceOrderInput;
import com.welab.wefe.serving.service.enums.ServiceOrderEnum;
import com.welab.wefe.serving.service.service.*;
import com.welab.wefe.serving.service.utils.RedisIntermediateCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author ivenn.zheng
 */
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartedListener.class);

    @Autowired
    private FeeDetailService feeDetailService;

    @Autowired
    private ClientServiceService clientServiceService;

    @Autowired
    private ApiRequestRecordService apiRequestRecordService;

    @Autowired
    private FeeConfigService feeConfigService;

    @Autowired
    private ServiceOrderService serviceOrderService;

    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @Autowired
    private Config config;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
//        checkAndSaveFeeDetail();
        checkAndSaveOrderStatistics();
        logger.info("config wefe.service.cache.type=" + config.getServiceCacheType());
        // init PrivateInformationRetrievalServer
        if ("redis".equalsIgnoreCase(config.getServiceCacheType())) {
            PrivateInformationRetrievalServer.init(100, new RedisIntermediateCache(config.getRedisHost(),
                    Integer.valueOf(config.getRedisPort()), config.getRedisPassword()));
            logger.info("init RedisIntermediateCache");
        } else {
            PrivateInformationRetrievalServer.init(100);
            logger.info("init LocalIntermediateCache");
        }
    }


    public void checkAndSaveOrderStatistics() {
        // 获取1h之前的时间
        String now = DateUtil.getCurrentDate();
        Date oneHourAgo = DateUtil.addHours(DateUtil.fromString(now, DateUtil.YYYY_MM_DD_HH_MM_SS2), -1);

        ServiceOrderInput input = new ServiceOrderInput();
        input.setUpdatedStartTime(oneHourAgo);
        input.setUpdatedEndTime(DateUtil.fromString(now, DateUtil.YYYY_MM_DD_HH_MM_SS2));
        // 排除进行中的订单
        input.setStatus(ServiceOrderEnum.ORDERING.getValue());
        List<ServiceOrderMysqlModel> serviceOrderMysqlModelList = serviceOrderService.getByParams(input);
        // 进行分组
        Map<String, Map<String, Map<String, List<ServiceOrderMysqlModel>>>> collect = serviceOrderMysqlModelList.stream().collect(
                Collectors.groupingBy(ServiceOrderMysqlModel::getServiceId,
                        Collectors.groupingBy(ServiceOrderMysqlModel::getRequestPartnerId,
                                Collectors.groupingBy(x -> DateUtil.toString(x.getUpdatedTime(), DateUtil.YYYY_MM_DD_HH_MM))))
        );

        List<OrderStatisticsMysqlModel> orderStatisticsMysqlModels = new ArrayList<>();

        collect.forEach((k, v) -> {
            v.forEach((k1, v2) -> {
                v2.forEach((str, list) -> {
                    ServiceOrderMysqlModel serviceOrderMysqlModel = list.get(0);
                    OrderStatisticsMysqlModel os = new OrderStatisticsMysqlModel();
                    int successTimes = (int) list.stream().filter(x ->
                            ServiceOrderEnum.SUCCESS.getValue().equals(x.getStatus())).count();

                    int failedTimes = (int) list.stream().filter(x ->
                            ServiceOrderEnum.FAILED.getValue().equals(x.getStatus())).count();

                    Date date = DateUtil.addMinutes(serviceOrderMysqlModel.getUpdatedTime(), 1);
                    // 秒位置0
                    date.setSeconds(0);
                    os.setCallTimes(list.size());
                    os.setSuccessTimes(successTimes);
                    os.setFailedTimes(failedTimes);
                    os.setMinute(DateUtil.toString(date, DateUtil.YYYY_MM_DD_HH_MM));
                    os.setHour(DateUtil.toString(date, DateUtil.YYYY_MM_DD_HH));
                    os.setDay(DateUtil.toString(date, DateUtil.YYYY_MM_DD));
                    os.setMonth(DateUtil.toString(date, DateUtil.YYYY_MM));
                    os.setRequestPartnerId(serviceOrderMysqlModel.getRequestPartnerId());
                    os.setRequestPartnerName(serviceOrderMysqlModel.getRequestPartnerName());
                    os.setResponsePartnerId(serviceOrderMysqlModel.getResponsePartnerId());
                    os.setResponsePartnerName(serviceOrderMysqlModel.getResponsePartnerName());
                    os.setServiceId(serviceOrderMysqlModel.getServiceId());
                    os.setServiceName(serviceOrderMysqlModel.getServiceName());
                    // 此处填写本应保存的时间，即和date 一致
                    os.setCreatedTime(date);
                    os.setUpdatedTime(date);
                    orderStatisticsMysqlModels.add(os);
                });
            });
        });


        // 统计时间段内未进行的订单统计
        orderStatisticsMysqlModels.forEach(x -> {
            OrderStatisticsInput input1 = new OrderStatisticsInput();
            input1.setServiceId(x.getServiceId());
            input1.setRequestPartnerId(x.getRequestPartnerId());
            input1.setMinute(x.getMinute());
            List<OrderStatisticsMysqlModel> orderStatisticsMysqlModelList = orderStatisticsService.getByParams(input1);
            if (orderStatisticsMysqlModelList.size() == 0) {
                orderStatisticsService.insert(x);
            }
        });


    }


    public void checkAndSaveFeeDetail() {

        List<ClientServiceMysqlModel> clientServiceMysqlModels = clientServiceService.getAll();

        // 获取1h之前的时间
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        calendar.setTime(now);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date oneHourAgo = calendar.getTime();

        for (ClientServiceMysqlModel model : clientServiceMysqlModels) {
            FeeDetailMysqlModel feeDetailMysqlModel = feeDetailService.getByIdAndDateTime(model.getServiceId(), model.getClientId(), oneHourAgo);
            if (feeDetailMysqlModel == null) {
                // 上个小时无记录，需要即刻统计
                addFeeDetailRecord(model.getServiceId(), model.getClientId(), oneHourAgo);
            }
        }


    }


    public void addFeeDetailRecord(String serviceId, String clientId, Date endTime) {

        try {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endTime);
            calendar.add(Calendar.HOUR, -1);
            Date startTime = calendar.getTime();

            // get request records
            List<ApiRequestRecordMysqlModel> list = apiRequestRecordService.getList(serviceId, clientId, startTime, endTime);
            if (list.size() != 0) {
                ApiRequestRecordMysqlModel apiRequestRecordMysqlModel = list.get(0);
                FeeConfigMysqlModel feeConfigMysqlModel = feeConfigService.queryOne(apiRequestRecordMysqlModel.getServiceId(), apiRequestRecordMysqlModel.getClientId());
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
                feeDetailMysqlModel.setClientName(apiRequestRecordMysqlModel.getClientName());
                feeDetailMysqlModel.setServiceType(apiRequestRecordMysqlModel.getServiceType());
                feeDetailMysqlModel.setServiceName(apiRequestRecordMysqlModel.getServiceName());

                feeDetailService.save(feeDetailMysqlModel);

                logger.info("save fee detail by the listener in: " + DateUtil.getCurrentDate() + ", service id: "
                        + apiRequestRecordMysqlModel.getServiceId() + ", client id: " + apiRequestRecordMysqlModel.getClientId()
                        + ", startTime: " + DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(startTime)
                        + ", endTime: " + DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(endTime));
            } else {
                logger.info("there is no request record between startTime: " + startTime + " and endTime: " + endTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error occur when fee details scheduler execute, happen in " + DateUtil.getCurrentDate());
        }
    }
}
