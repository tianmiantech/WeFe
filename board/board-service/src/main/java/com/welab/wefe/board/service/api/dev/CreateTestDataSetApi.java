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

package com.welab.wefe.board.service.api.dev;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Random;

/**
 * @author Zane
 */
@Api(path = "test/create_data_set", name = "generate data set for testing", login = false)
public class CreateTestDataSetApi extends AbstractApi<CreateTestDataSetApi.Input, CreateTestDataSetApi.Output> {

    private final Random random = new Random();
    private static final String[] PHONE_NUMBER_PREFIX = {"139", "138", "137", "136", "135", "134", "159", "158", "157", "150", "151", "152", "188", "187", "182", "183", "184", "178", "130", "131", "132", "156", "155", "186", "185", "176", "133", "153", "189", "180", "181", "177"};

    @Autowired
    private Config config;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {

        try {
            String file = createCsv(input);
            return success(new Output(file));

        } catch (IOException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }


    }

    private String createCsv(Input input) throws IOException {
        String fileName = config.getFileUploadDir() + "/"
                + input.idType + "-"
                + input.features + "-"
                + input.rows
                + (input.hasY ? "-y" : "")
                + ".csv";

        File file = new File(fileName);

        if (file.exists()) {
            FileUtils.deleteQuietly(file);
        }

        FileOutputStream fileOutputStream = FileUtils.openOutputStream(file, true);

        // Count the number of columns
        int columns = input.hasY ? input.features + 2 : input.features + 1;

        // Generate column headers
        String[] header = new String[columns];
        header[0] = "id";
        if (input.hasY) {
            header[1] = "y";
        }

        if (input.features > 0) {
            for (int i = input.hasY ? 2 : 1; i < columns; i++) {
                header[i] = "x" + (input.hasY ? i - 1 : i);
            }
        }

        String title = StringUtil.join(header, ",") + System.lineSeparator();
        fileOutputStream.write(title.getBytes());

        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(3);

        // Generate data rows
        for (int i = 0; i < input.rows; i++) {
            String[] row = new String[columns];

            row[0] = createId(i, input.idType);
            for (int c = 1; c < columns; c++) {

                if (input.hasY && c == 1) {
                    row[c] = random.nextBoolean() ? "1" : "0";
                } else if (input.features > 0) {
                    row[c] = format.format(random.nextDouble());
                }

            }

            String line = StringUtil.join(row, ",") + System.lineSeparator();
            fileOutputStream.write(line.getBytes());

            if (i % 10000 == 0) {
                System.out.println(i);
            }
        }

        fileOutputStream.close();

        return file.getAbsolutePath();
    }

    private String createId(int index, String idType) {
        switch (idType) {
            case "auto_increment":
                return index + "";
            case "cnid":
                return createCnid();
            case "phone_number":
                return createPhoneNumber();
            default:
                return "";
        }
    }

    private String createCnid() {
        StringBuilder str = new StringBuilder(18);
        for (int i = 0; i < 18; i++) {
            str.append(random.nextInt(9));
        }
        return str.toString();
    }

    private String createPhoneNumber() {
        int index = random.nextInt(PHONE_NUMBER_PREFIX.length);
        StringBuilder str = new StringBuilder(11);
        str.append(PHONE_NUMBER_PREFIX[index]);
        for (int i = 0; i < 8; i++) {
            str.append(random.nextInt(9));
        }
        return str.toString();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "特征数")
        private int features;
        @Check(name = "数据行数")
        private int rows;
        @Check(name = "是否包含 y 列")
        private boolean hasY;
        @Check(name = "id 类型", regex = "^(auto_increment)|(cnid)|(phone_number)$")
        private String idType = "auto_increment";

        //region getter/setter

        public int getFeatures() {
            return features;
        }

        public void setFeatures(int features) {
            this.features = features;
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public boolean isHasY() {
            return hasY;
        }

        public void setHasY(boolean hasY) {
            this.hasY = hasY;
        }

        public String getIdType() {
            return idType;
        }

        public void setIdType(String idType) {
            this.idType = idType;
        }


        //endregion
    }

    public static class Output {
        private String file;

        public Output(String file) {
            this.file = file;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }
    }
}
