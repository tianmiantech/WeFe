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

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author Zane
 */
public class LocalTextFileReader extends AbstractTextReader {

    private File file;
    InputStreamReader inputStreamReader;
    BufferedReader bufferedReader;

    /**
     * The default is UTF_ 8 parsing text
     */
    public LocalTextFileReader(File file) throws FileNotFoundException {
        this(file, StandardCharsets.UTF_8);
    }

    public LocalTextFileReader(File file, Charset charset) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getPath());
        }
        this.file = file;

        inputStreamReader = new InputStreamReader(new FileInputStream(file), charset);
        bufferedReader = new BufferedReader(inputStreamReader);
    }

    /**
     * Recursively calculate the size of the file (in bytes)
     */
    public double getFileTotalByteSize() {
        return getFileTotalByteSize(file);
    }

    private static double getFileTotalByteSize(File f) {
        if (f.isFile()) {
            return f.length();
        }
        File[] children = f.listFiles();
        double total = 0;
        if (children != null) {
            for (File child : children) {
                total += getFileTotalByteSize(child);
            }
        }
        return total;
    }

    /**
     * Overload (in MB)
     */
    public double getFileTotalMbSize() {
        return getFileTotalByteSize() / 1024 / 1024;
    }


    /**
     * Overload (unit: GB)
     */
    public double getFileTotalGbSize() {
        return getFileTotalMbSize() / 1024;
    }


    @Override
    public String getFileName() {
        return file.getName();
    }

    @Override
    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    @Override
    public void close() throws IOException {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        } finally {
            bufferedReader = null;
        }

        try {
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
        } finally {
            inputStreamReader = null;
        }
    }
}
