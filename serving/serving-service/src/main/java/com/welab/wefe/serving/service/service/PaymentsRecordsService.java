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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.serving.service.api.paymentsrecords.DownloadApi;
import com.welab.wefe.serving.service.api.paymentsrecords.QueryListApi;
import com.welab.wefe.serving.service.api.paymentsrecords.SaveApi;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.database.serving.entity.ClientMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.PaymentsRecordsMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ServiceMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientRepository;
import com.welab.wefe.serving.service.database.serving.repository.PaymentsRecordsRepository;
import com.welab.wefe.serving.service.database.serving.repository.ServiceRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.PaymentsTypeEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;

import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.LineDelimiter;
import de.siegmar.fastcsv.writer.QuoteStrategy;

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

    @Autowired
    private Config config;

    private static final String filePrefix = "payments_records/";

    public File downloadFile(DownloadApi.Input input) {
        String fileName = DateUtil.getCurrentDate() + "_result.csv";
        Specification<PaymentsRecordsMysqlModel> where = Where
                .create()
                .contains("serviceName", input.getServiceName())
                .contains("clientName", input.getClientName())
                .betweenAndDate("createdTime", input.getStartTime(), input.getEndTime())
                .equal("payType", input.getPayType())
                .equal("serviceType", input.getServiceType())
                .orderBy("createdTime", OrderBy.desc)
                .build(PaymentsRecordsMysqlModel.class);

        List<PaymentsRecordsMysqlModel> all = paymentsRecordsRepository.findAll(where);
        try {
            return writeCSV(all, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File writeCSV(List<PaymentsRecordsMysqlModel> dataList, String fileName) throws IOException {
        final StringWriter sw = new StringWriter();
        CsvWriter csvWriter = CsvWriter.builder()
                .fieldSeparator(',')
                .quoteStrategy(QuoteStrategy.EMPTY)
                .lineDelimiter(LineDelimiter.LF)
                .build(sw);

        csvWriter.writeRow("服务Id", "服务名称", "服务类型", "客户Id", "客户名称",
                "收支类型", "时间", "金额", "余额", "备注");

        for (PaymentsRecordsMysqlModel model : dataList) {
            csvWriter.writeRow(
                    model.getServiceId(),
                    model.getServiceName(),
                    ServiceTypeEnum.getValue(model.getServiceType()),
                    model.getClientId(),
                    model.getClientName(),
                    PaymentsTypeEnum.getValueByCode(model.getPayType()),
                    DateUtil.toString(model.getCreatedTime(), DateUtil.YYYY_MM_DD_HH_MM_SS2),
                    model.getAmount().toString(),
                    model.getBalance().toString(),
                    StringUtil.isEmptyToBlank(model.getRemark()));
        }

        File csvFile = new File(config.getFileBasePath() + filePrefix + fileName);
        if (!csvFile.exists()) {
            File file = new File(csvFile.getParent());
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8));
        bw.write(sw.toString());
        bw.flush();
        bw.close();

        return csvFile;
    }


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
                .orderBy("createdTime", OrderBy.desc)
                .build(PaymentsRecordsMysqlModel.class);
        PaymentsRecordsMysqlModel paymentsRecordsMysqlModel = paymentsRecordsRepository.findAll(where).get(0);

        if (paymentsRecordsMysqlModel != null) {
            if (input.getPayType() == PaymentsTypeEnum.RECHARGE.getCode()) {
                // 充值，余额增加
                model.setBalance(paymentsRecordsMysqlModel.getBalance().add(input.getAmount()));
            } else if (input.getPayType() == PaymentsTypeEnum.PAID.getCode()) {
                // 支出，余额减少
                model.setBalance(paymentsRecordsMysqlModel.getBalance().subtract(input.getAmount()));
            }
        } else {
            if (input.getPayType() == PaymentsTypeEnum.RECHARGE.getCode()) {
                // 充值，余额增加
                model.setBalance(new BigDecimal("0.0").add(input.getAmount()));
            } else if (input.getPayType() == PaymentsTypeEnum.PAID.getCode()) {
                // 支出，余额减少
                model.setBalance(new BigDecimal("0.0").subtract(input.getAmount()));
            }
        }

        paymentsRecordsRepository.save(model);

    }

}
