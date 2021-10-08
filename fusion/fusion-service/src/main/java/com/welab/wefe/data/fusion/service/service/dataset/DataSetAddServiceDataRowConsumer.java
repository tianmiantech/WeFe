/**
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

package com.welab.wefe.data.fusion.service.service.dataset;

import com.welab.wefe.common.BatchConsumer;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.data.fusion.service.database.entity.DataSetMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.DataSetRepository;
import com.welab.wefe.data.fusion.service.enums.Progress;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

/**
 * @author zane.luo
 */
public class DataSetAddServiceDataRowConsumer implements Consumer<Map<String, Object>> {
    /**
     * Data set Id
     */
    private String dataSetId;
    /**
     * Data set file
     */
    private File file;


    private List<String> rows;

    /**
     * To speed up writing, use batch processing.
     */
    private BatchConsumer<Map<String, Object>> batchConsumer;

    /**
     * The amount of duplicate data
     */
    private LongAdder repeatDataCount = new LongAdder();

    /**
     * If the data comes from reading data from a table in the database, this field represents the total number of rows derived from the query statement
     */
    private long rowCountFromDB;

    public DataSetAddServiceDataRowConsumer(DataSetMySqlModel model, boolean deduplication, File file, List<String> headers) {
        this.dataSetId = model.getId();
        this.file = file;
        this.rows = headers;
        batchConsumer = new BatchConsumer<>(1024, 1_000, rows -> {
            DataSetRepository dataSetRepository = Launcher.CONTEXT.getBean(DataSetRepository.class);
            dataSetRepository.updateById(model.getId(), "process", Progress.Running, DataSetMySqlModel.class);
            saveDataRows(model, rows);
        });
    }

    public DataSetAddServiceDataRowConsumer(DataSetMySqlModel model, boolean deduplication, long rowCountFromDB, List<String> headers) {
        this.dataSetId = model.getId();
        this.rowCountFromDB = rowCountFromDB;
        this.rows = headers;

        batchConsumer = new BatchConsumer<>(1024, 1_000, rows -> {
            DataSetRepository dataSetRepository = Launcher.CONTEXT.getBean(DataSetRepository.class);
            dataSetRepository.updateById(model.getId(), "process", Progress.Running, DataSetMySqlModel.class);
            saveDataRows(model, rows);
        });
    }


    /**
     * Bulk storage
     */
    public void saveDataRows(DataSetMySqlModel model, List<Map<String, Object>> rows) {

        CommonThreadPool.run(() -> DataSetStorageHelper.saveDataSetRows(model, rows));
    }

    @Override
    public void accept(Map<String, Object> data) {

        // Save data row
        batchConsumer.setMaxBatchSize(1000);
        batchConsumer.add(data);

    }

    /**
     * Wait for the consumption queue to complete
     */
    public void waitForFinishAndClose() {
        batchConsumer.waitForFinishAndClose();
    }
}
