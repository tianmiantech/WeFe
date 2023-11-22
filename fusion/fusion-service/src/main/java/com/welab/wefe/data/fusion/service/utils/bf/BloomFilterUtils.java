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

package com.welab.wefe.data.fusion.service.utils.bf;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author hunter.zhao
 */
public class BloomFilterUtils {

    /**
     * Persist bloomFilter to the specified file
     */
    public static void writeTo(String fileName, BloomFilters bf) throws IOException {
        bf.writeTo(new FileOutputStream(fileName, false));
    }

    /**
     * Extract bloomFilter from the file
     */
    public static BloomFilters readFrom(String src) {
        try {
            return BloomFilters.readFrom(new FileInputStream(src));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
