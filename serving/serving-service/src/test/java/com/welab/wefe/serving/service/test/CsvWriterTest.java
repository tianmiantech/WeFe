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
package com.welab.wefe.serving.service.test;

import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.serving.service.database.serving.entity.ApiRequestRecordMysqlModel;
import com.welab.wefe.serving.service.enums.ServiceResultEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.LineDelimiter;
import de.siegmar.fastcsv.writer.QuoteStrategy;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zane
 * @date 2022/3/2
 */
public class CsvWriterTest {
    public static void main(String[] args) throws IOException {
        final StringWriter sw = new StringWriter();

        CsvWriter csvWriter = CsvWriter.builder()
                .fieldSeparator(',')
                .quoteStrategy(QuoteStrategy.EMPTY)
                .lineDelimiter(LineDelimiter.LF)
                .build(sw);

        csvWriter.writeRow("Id", "服务Id", "服务名称", "服务类型", "客户Id", "客户名称",
                "调用IP", "请求结果", "请求时间");

        List<ApiRequestRecordMysqlModel> dataList = getData();

        for (ApiRequestRecordMysqlModel model : dataList) {
            csvWriter.writeRow(
                    model.getId(),
                    model.getServiceId(),
                    model.getServiceName(),
                    ServiceTypeEnum.getValue(model.getServiceType()),
                    model.getClientId(),
                    model.getClientName(),
                    model.getIpAdd(),
                    ServiceResultEnum.getValueByCode(model.getRequestResult()),
                    DateUtil.toString(model.getCreatedTime(), DateUtil.YYYY_MM_DD_HH_MM_SS2)
            );
        }

        File csvFile = Paths.get("/Users/Zane/data/csv_test_without_bom.csv").toFile();
        if (!csvFile.exists()) {
            File file = new File(csvFile.getParent());
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8));
        bw.write('\ufeff');
        bw.write(sw.toString());
        bw.flush();
        bw.close();
    }

    private static List<ApiRequestRecordMysqlModel> getData() {
        List<ApiRequestRecordMysqlModel> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ApiRequestRecordMysqlModel model = new ApiRequestRecordMysqlModel();
            model.setId("id" + i);
            model.setServiceId("serviceId" + i);
            model.setServiceName("服务名称" + i);
            model.setServiceType(i % 2);
            model.setClientId("客户Id" + i);
            model.setClientName("客户名称" + i);
            model.setIpAdd("ip_add" + i);
            model.setRequestResult(i % 2);
            model.setCreatedTime(new Date());
            list.add(model);
        }
        return list;
    }

}
