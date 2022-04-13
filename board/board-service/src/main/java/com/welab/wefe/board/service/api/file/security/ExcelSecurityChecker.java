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

import com.welab.wefe.common.io.excel.ExcelReader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author zane
 * @date 2021/12/31
 */
public class ExcelSecurityChecker extends FileSecurityChecker {

    @Override
    protected void doCheck(File file) throws IOException {
        ExcelReader reader = new ExcelReader(file);

        if (reader.getSheetCount() == 0) {
            return;
        }

        long totalRowCount = reader.getRowCount(0);
        for (int i = 0; i < totalRowCount; i++) {
            List<Object> row = reader.getRowData(0, i);
            if (row == null) {
                continue;
            }
            for (Object item : row) {
                String str = String.valueOf(item);
                for (String key : keywords) {
                    if (str.contains(key)) {
                        throw new IOException("文件包含不安全的字符：" + key);
                    }
                }
            }
        }

    }
}
