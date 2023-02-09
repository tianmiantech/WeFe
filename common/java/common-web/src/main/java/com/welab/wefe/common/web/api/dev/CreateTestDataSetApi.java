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

package com.welab.wefe.common.web.api.dev;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.OS;
import com.welab.wefe.common.util.RandomUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.config.CommonConfig;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zane
 */
@Api(path = "test/create_data_set", name = "generate data set for testing")
public class CreateTestDataSetApi extends AbstractApi<CreateTestDataSetApi.Input, CreateTestDataSetApi.Output> {

    private static final Random random = new Random();
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
    private static final String[] PHONE_NUMBER_PREFIX = {"139", "138", "137", "136", "135", "134", "159", "158", "157", "150", "151", "152", "188", "187", "182", "183", "184", "178", "130", "131", "132", "156", "155", "186", "185", "176", "133", "153", "189", "180", "181", "177"};

    @Autowired
    private CommonConfig commonConfig;

    static {
        NUMBER_FORMAT.setMaximumFractionDigits(3);
    }

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {

        try {
            String file = createCsv(input);
            return success(new Output(file));

        } catch (IOException e) {
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, e.getMessage());
        }


    }

    public Map<String, Feature> buildFeaturesConfig(Input input) {
        LinkedHashMap<String, Feature> result = new LinkedHashMap<>(input.features);

        Feature idFeature = Feature.ofId();
        result.put(idFeature.name, idFeature);

        if (input.hasY) {
            Feature yFeature = Feature.ofY();
            result.put(yFeature.name, yFeature);
        }

        // Count the number of columns
        int columns = input.hasY ? input.features + 2 : input.features + 1;

        for (String name : input.featureNameList) {
            result.put(name, new Feature(name));
        }

        for (Feature feature : input.manualFeatureList) {
            if (StringUtil.isEmpty(feature.name)) {
                throw new RuntimeException("manual_feature_list 中手动指定的特征必须声明其 name");
            }
            result.put(feature.name, feature);
        }

        while (result.size() < columns) {
            String name = input.featureNameLength > 0
                    ? RandomUtil.random(input.featureNameLength, "abcdefghijklmnopqrstuvwxyz0123456789_")
                    : "x" + (result.size() - 1);

            result.put(name, new Feature(name));
        }

        return result;
    }

    private String createCsv(Input input) throws IOException {
        String fileName = StringUtil.isNotEmpty(input.fileName)
                // 指定文件名
                ? input.fileName
                // 生成文件名
                : input.features + "x"
                + input.rows + "rows-"
                + "miss" + input.featureMissRate + "-"
                + input.idType + "-"
                + (input.hasY ? "-y" : "") + ".csv";

        // 将生成的文件存放在日志目录
        String baseDir = "";
        switch (OS.get()) {
            case windows:
                baseDir = "D:\\";
                break;
            default:
                baseDir = "~";
        }
        if (!StringUtil.isEmpty(commonConfig.getLoggingFilePath())) {
            baseDir = new File(commonConfig.getLoggingFilePath()).getParentFile().getAbsolutePath();
        }
        Path dir = Paths.get(baseDir).resolve("create_data_set");

        File file = dir.resolve(fileName).toFile();
        if (file.exists()) {
            FileUtils.deleteQuietly(file);
        }

        FileOutputStream fileOutputStream = FileUtils.openOutputStream(file, true);


        // Generate column headers
        Map<String, Feature> featuresConfig = buildFeaturesConfig(input);
        List<Feature> features = new ArrayList<>(featuresConfig.values());

        String title = StringUtil.join(features.stream().map(x -> x.name).collect(Collectors.toList()), ",") + System.lineSeparator();
        fileOutputStream.write(title.getBytes());

        // Generate data rows
        for (long rowIndex = 1; rowIndex < input.rows; rowIndex++) {
            String[] row = new String[features.size()];

            for (int columnIndex = 0; columnIndex < features.size(); columnIndex++) {
                row[columnIndex] = createValue(input, features.get(columnIndex), rowIndex);
            }

            String line = StringUtil.join(row, ",") + System.lineSeparator();
            fileOutputStream.write(line.getBytes());

            if (rowIndex % 10000 == 0) {
                System.out.println(rowIndex);
            }
        }

        fileOutputStream.close();

        return file.getAbsolutePath();
    }

    private String createValue(Input input, Feature feature, long rowIndex) {

        if (feature.primaryKey) {
            return createId(rowIndex, input.idType);
        }

        if (feature.y) {
            return random.nextBoolean() ? "1" : "0";
        }

        // 根据指定概率产生空值
        int missRate = 0;
        if (feature.missRate >= 0) {
            missRate = feature.missRate;
        } else if (input.featureMissRate > 0) {
            missRate = input.featureMissRate;
        }

        if (missRate > 0) {
            int limit = random.nextInt(100);
            if (missRate > limit) {
                return "";
            }
        }

        switch (feature.dataType) {
            case "uint":
                return String.valueOf(random.nextInt(101));
            case "int":
                return (random.nextBoolean() ? "" : "-") + random.nextInt(101);
            case "sex_cn":
                return (random.nextBoolean() ? "男" : "女");
            case "datetime":
                return DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(
                        new Date(System.currentTimeMillis() - random.nextInt(Integer.MAX_VALUE))
                );
            case "date":
                return DateUtil.toStringYYYY_MM_DD(
                        new Date(System.currentTimeMillis() - random.nextInt(Integer.MAX_VALUE))
                );
            case "city_cn":
            case "double":
            default:
                return NUMBER_FORMAT.format(random.nextDouble());
        }
    }

    private String createId(long index, String idType) {
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
        @Check(name = "文件名")
        public String fileName;
        @Check(name = "特征数")
        public int features;
        @Check(name = "数据行数")
        public long rows;
        @Check(name = "是否包含 y 列")
        public boolean hasY;
        @Check(name = "id 类型", regex = "^(auto_increment)|(cnid)|(phone_number)$")
        public String idType = "auto_increment";

        @Check(name = "特征名称列表", desc = "生成工具将会优先使用该 list 中的特征名，仅在不够用时才自动生成特征名。")
        public List<String> featureNameList = new ArrayList<>();
        @Check(name = "特征名称长度", desc = "生成工具在自动生成特征名时，将会生产指定长度的名称。")
        public int featureNameLength;
        @Check(name = "缺失率", desc = "取值范围 0 - 100，默认 0（无缺失）。生成工具将会使用该缺失率配置项生成数据。")
        public int featureMissRate;
        @Check(name = "自定义特征列表", desc = "生成工具在生成特征数据时，将会根据指定配置进行生成。")
        public List<Feature> manualFeatureList = new ArrayList<>();
    }

    public static class Feature {
        public String name;
        public int missRate = -1;
        public String dataType = "double";
        public boolean primaryKey;
        public boolean y;

        public Feature() {
        }

        public Feature(String name) {
            this.name = name;
        }

        public static Feature ofId() {
            Feature feature = new Feature();
            feature.name = "id";
            feature.primaryKey = true;
            return feature;
        }

        public static Feature ofY() {
            Feature feature = new Feature();
            feature.name = "y";
            feature.y = true;
            return feature;
        }

        // region getter/setter

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getMissRate() {
            return missRate;
        }

        public void setMissRate(int missRate) {
            this.missRate = missRate;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public boolean isPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
        }

        public boolean isY() {
            return y;
        }

        public void setY(boolean y) {
            this.y = y;
        }


        // endregion
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
