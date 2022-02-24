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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.mpc.pir.server.PrivateInformationRetrievalServer;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.database.serving.entity.ApiRequestRecordMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.FeeConfigMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.FeeDetailMysqlModel;
import com.welab.wefe.serving.service.service.ApiRequestRecordService;
import com.welab.wefe.serving.service.service.ClientServiceService;
import com.welab.wefe.serving.service.service.FeeConfigService;
import com.welab.wefe.serving.service.service.FeeDetailService;
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
    private ApiRequestRecordService apiRequestRecordService;

    @Autowired
    private FeeConfigService feeConfigService;
    
    @Autowired
    private Config config;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        checkAndSaveFeeDetail();
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


    /**
     *
     */
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
