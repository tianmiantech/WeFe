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

package com.welab.wefe.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Data compression and decompression tool Util
 *
 * @author johnny.lin
 * @version V1.0
 */
public class GzipUtil {
    private static final Logger LOG = LoggerFactory.getLogger(GzipUtil.class);

    /**
     * unzip
     */
    public static byte[] unzip(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try (ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
             PrintStream out = new PrintStream(byteArrayOut);
             ByteArrayInputStream byteArrayIn = new ByteArrayInputStream(bytes);
             GZIPInputStream in = new GZIPInputStream(byteArrayIn)) {

            byte[] b = new byte[1024];
            int readLen;

            while ((readLen = in.read(b)) != -1) {
                out.write(b, 0, readLen);
            }
            out.flush();

            return byteArrayOut.toByteArray();

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return null;

    }

    /**
     * zip
     */
    public static byte[] zip(byte[] content) {
        if (content == null || content.length == 0) {
            return null;
        }
        byte[] result = null;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(out)) {

            gzip.write(content);
            gzip.finish();

            result = out.toByteArray();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return result;
    }
}
