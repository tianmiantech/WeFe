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

package com.welab.wefe.common.io.text.writer;

import com.alibaba.fastjson.JSON;
import com.welab.wefe.common.io.text.writer.delegate.FileNameFunction;
import com.welab.wefe.common.io.text.writer.delegate.RecordToStringFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Zane
 */
public abstract class AbstractTextWriter<S> implements Closeable {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * Written string length
     */
    protected LongAdder totalLength = new LongAdder();
    /**
     * Separator of data row
     */
    protected String recordSeparator = System.lineSeparator();
    /**
     * Text encoding
     */
    protected String encoding = "utf-8";
    protected long maxFileLength = Integer.MAX_VALUE;
    /**
     * File path currently written
     */
    protected String currentFilePath;
    /**
     * Delegate to get file name
     */
    protected FileNameFunction<S> fileNameFunction;
    /**
     * Delegate to convert record to text
     */
    protected RecordToStringFunction<S> recordToStringFunction = (record, sequence) -> JSON.toJSONString(record);
    /**
     * A counter that records the amount of data received.
     */
    protected LongAdder dataTotalCounter = new LongAdder();
    /**
     * Counter that records the amount of data that failed to write.
     */
    protected LongAdder dataFailCounter = new LongAdder();
    /**
     * Last activity time
     */
    private long lastActionTime = System.currentTimeMillis();

    public AbstractTextWriter() {
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    try {
                        this.close();
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                })
        );
    }


    /**
     * Abstract method: save data
     * @param record
     * @throws Exception
     */
    protected abstract void write(final S record) throws Exception;

    /**
     * Receive a piece of data
     */
    public synchronized void receive(final S record) {
        dataTotalCounter.increment();
        this.lastActionTime = System.currentTimeMillis();
        try {
            write(record);
        } catch (Exception e) {
            dataFailCounter.increment();
            logger.error("Data write failure, the current number of writer write failures：" + dataFailCounter.longValue(), e);
        }
    }

    /**
     * Reset writer
     */
    public void reset() {
        dataTotalCounter.reset();
        dataFailCounter.reset();
        totalLength.reset();
        this.currentFilePath = null;
    }

    public long getTotalCount() {
        return dataTotalCounter.longValue();
    }

    public long getFailCount() {
        return dataFailCounter.longValue();
    }

    public long getLastActionTime() {
        return lastActionTime;
    }

    public void setRecordSeparator(String recordSeparator) {
        this.recordSeparator = recordSeparator;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setFileNameFunction(FileNameFunction<S> fileNameFunction) {
        this.fileNameFunction = fileNameFunction;
    }

    public void setRecordToStringFunction(RecordToStringFunction<S> recordToStringFunction) {
        this.recordToStringFunction = recordToStringFunction;
    }

    public long getTotalLength() {
        return totalLength.longValue();
    }

    public String getRecordSeparator() {
        return recordSeparator;
    }

    public String getCurrentFilePath() {
        return currentFilePath;
    }

    public void setMaxFileLength(long maxFileLength) {
        this.maxFileLength = maxFileLength;
    }
}
