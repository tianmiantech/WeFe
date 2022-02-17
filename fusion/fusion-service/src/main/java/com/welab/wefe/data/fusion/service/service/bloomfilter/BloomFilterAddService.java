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

package com.welab.wefe.data.fusion.service.service.bloomfilter;


import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.api.bloomfilter.AddApi;
import com.welab.wefe.data.fusion.service.database.entity.BloomFilterMySqlModel;
import com.welab.wefe.data.fusion.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.BloomFilterRepository;
import com.welab.wefe.data.fusion.service.enums.DataResourceSource;
import com.welab.wefe.data.fusion.service.enums.Progress;
import com.welab.wefe.data.fusion.service.manager.JdbcManager;
import com.welab.wefe.data.fusion.service.service.AbstractService;
import com.welab.wefe.data.fusion.service.service.FieldInfoService;
import com.welab.wefe.data.fusion.service.service.dataset.DataSetService;
import com.welab.wefe.data.fusion.service.utils.AbstractDataSetReader;
import com.welab.wefe.data.fusion.service.utils.CsvDataSetReader;
import com.welab.wefe.data.fusion.service.utils.ExcelDataSetReader;
import com.welab.wefe.data.fusion.service.utils.bf.BloomFilters;
import com.welab.wefe.data.fusion.service.utils.primarykey.FieldInfo;
import com.welab.wefe.data.fusion.service.utils.primarykey.PrimaryKeyUtils;
import com.welab.wefe.fusion.core.utils.CryptoUtils;
import com.welab.wefe.fusion.core.utils.PSIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Adding a Filter
 *
 * @author Jacky.jiang
 */
@Service
public class BloomFilterAddService extends AbstractService {

    @Autowired
    protected BloomFilterRepository bloomFilterRepository;

    @Autowired
    protected DataSetService dataSetService;

    @Value("${file.upload.dir}")
    private String fileUploadDir;

    @Value("${file.filter.dir}")
    private String filterDir;

    @Autowired
    FieldInfoService fieldInfoService;

    public AddApi.BloomfilterAddOutput addFilter(AddApi.Input input) throws Exception {
        List<FieldInfo> fieldInfos = input.getFieldInfoList();
        int count = 0;
        for (FieldInfo info : fieldInfos) {
            count = count + info.getColumnList().size();
        }

        if (count > 5) {
            throw new StatusCodeWithException("选择字段数量不宜超过5", StatusCode.PARAMETER_VALUE_INVALID);
        }

        BloomFilterMySqlModel model = bloomFilterRepository.findOne("id", input.getId(), BloomFilterMySqlModel.class);
        if (model == null) {
            model = new BloomFilterMySqlModel();
            model.setName(input.getName());
            model.setDataSourceId(input.getDataSourceId());
            model.setUpdatedBy(CurrentAccount.id());
            model.setCreatedBy(CurrentAccount.id());
            model.setDescription(input.getDescription());
            model.setDataResourceSource(input.getDataResourceSource());
            model.setRows(StringUtil.join(input.getRows(), ','));
            fieldInfoService.saveAll(model.getId(), input.getFieldInfoList());

            model.setUsedCount(0);
            model.setUpdatedTime(new Date());
            bloomFilterRepository.save(model);
        }

        if (DataResourceSource.Sql.equals(input.getDataResourceSource())) {
            readAndSaveFromDB(model, input.getName(), input.getDataSourceId(), input.getRows(), input.getSql(), input.isDeduplication());
            model.setStatement(input.getSql());
        } else {

            File file = dataSetService.getDataSetFile(input.getDataResourceSource(), input.getFilename());
            // Parse and save the dataset file
            try {
                //Read from the data file and generate a filter
                readAndSaveFile(model, input.getName(), file, input.getRows(), input.isDeduplication());
                model.setSourcePath(input.getFilename());
            } catch (IOException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "文件读取失败！");
            }
        }

        model.setUsedCount(0);
        model.setUpdatedTime(new Date());
        bloomFilterRepository.save(model);

        AddApi.BloomfilterAddOutput output = new AddApi.BloomfilterAddOutput();
        output.setDataSourceId(model.getId());
        return output;
    }


    /**
     * Parse the dataset file and save it to mysql
     *
     * @param deduplication Whether the data set needs to be deduplicated
     * @return Returns the number of repeated rows of data in a dataset
     */
    private int readAndSaveFile(BloomFilterMySqlModel model, String name, File file, List<String> rows, boolean deduplication) throws IOException, StatusCodeWithException {
        long startTime = System.currentTimeMillis();
        LOG.info("Start parsing the data set：" + model.getId());

        long fileLength = file.length();
        LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
        lineNumberReader.skip(fileLength);
        int rowsCount = lineNumberReader.getLineNumber() - 1;
        lineNumberReader.close();
        BloomFilterRepository bloomFilterRepository = Launcher.CONTEXT.getBean(BloomFilterRepository.class);
        bloomFilterRepository.updateById(model.getId(), "process", Progress.Ready, BloomFilterMySqlModel.class);
        bloomFilterRepository.updateById(model.getId(), "rowCount", rowsCount, BloomFilterMySqlModel.class);

        BloomFilterMySqlModel bloomFilterMySqlModel = bloomFilterRepository.getOne(model.getId());
        int processCount = 0;
        if (bloomFilterMySqlModel != null) {
            processCount = bloomFilterMySqlModel.getProcessCount();
        }

        boolean isCsv = file.getName().endsWith("csv");

        AbstractDataSetReader dataSetReader = isCsv
                ? new CsvDataSetReader(file)
                : new ExcelDataSetReader(file);

        List<String> headers = dataSetReader.getHeader();

        String src = filterDir + name;
        File outFile = new File(filterDir);

        if (!outFile.exists() && !outFile.isDirectory()) {
            outFile.mkdir();
        }

        BloomFilterAddServiceDataRowConsumer bloomFilterAddServiceDataRowConsumer = new BloomFilterAddServiceDataRowConsumer(model, deduplication, file, rowsCount, processCount, rows, src);
        // Read all rows of data

        int finalProcessCount = processCount;
        CommonThreadPool.run(() -> {
            try {
                dataSetReader.readAllWithSelectRow(bloomFilterAddServiceDataRowConsumer, rows, finalProcessCount);
            } catch (StatusCodeWithException e) {

            } catch (IOException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }

        });


        System.out.println("-----------------ThreadPoolExecutor Time used:" + (System.currentTimeMillis() - startTime) + "ms");

        //Verify generated filters
        String id = model.getId();
        List<Object> CheckData = bloomFilterAddServiceDataRowConsumer.getCheckData();
        BigInteger N = bloomFilterAddServiceDataRowConsumer.getN();
        BigInteger e = bloomFilterAddServiceDataRowConsumer.getE();
        BigInteger d = bloomFilterAddServiceDataRowConsumer.getD();
        BloomFilters bf = bloomFilterAddServiceDataRowConsumer.getBf();
        boolean checkFlag = CheckFilter(id, N, e, d, CheckData, bf);

        if (checkFlag) {
            model.setD(bloomFilterAddServiceDataRowConsumer.getD().toString());
            model.setN(bloomFilterAddServiceDataRowConsumer.getN().toString());
            model.setE(bloomFilterAddServiceDataRowConsumer.getE().toString());
            model.setSrc(src);
            System.out.println("Generating filter：" + bloomFilterAddServiceDataRowConsumer.getBf());

            return rowsCount;
        } else {
            return 0;
        }
    }


    /**
     * Read data from the specified database according to SQL and save to mysql
     *
     * @param model
     * @param sql
     * @throws StatusCodeWithException
     */
    public int readAndSaveFromDB(BloomFilterMySqlModel model, String name, String dataSourceId, List<String> headers, String sql, boolean deduplication) throws Exception {
        long startTime = System.currentTimeMillis();

        BloomFilterMySqlModel bloomFilterMySqlModel = bloomFilterRepository.getOne(model.getId());
        int processCount = bloomFilterMySqlModel.getProcessCount();


        DataSourceMySqlModel dsModel = dataSetService.getDataSourceById(dataSourceId);
        if (dsModel == null) {
            throw new StatusCodeWithException("dataSourceId在数据库不存在", StatusCode.DATA_NOT_FOUND);
        }

        JdbcManager jdbcManager = new JdbcManager();
        Connection conn = jdbcManager.getConnection(dsModel.getDatabaseType(), dsModel.getHost(), dsModel.getPort()
                , dsModel.getUserName(), dsModel.getPassword(), dsModel.getDatabaseName());

        int rowsCount = (int) jdbcManager.count(conn, sql);
        BloomFilterRepository bloomFilterRepository = Launcher.CONTEXT.getBean(BloomFilterRepository.class);
        bloomFilterRepository.updateById(model.getId(), "process", Progress.Ready, BloomFilterMySqlModel.class);
        bloomFilterRepository.updateById(model.getId(), "rowCount", rowsCount, BloomFilterMySqlModel.class);

        String src = filterDir + name;
        File outFile = new File(filterDir);
        //Create a folder if it does not exist
        if (!outFile.exists() && !outFile.isDirectory()) {
            outFile.mkdir();
        }

        BloomFilterAddServiceDataRowConsumer bloomFilterAddServiceDataRowConsumer = new BloomFilterAddServiceDataRowConsumer(model, deduplication, rowsCount, processCount, headers, src);
        jdbcManager.readWithSelectRow(conn, sql, bloomFilterAddServiceDataRowConsumer, headers);

        bloomFilterAddServiceDataRowConsumer.waitForFinishAndClose();
        System.out.println("-----------------ThreadPoolExecutor Time used:" + (System.currentTimeMillis() - startTime) + "ms");

        return rowsCount;
    }


    public boolean CheckFilter(String id, BigInteger N, BigInteger e, BigInteger d, List<Object> CheckData, BloomFilters bf) {
        FieldInfoService service = Launcher.CONTEXT.getBean(FieldInfoService.class);
        List<FieldInfo> fieldInfoList = service.fieldInfoList(id);
        List<String> data = new ArrayList<>();
        List<BigInteger> r_ = new ArrayList<>();
        BigInteger blindFactor;
        List<BigInteger> r_inv = new ArrayList<>();

        BigInteger ZERO = BigInteger.valueOf(0);
        BigInteger ONE = BigInteger.valueOf(1);
        int length = N.bitLength() - 1;
        BigInteger gcd;
        do {
            blindFactor = new BigInteger(length, new SecureRandom());
            gcd = blindFactor.gcd(N);
        }
        while (blindFactor.equals(ZERO) || blindFactor.equals(ONE) || !gcd.equals(ONE));
        byte[][] query = new byte[CheckData.size()][];
        for (int i = 0; i < CheckData.size(); i++) {

            //Handle the primary key according to the keyPrimary method
            String key = PrimaryKeyUtils.create(JObject.create(CheckData.get(i)), fieldInfoList);
            data.add(key);

            BigInteger h = PSIUtils.stringToBigInteger(key);
            r_.add(blindFactor.modPow(e, N));
            r_inv.add(blindFactor.modInverse(N));
            BigInteger x = h.multiply(r_.get(i)).mod(N);
            query[i] = PSIUtils.bigIntegerToBytes(x, false);
        }

        byte[][] result = CryptoUtils.sign(N, d, query);
        long start = System.currentTimeMillis();
        int check = 0;
        for (int i = 0; i < result.length; i++) {

            BigInteger y = PSIUtils.bytesToBigInteger(result[i], 0, result[i].length);
            BigInteger z = y.multiply(r_inv.get(i)).mod(N);

            if (bf.contains(z)) {
                System.out.println("匹配成功");
                check++;
            } else {
                System.out.println("匹配失败");
            }

        }

        LOG.info("client y.mod(N) spend : " + (System.currentTimeMillis() - start) + " ms");
        if (check > 0) {
            return true;
        }
        return false;
    }
}
