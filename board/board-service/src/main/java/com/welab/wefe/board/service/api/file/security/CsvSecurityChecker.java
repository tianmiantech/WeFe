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
package com.welab.wefe.board.service.api.file.security;

import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author zane
 * @date 2021/12/31
 */
public class CsvSecurityChecker extends FileSecurityChecker {
    @Override
    protected void doCheck(File file) throws IOException {
        CsvReader reader = new CsvReader();
        reader.setContainsHeader(false);
        reader.setSkipEmptyRows(true);
        CsvParser parser = reader.parse(file, StandardCharsets.UTF_8);

        while (true) {
            CsvRow row = parser.nextRow();

            if (row == null) {
                break;
            }

            for (String str : row.getFields()) {
                for (String key : keywords) {
                    if (str.contains(key)) {
                        throw new IOException("文件包含不安全的字符：" + key);
                    }
                }
            }
        }
    }
}
