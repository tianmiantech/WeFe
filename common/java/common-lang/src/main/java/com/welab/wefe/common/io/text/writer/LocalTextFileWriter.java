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

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

/**
 * Local text file write
 *
 * @author Zane
 */
public class LocalTextFileWriter<S> extends AbstractTextWriter<S> {
    @Override
    protected void write(S record) throws Exception {

        synchronized (WRITER_LOCKER) {
            prepareWriter(record);
        }

        String line = recordToStringFunction.get(record, super.dataTotalCounter.longValue());
        this.outputBufferedWriter.write(line + super.recordSeparator);
        this.totalLength.add(line.length() + super.recordSeparator.length());
        this.outputBufferedWriter.flush();
        if (totalLength.longValue() >= super.maxFileLength) {
            this.reset();
        }
    }

    /**
     * Set file writer
     */
    private void prepareWriter(S record) throws IOException {
        if (StringUtils.isEmpty(this.currentFilePath)) {
            this.currentFilePath = fileNameFunction.get(record, super.dataTotalCounter.longValue());
        }
        File file = new File(this.currentFilePath);
        if (this.fileChannel != null && this.outputBufferedWriter != null) {
            return;
        } else {
            this.currentFilePath = file.getAbsolutePath();
        }

        // Release current file object
        closeFile();

        // Reset file object
        if (!file.exists()) {
            if (file.getParent() != null) {
                (new File(file.getParent())).mkdirs();
            }

            createNewFile(file);
        } else {
            this.currentFilePath += "." + System.currentTimeMillis();
            file = new File(this.currentFilePath);
            createNewFile(file);
        }

        if (!file.canWrite()) {
            throw new IOException("File is not writable: [" + file.getAbsolutePath() + "]");
        }

        FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath(), true);
        this.fileChannel = fileOutputStream.getChannel();
        this.outputBufferedWriter = new BufferedWriter(Channels.newWriter(fileChannel, this.encoding));
    }

    public static boolean createNewFile(File file) throws IOException {
        if (file.exists()) {
            return false;
        } else {
            try {
                return file.createNewFile() && file.exists();
            } catch (IOException var2) {
                if (file.exists()) {
                    return true;
                } else {
                    throw var2;
                }
            }
        }
    }

    /**
     * Release file related resources
     */
    private void closeFile() {
        try {
            if (this.outputBufferedWriter != null) {
                this.outputBufferedWriter.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (this.fileChannel != null) {
                    this.fileChannel.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (this.fileChannel != null) {
                    this.fileChannel = null;
                }
                try {
                    if (this.outputBufferedWriter != null) {
                        this.outputBufferedWriter.close();
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if (this.outputBufferedWriter != null) {
                        this.outputBufferedWriter = null;
                    }
                }

            }

        }
    }

    @Override
    public void reset() {
        closeFile();
        super.reset();
    }

    @Override
    public void close() {
        closeFile();
    }


    //region

    private FileChannel fileChannel;
    private Writer outputBufferedWriter;
    private final Object WRITER_LOCKER = new Object();

    //endregion
}
