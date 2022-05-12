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

package com.welab.wefe.board.service.service.data_resource.add;


import com.welab.wefe.board.service.dto.vo.data_set.table_data_set.LabelDistribution;
import com.welab.wefe.board.service.service.DataSetStorageService;
import com.welab.wefe.board.service.service.data_resource.DataResourceUploadTaskService;
import com.welab.wefe.board.service.util.AbstractTableDataSetReader;
import com.welab.wefe.board.service.util.unique.AbstractDataSetUniqueFilter;
import com.welab.wefe.board.service.util.unique.ContainResult;
import com.welab.wefe.board.service.util.unique.DataSetBloomUniqueFilter;
import com.welab.wefe.board.service.util.unique.DataSetMemoryUniqueFilter;
import com.welab.wefe.common.BatchConsumer;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.ListUtil;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.web.Launcher;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

/**
 * @author zane.luo
 */
public class TableDataSetAddServiceDataRowConsumer implements Consumer<LinkedHashMap<String, Object>> {
    private final Logger LOG = LoggerFactory.getLogger(TableDataSetAddServiceDataRowConsumer.class);
    private final static String Y_COLUMN_NAME = "y";

    //region construction parameters

    /**
     * data set id
     */
    private final String dataSetId;
    /**
     * Do you need to de-duplicate
     */
    private final boolean deduplication;

    //endregion

    /**
     * To increase the writing speed, batch processing is used.
     */
    private final BatchConsumer<List<Object>> batchConsumer;
    private int maxBatchSize = 0;
    /**
     * deduplication filter
     */
    private AbstractDataSetUniqueFilter uniqueFilter;
    private final DataSetStorageService dataSetStorageService;
    private final DataResourceUploadTaskService dataResourceUploadTaskService;
    private final AbstractTableDataSetReader dataSetReader;

    /**
     * first column name in headers
     */
    private final String firstColumnName;
    /**
     * is headers contains y column
     */
    private final boolean containsY;
    /**
     * index of y in headers
     */
    private final int yIndex;

    /**
     * Number of positive cases
     */
    private final AtomicLong yPositiveExampleCount = new AtomicLong(0);

    /**
     * The number of duplicate data in the primary key
     */
    private final LongAdder repeatDataCount = new LongAdder();
    /**
     * 标签列表
     */
    private final Set<String> labelSet = new ConcurrentHashSet<>();
    /**
     * 标签分布情况
     */
    private final Map<String, Integer> labelDistribution = new ConcurrentHashMap<>();

    public TableDataSetAddServiceDataRowConsumer(String dataSetId, boolean deduplication, AbstractTableDataSetReader dataSetReader) throws StatusCodeWithException {
        this.dataSetId = dataSetId;
        this.deduplication = deduplication;
        this.dataSetReader = dataSetReader;

        if (deduplication) {
            uniqueFilter = createUniqueFilter(dataSetReader.getTotalDataRowCount());
        }

        List<String> headers = dataSetReader.getHeader();
        this.firstColumnName = headers.get(0);
        this.containsY = headers.contains(Y_COLUMN_NAME);
        this.yIndex = headers.indexOf(Y_COLUMN_NAME);

        this.dataSetStorageService = Launcher.getBean(DataSetStorageService.class);
        this.dataResourceUploadTaskService = Launcher.getBean(DataResourceUploadTaskService.class);

        batchConsumer = new BatchConsumer<>(10, 1_000, rows -> {

            try {
                // save data row to storage
                dataSetStorageService.saveDataRows(dataSetId, rows);

                // statistic positive rate
                statisticPositiveExampleCount(this.containsY, this.yIndex, rows);

                // update data set upload progress
                dataResourceUploadTaskService.updateProgress(
                        dataSetId,
                        dataSetReader.getTotalDataRowCount(),
                        dataSetReader.getReadDataRows(),
                        getRepeatDataCount()
                );
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                dataResourceUploadTaskService.onError(dataSetId, e);
            }

        });

    }


    @Override
    public void accept(LinkedHashMap<String, Object> row) {

        // In order to enable the upload progress bar to start as soon as possible,
        // the initial batch size is set to be smaller.
        if (dataSetReader.getReadDataRows() < 100) {
            batchConsumer.setMaxBatchSize(50);
        } else if (dataSetReader.getReadDataRows() < 1000) {
            batchConsumer.setMaxBatchSize(100);
        }
        // Later processing according to reasonable batch size
        else {
            // Update the batch size of batch write according to the number of columns
            if (this.maxBatchSize < 1) {
                this.maxBatchSize = dataSetStorageService.getAddBatchSize(row.size());
                batchConsumer.setMaxBatchSize(this.maxBatchSize);
            }
        }


        // Salt and hash the primary key
        String id = String.valueOf(row.get(firstColumnName));
        id = Md5.of("hello" + id + "world");
        row.put(firstColumnName, id);


        List<Object> values = new ArrayList<>(row.values());

        // Move column y to the second column (the first column is the primary key)
        if (containsY) {
            moveY(values, values.get(yIndex));

            /**
             * 统计 Y 的分布情况
             *
             * 在回归场景中 y 值是连续型，这种情况统计分布没有意义。
             * 所以在种类过多时停止统计
             */
            if (labelSet.size() < 100_000) {
                labelSet.add(row.get(Y_COLUMN_NAME).toString());

                if (labelSet.size() < 1_000) {
                    labelDistribution.compute(row.get(Y_COLUMN_NAME).toString(), (k, v) -> v == null ? 1 : v + 1);
                }
            } else {
                labelDistribution.clear();
            }
        }

        // Save the data row
        if (deduplication) {
            saveRowWithDeduplication(values);
        } else {
            batchConsumer.add(values);
        }
    }

    /**
     * Wait for the consumption queue to finish
     */
    public void waitForFinishAndClose() {
        batchConsumer.waitForFinishAndClose();
    }

    /**
     * The count of data duplicated by the primary key
     */
    public long getRepeatDataCount() {
        return repeatDataCount.longValue();
    }

    /**
     * Move column y to the second column (the first column is the primary key)
     */
    private void moveY(List<Object> values, Object y) {
        // 由于深度学习的回归场景允许 y 为连续型数据，所以这里不再限制 y 为 int。
        // if (!Validator.isInteger(y)) {
        //     throw new RuntimeException(
        //             "y 列必须为整数，数据集第 "
        //                     + dataSetReader.getReadDataRows()
        //                     + " 行附近发现非整数："
        //                     + (StringUtil.isEmpty(String.valueOf(y)) ? "空" : y)
        //                     + "，请修正数据集后重试。"
        //     );
        // }

        ListUtil.moveElement(values, yIndex, 1);
    }

    /**
     * Save data to storage and ensure that the data is not duplicated
     */
    private void saveRowWithDeduplication(List<Object> row) {
        String id = String.valueOf(row.get(0));

        ContainResult containResult = uniqueFilter.contains(id);
        while (true) {
            switch (containResult) {
                // Already exists: discard duplicate data
                case In:
                    repeatDataCount.increment();
                    return;

                // Does not exist, write
                case NotIn:
                    batchConsumer.add(row);
                    return;

                // Not sure: Wait for the data written in the queue to be written to confirm the query
                case MaybeIn:
                    // Waiting for all data in the queue to be written to storage
                    batchConsumer.waitForClean();

                    // Query in the storage to confirm whether it exists
                    containResult = dataSetStorageService.containsKey(dataSetId, id)
                            ? ContainResult.In
                            : ContainResult.NotIn;
                    continue;

                default:
                    return;
            }
        }
    }

    /**
     * Create a deduplication filter
     */
    private AbstractDataSetUniqueFilter createUniqueFilter(long totalDataRowCount) {

        // Use memory filters when the amount of data is small
        if (totalDataRowCount > 100_000) {
            return new DataSetBloomUniqueFilter(totalDataRowCount);
        } else {
            return new DataSetMemoryUniqueFilter();
        }
    }

    /**
     * Count the number of positive cases
     */
    private void statisticPositiveExampleCount(boolean containsY, int yIndex, List<List<Object>> rows) {
        if (!containsY || yIndex < 0 || CollectionUtils.isEmpty(rows)) {
            return;
        }

        // When it comes in, the y of row has moved to the position with index 1
        yIndex = 1;
        for (List<Object> row : rows) {
            Object value = row.get(yIndex);
            if (null == value) {
                continue;
            }
            String yValue = String.valueOf(value);

            if ("0".equals(yValue)) {
                continue;
            }

            yPositiveExampleCount.incrementAndGet();
        }
    }

    /**
     * Calculate the proportion of positive examples
     */
    public double getPositiveExampleRatio() {
        long totalCount = this.dataSetReader.getReadDataRows() - this.getRepeatDataCount();
        if (totalCount <= 0) {
            return 0;
        }
        return new BigDecimal(this.yPositiveExampleCount.get())
                .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Get the number of positive cases
     */
    public long getPositiveExampleCount() {
        return yPositiveExampleCount.get();
    }

    public LabelDistribution getLabelDistribution() {
        return new LabelDistribution(labelSet.size(), labelDistribution);
    }
}
