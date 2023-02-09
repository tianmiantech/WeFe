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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.mpc.pir.server.PrivateInformationRetrievalServer;
import com.welab.wefe.serving.service.database.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.entity.FeeConfigMysqlModel;
import com.welab.wefe.serving.service.database.entity.FeeDetailMysqlModel;
import com.welab.wefe.serving.service.database.entity.OrderStatisticsMysqlModel;
import com.welab.wefe.serving.service.database.entity.PartnerMysqlModel;
import com.welab.wefe.serving.service.database.entity.ServiceOrderMysqlModel;
import com.welab.wefe.serving.service.dto.ServiceOrderInput;
import com.welab.wefe.serving.service.dto.globalconfig.ServiceCacheConfigModel;
import com.welab.wefe.serving.service.enums.CallByMeEnum;
import com.welab.wefe.serving.service.enums.ServiceClientTypeEnum;
import com.welab.wefe.serving.service.enums.ServiceOrderEnum;
import com.welab.wefe.serving.service.service.ClientServiceService;
import com.welab.wefe.serving.service.service.FeeConfigService;
import com.welab.wefe.serving.service.service.FeeDetailService;
import com.welab.wefe.serving.service.service.OrderStatisticsService;
import com.welab.wefe.serving.service.service.PartnerService;
import com.welab.wefe.serving.service.service.ServiceOrderService;
import com.welab.wefe.serving.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.serving.service.utils.RedisIntermediateCache;

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
    private FeeConfigService feeConfigService;

    @Autowired
    private ServiceOrderService serviceOrderService;

    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private PartnerService partnerService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        checkAndSaveFeeDetail();
        checkAndSaveOrderStatistics();
        ServiceCacheConfigModel cacheConfigModel = globalConfigService.getModel(ServiceCacheConfigModel.class);

        if (cacheConfigModel == null) {
            return;
        }

        logger.info("config wefe.service.cache.type=" + cacheConfigModel.getType());
        // init PrivateInformationRetrievalServer
        if (ServiceCacheConfigModel.CacheType.redis.equals(cacheConfigModel.getType())) {
            PrivateInformationRetrievalServer.init(100, new RedisIntermediateCache(cacheConfigModel.getRedisHost(),
                    Integer.valueOf(cacheConfigModel.getRedisPort()), cacheConfigModel.getRedisPassword()));
            logger.info("init RedisIntermediateCache");
        } else {
            PrivateInformationRetrievalServer.init(100);
            logger.info("init LocalIntermediateCache");
        }
    }

    public void checkAndSaveOrderStatistics() {
        String now = DateUtil.getCurrentDate2();
        // 获取 order_statistics 表中最新一次统计的时间
        OrderStatisticsMysqlModel lastRecord = orderStatisticsService.getLastRecord();
        Date lastRecordCreatedTime = new Date();
        if (lastRecord != null) {
            // 获取最后记录时间
            lastRecordCreatedTime = lastRecord.getCreatedTime();
        }

        ServiceOrderInput input = new ServiceOrderInput();
        input.setUpdatedStartTime(lastRecordCreatedTime);
        input.setUpdatedEndTime(DateUtil.fromString(now, DateUtil.YYYY_MM_DD_HH_MM_SS3));
        // 排除进行中的、我方自调的订单
        input.setStatus(ServiceOrderEnum.ORDERING.getValue());
        input.setOrderType(CallByMeEnum.NO.getCode());

        List<ServiceOrderMysqlModel> serviceOrderMysqlModelList = serviceOrderService.getByParams(input);
        // 进行分组
        Map<String, Map<String, Map<String, List<ServiceOrderMysqlModel>>>> collect = serviceOrderMysqlModelList
                .stream()
                .collect(Collectors.groupingBy(ServiceOrderMysqlModel::getServiceId, Collectors.groupingBy(
                        ServiceOrderMysqlModel::getRequestPartnerId,
                        Collectors.groupingBy(x -> DateUtil.toString(x.getUpdatedTime(), DateUtil.YYYY_MM_DD_HH_MM)))));

        List<OrderStatisticsMysqlModel> orderStatisticsMysqlModels = new ArrayList<>();

        collect.forEach((k, v) -> {
            v.forEach((k1, v2) -> {
                v2.forEach((str, list) -> {
                    ServiceOrderMysqlModel serviceOrderMysqlModel = list.get(0);
                    OrderStatisticsMysqlModel os = new OrderStatisticsMysqlModel();
                    int successTimes = (int) list.stream()
                            .filter(x -> ServiceOrderEnum.SUCCESS.getValue().equals(x.getStatus())).count();

                    int failedTimes = (int) list.stream()
                            .filter(x -> ServiceOrderEnum.FAILED.getValue().equals(x.getStatus())).count();

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
            orderStatisticsService.insert(x);
        });

    }

    public void checkAndSaveFeeDetail() {
        List<ClientServiceMysqlModel> clientServiceMysqlModels = clientServiceService.getAll();
        FeeDetailMysqlModel lastRecord = feeDetailService.getLastRecord();
        Date lastRecordTime = new Date();
        if (lastRecord != null) {
            // 获取最后记录时间
            lastRecordTime = lastRecord.getCreatedTime();
        }
        for (ClientServiceMysqlModel model : clientServiceMysqlModels) {
            if (model == null || model.getType() == null || model.getType() == ServiceClientTypeEnum.ACTIVATE.getValue()) {
                continue;
            }
            // 统计开通服务
            PartnerMysqlModel partnerServiceOne = partnerService.findOne(model.getClientId());
            if(partnerServiceOne == null) {
                continue;
            }
            addFeeDetailRecord(model.getServiceId(), partnerServiceOne.getCode(), lastRecordTime);
        }
    }

    public void addFeeDetailRecord(String serviceId, String clientId, Date endTime) {

        try {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endTime);
            calendar.add(Calendar.HOUR, -1);
            Date startTime = calendar.getTime();

            // get request records
            ServiceOrderInput input = new ServiceOrderInput();
            input.setStatus(ServiceOrderEnum.ORDERING.getValue());
            // 不统计我方调起的订单
            input.setOrderType(CallByMeEnum.NO.getCode());
            input.setServiceId(serviceId);
            input.setRequestPartnerId(clientId);
            input.setUpdatedStartTime(startTime);
            input.setUpdatedEndTime(endTime);
            List<ServiceOrderMysqlModel> list = serviceOrderService.getByParams(input);

            if (!list.isEmpty()) {
                ServiceOrderMysqlModel serviceOrderMysqlModel = list.get(0);
                FeeConfigMysqlModel feeConfigMysqlModel = feeConfigService
                        .queryOne(serviceOrderMysqlModel.getServiceId(), serviceOrderMysqlModel.getRequestPartnerId());
                Double unitPrice = feeConfigMysqlModel.getUnitPrice();
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
                feeDetailMysqlModel.setClientName(serviceOrderMysqlModel.getRequestPartnerName());
                feeDetailMysqlModel.setServiceType(serviceOrderMysqlModel.getServiceType());
                feeDetailMysqlModel.setServiceName(serviceOrderMysqlModel.getServiceName());

                feeDetailService.save(feeDetailMysqlModel);

                logger.info("save fee detail by the listener in: " + DateUtil.getCurrentDate() + ", service id: "
                        + serviceOrderMysqlModel.getServiceId() + ", request partner id: "
                        + serviceOrderMysqlModel.getRequestPartnerId() + ", startTime: "
                        + DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(startTime) + ", endTime: "
                        + DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(endTime));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("error occur when fee details scheduler execute, happen in " + DateUtil.getCurrentDate());
        }
    }
}
