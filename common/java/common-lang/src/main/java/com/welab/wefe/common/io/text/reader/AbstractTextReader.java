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

package com.welab.wefe.common.io.text.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;

/**
 * @author Zane
 */
public abstract class AbstractTextReader implements Closeable {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * Get file name
     * @return  file name
     */
    public abstract String getFileName();

    /**
     *
     * Get BufferedReader object
     * <p>
     * Read all lines of the file：
     * while ((line = bufferedReader.readLine()) != null) { }
     * @return
     */
    public abstract BufferedReader getBufferedReader();

    /**
     * Gets the file name without a suffix
     */
    public String getFileNameWithoutSuffix() {
        String fileName = getFileName();
        int slash = fileName.lastIndexOf('.');
        if (slash < 1) {
            return fileName;
        }
        return fileName.substring(0, slash + 1);
    }
}
