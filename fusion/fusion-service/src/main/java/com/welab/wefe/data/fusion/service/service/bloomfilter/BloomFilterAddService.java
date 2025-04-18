/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
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
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.data.fusion.service.api.bloomfilter.AddApi;
import com.welab.wefe.data.fusion.service.config.Config;
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.math.BigInteger;
import java.nio.file.Paths;
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

    @Autowired
    private Config config;

    @Autowired
    FieldInfoService fieldInfoService;


    @Transactional(rollbackFor = Exception.class)
    public AddApi.BloomfilterAddOutput addFilter(AddApi.Input input) throws Exception {
        try {
            if (StringUtils.isNotBlank(config.getBloomFilterDir())) {
                File file = Paths.get(config.getBloomFilterDir()).toFile();
                if (!file.exists()) {
                    file.mkdirs();
                }
            }
        } catch (Exception e) {
            LOG.error("mkdir " + config.getBloomFilterDir() + " error", e);
        }

        List<FieldInfo> fieldInfos = input.getFieldInfoList();
        int count = 0;
        for (FieldInfo info : fieldInfos) {
            count = count + info.getColumnList().size();
        }

        if (count > 5) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "加密组合不宜过于复杂");
        }

        BloomFilterMySqlModel model = new BloomFilterMySqlModel();

        model.setUpdatedBy(CurrentAccountUtil.get().getId());
        model.setCreatedBy(CurrentAccountUtil.get().getId());
        model.setDescription(input.getDescription());
        model.setDataResourceSource(input.getDataResourceSource());
        model.setName(input.getName());
        model.setRows(StringUtil.join(input.getRows(), ','));
        model.setUsedCount(0);
        model.setRowCount(0);
        model.setUpdatedTime(new Date());
        model.setStatement(input.getSql());
        model.setSourcePath(input.getFilename());
        model.setDataSourceId(input.getDataSourceId());
        model.setHashFunction(PrimaryKeyUtils.hashFunction(input.getFieldInfoList()));
        bloomFilterRepository.save(model);

        fieldInfoService.saveAll(model.getId(), input.getFieldInfoList());

        CommonThreadPool.TASK_SWITCH = true;

        if (DataResourceSource.Sql.equals(input.getDataResourceSource())) {
            readAndSaveFromDB(model, input.getRows());
        } else {
            // Parse and save the bloom filter file
            try {
                File file = dataSetService.getDataSetFile(input.getDataResourceSource(), input.getFilename());
                //Read from the data file and generate a filter
                readAndSaveFile(model, file, input.getRows());
            } catch (IOException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                StatusCode.FILE_IO_READ_ERROR.throwException();
            }
        }

        AddApi.BloomfilterAddOutput output = new AddApi.BloomfilterAddOutput();
        output.setDataSourceId(model.getId());
        return output;
    }


    /**
     * Parse the dataset file and save it to mysql
     *
     * @return Returns the number of repeated rows of data in a dataset
     */
    private int readAndSaveFile(BloomFilterMySqlModel model, File file, List<String> idFeatureFields) throws IOException, StatusCodeWithException {
        LOG.info("Start parsing the data set：" + model.getId());
        boolean isCsv = file.getName().endsWith("csv");
        int rowCount = 0;
        AbstractDataSetReader dataSetReader = isCsv ? new CsvDataSetReader(file) : new ExcelDataSetReader(file);
        if(isCsv) {
            long fileLength = file.length();
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
            lineNumberReader.skip(fileLength);
            rowCount = lineNumberReader.getLineNumber() - 1; // 这个方法对于xlsx会有不准确
            lineNumberReader.close();
        }
        else {
            rowCount = (int) dataSetReader.getRowCount(0) - 1; // 去除header
        }
        BloomFilterRepository bloomFilterRepository = Launcher.CONTEXT.getBean(BloomFilterRepository.class);
        bloomFilterRepository.updateById(model.getId(), "rowCount", rowCount, BloomFilterMySqlModel.class);
        model.setRowCount(rowCount);
        dataSetReader.getHeader();
        File dir = Paths.get(config.getBloomFilterDir()).toFile();
        if(!dir.exists()){
            boolean result = dir.mkdirs();
            LOG.info("mkdir " + dir.toString() + (result ? "success":"fail"));
        }
        File src = Paths.get(config.getBloomFilterDir()).resolve(model.getName()).toFile();
        model.setSrc(src.toString());

        BloomFilterAddServiceDataRowConsumer bloomFilterAddServiceDataRowConsumer = new BloomFilterAddServiceDataRowConsumer(model, file);
        // Read all rows of data

        int finalProcessCount = model.getProcessCount();
        CommonThreadPool.run(() -> {
            try {
                dataSetReader.readAllWithSelectRow(bloomFilterAddServiceDataRowConsumer, idFeatureFields, finalProcessCount);
            } catch (StatusCodeWithException e) {

            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }

        });
        return rowCount;
    }


    /**
     * Read data from the specified database according to SQL and save to mysql
     *
     * @param model
     * @throws StatusCodeWithException
     */
    public int readAndSaveFromDB(BloomFilterMySqlModel model, List<String> headers) throws Exception {
//        BloomFilterMySqlModel bloomFilterMySqlModel = bloomFilterRepository.getOne(model.getId());
//        int processCount = bloomFilterMySqlModel.getProcessCount();
        DataSourceMySqlModel dsModel = dataSetService.getDataSourceById(model.getDataSourceId());
        if (dsModel == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "dataSourceId在数据库不存在");
        }
        Connection conn = JdbcManager.getConnection(dsModel.getDatabaseType(), dsModel.getHost(), dsModel.getPort()
                , dsModel.getUserName(), dsModel.getPassword(), dsModel.getDatabaseName());
        String sql_script = model.getStatement();
        int rowCount = (int) JdbcManager.count(conn, sql_script);
        BloomFilterRepository bloomFilterRepository = Launcher.CONTEXT.getBean(BloomFilterRepository.class);
        bloomFilterRepository.updateById(model.getId(), "process", Progress.Ready, BloomFilterMySqlModel.class);
        bloomFilterRepository.updateById(model.getId(), "rowCount", rowCount, BloomFilterMySqlModel.class);
        model.setRowCount(rowCount);
        File dir = Paths.get(config.getBloomFilterDir()).toFile();
        if(!dir.exists()){
            boolean result = dir.mkdirs();
            LOG.info("mkdir " + dir.toString() + (result ? "success":"fail"));
        }
        File src = Paths.get(config.getBloomFilterDir()).resolve(model.getName()).toFile();
        model.setSrc(src.toString());
        BloomFilterAddServiceDataRowConsumer bloomFilterAddServiceDataRowConsumer = new BloomFilterAddServiceDataRowConsumer(model, null);
        CommonThreadPool.run(() -> {
            JdbcManager.readWithSelectRow(conn, sql_script, bloomFilterAddServiceDataRowConsumer, headers);
        });
//        bloomFilterAddServiceDataRowConsumer.waitForFinishAndClose();
        return rowCount;
    }


    public boolean CheckFilter(String id, BigInteger N, BigInteger e, BigInteger d, List<Object> CheckData, BloomFilters<BigInteger> bf) {
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
