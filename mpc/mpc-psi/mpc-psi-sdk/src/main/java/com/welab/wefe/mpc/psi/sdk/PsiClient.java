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

package com.welab.wefe.mpc.psi.sdk;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.psi.sdk.excel.AbstractDataSetReader;
import com.welab.wefe.mpc.psi.sdk.excel.CsvDataSetReader;
import com.welab.wefe.mpc.psi.sdk.excel.ExcelDataSetReader;
import com.welab.wefe.mpc.psi.sdk.model.ConfuseData;

//两方交集查询 psi
//配合 mpc-psi-sdk-1.0.0.jar使用
public class PsiClient {
    // 私钥
    private static final String 测试客户1_privateKey1 = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCS2skBqbjKy4bo6UaevFfjHCBHxBwhEZZPFt0SvOGVAcZKTMQJ96ujhK/4l+XigvjlF8l0d0V1GV9tVip+2jtxmLJXv01l3Aq7Vp2A/bx55qAIgEOdyWpmUbaAYp39Cx4pm0TxtV+q2QC3bWxG6Q+usxOk/NqCwknMcU37ggZ7VQOen2zlN+J/NbnCAv3dgUMacTGO9EZAGfZI/0kztqy+S/csiHnGJe9yaGYoMymy0Flh4S+T2ZUvZdUCLGk5jHd5gpPpmvoz1+VBGrFtYk7saJxiBt+qXPlTpgPzRKRX18zaDdm6ug+PuElruXQONfaNQ9389gf9BjXazIuacTzzAgMBAAECggEABjomyd5Dir3ko524s+RyqZu4trJXKwPO5t2mVDQUMLhZiy8EJRQOgCmROK5gPiGlojlG/pxMIrIHvOApfyWL3b5G2bev9YDGJ7l9hkm8I0MnSBUASQGQdHREk98aqe89wS/nVYSGYP1ArYaDT+J0GslXLOjX7zGD5ZpsXxzdX39j6MIZS9jO6r8eb4981oZ60JSPDkxDkNpM42sHnyUoSXLc1SGqj7t2qjDhodi1yaAIaIlqS7LO+o/iyufxKQVGC5tGnDX+Lt/+H/h7cfJdA9GJmcOkHRXuoVVZKg0dgmh5z9P70aXJj405RcGZbbLaMb/V8GHBBWNj3yw2IvTm+QKBgQDLGV0ugIwZw+QtLAJXnWMkbcKDEDxIfEYAQQOY9usskMjXNSujKgK6g35BY4+Viv/4J2T7VqyIiSawqXooJ/R/N99i/8dpYlEPq+6kOBQdbFfqbXIaocfGPOA/gXkwgMUBEZa22uh5o18TsyzGzrX4Wi07+83cYZU0Lk7m2B5KPwKBgQC5GwtV44KJTWquVMn8YYcazMwxgIgaCO4OAhhVe9My7QPKNB2UnZvwVgnBw8dDPmZY4p6Ufkt6g4ezDwZx/NOoNzLJDMa5jCUA4doG55MCmD3jHpC6R91a88TkdxymdSbbmYDXXsuruW0ftMjQUW/5WpraXJFVapSKa1rLP+EYTQKBgEl+JSTlqUzNgO0DptlTf4O4IBHTNy03oxEGdanNd+5JehO+DstdMV2SpYY948BGaze98aGtjINftdtpiGWth6Ddc/1b9yngRNr56UINdOHsyadv8UFH14WZk8AHNvZhMmifl70VW9hgUNBNHt/V/y0eXI3/IVAlE5utQinwfI17AoGAaWN1a7wxuPyjzYemzJO2eVK6u0BjvPbymnUk556tW2RIcRqE8PVfMFG8El7mF2nymY7FHORfEuzKGO9oCwQYecCboI4uorug4cS0/pNgRuzo52OioPyDkeISasVKQZeXvRXDnltxeF8FOzUR1FFZgE3Otp5XfJtG4RxIQEopXU0CgYEAgurzRIsAzGl859cfDThv5OgE0r3oTiQUzlDy3aSv4pKxBGnBRSFuLR2hCs6drVAltGECE+Xthk/Ik8zKMX3rpU1uGUmOa3wSZBYH7kZKxaobScxg9SzHuEOaHsw0wKWx2OKJtuS7iMF8RugaDGZChLc8NgEB5RHtaRpef519Jt8=";
    // 公钥
    private static final String 测试客户1_publicKey1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAktrJAam4ysuG6OlGnrxX4xwgR8QcIRGWTxbdErzhlQHGSkzECfero4Sv+Jfl4oL45RfJdHdFdRlfbVYqfto7cZiyV79NZdwKu1adgP28eeagCIBDnclqZlG2gGKd/QseKZtE8bVfqtkAt21sRukPrrMTpPzagsJJzHFN+4IGe1UDnp9s5TfifzW5wgL93YFDGnExjvRGQBn2SP9JM7asvkv3LIh5xiXvcmhmKDMpstBZYeEvk9mVL2XVAixpOYx3eYKT6Zr6M9flQRqxbWJO7GicYgbfqlz5U6YD80SkV9fM2g3ZuroPj7hJa7l0DjX2jUPd/PYH/QY12syLmnE88wIDAQAB";
    // 客户code
    private static final String 测试客户1_code1 = "kaylee-partner";

    // 私钥
    private static final String 测试客户1_privateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCW4gsKl4TWdJtq3+TDpOOUrB+w/GEtU8kQ5zjT9MpPC3FQ9PGtzl3PANp2SQFNIhwxRs0UMn2NC5yrBlb/wNhTqYVx6wr74I3O9cyaJQZy4tgipgfiHsi3zEyfQOHbmLwMay+1g16JfuNXCP20mIpXoLFps1fxDm8vDUNJ0m+/Q8WunjZjVQzGFFn+YpeRRvjR+KjO434j4nAtOK7N3OgJeG2XFrO8dYQNbfM1gPW31ZVq361lOJEGZGEVOGtjvwASt0EoHpTE7ZdiHO7uvcYMdnnmITEoEoigBu425ORbVc7gKh/i2XJG0ibIaRVLcRLmq+DjO4JZ0zwYC8HYf+aHAgMBAAECggEAMZmNV6YHnRgoHzIK2eXSioUqxduxdl0cs9nO7J6EAwgX8C0VZbV5URtOtaO6MckMy4Yi1IjkjQFMN0yWPO0AMECCBbjmm3ZuJ6LV57ZUMGAx/PHnlYXZJdKbPZ7uoGnNJ6f0vw64fSqhQXNLM5sRn0zMBZtXRX8hPa52eEYdRhpX0x266L/LMT5DHLiHrsCTxw/alwjIz4/Cqolukz0HgRIj1sUPJvbOSooTk65tk7lLuBWJAbDUS+JBSma/WlHcIrU55X8Zm1Ksh+SZ3wSNDkvP/VzFYCgbJT4XtUuEMUbXA0A+wk2lIqOiH5NwTEcdKgsb0jKkUX/ID1WkE1zMAQKBgQDSoh//tYt4KfKqT5DC2m3nJCux7cK2GnkW9p9figFPZ03Hp6lMYGwIKlks4UqFQghB/+i2iW53FHIBaOPpHnSzkQDavtF28W2xx9ukB1GmvkdFRlqGwlrYgvs0WvzwAogEptFEHfoBgV+OmH+4/PracU23jZMiF0ElZPXS3oVvQQKBgQC3YWlrK7YkI9yeuf5cwWWKIDYVH0IN/2xdLbWf6qXCSvnSyxpwoLIPZ/jxtBsL9G/jIlKvegturwp1Ml21fvpe5PC2hABpqAF3lg8US7sR3jEgjV9lg7EW5/ylwsWGTdX2Nmi1ttiypTZ1i/W4VER8gjKmFkUmUYqnxge6lfarxwKBgAhdrh2u7UIxkFTZYiuLF3BlzGJ9yg0HkiWqV/wodBLeTIWQkDXbYo8Ud4RD0nzmeYN9kZWmcb/DDSAnNV4t+n6jVMBbceBiiHPWN1AVhlW0GwqSV8ggchFFaorSzlcOEq70nT0yX5qts8jrH6+ORLxmYarXsa9Z8xl0IBtnAS/BAoGAEyOflQfi4rK7e/V9jsDAkEH8Ywf4JO3bqX3zztT2p+ibWlzITaj37JO1b8SUbhL8+n/CkX3zY4HxXXn8Pc0a5HyQgvwT7vMR9CE9QakFxT+jcPpQUpAEE5fyznodk37cLe5Pz2deBocnv+zzoGFrJHOSRbNdVDp+djQ86kRsklECgYAtWKT/GVRAccOdDo3uN87aAWb/awhDGIeh2emyOPKAb2Lil9F4ZgC+mpwGzLHqVr2zlpAEfWz6RKmTyh0UhclBRTiO0S1LW4p0c8SVS6fzBWGlwO8eLSK0S2/JqzVBhzyDCbFT0du9jffSCVbHYhOPjzAW0woNIxTS8zvKDQBlbQ==";
    // 公钥
    private static final String 测试客户1_publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAluILCpeE1nSbat/kw6TjlKwfsPxhLVPJEOc40/TKTwtxUPTxrc5dzwDadkkBTSIcMUbNFDJ9jQucqwZW/8DYU6mFcesK++CNzvXMmiUGcuLYIqYH4h7It8xMn0Dh25i8DGsvtYNeiX7jVwj9tJiKV6CxabNX8Q5vLw1DSdJvv0PFrp42Y1UMxhRZ/mKXkUb40fiozuN+I+JwLTiuzdzoCXhtlxazvHWEDW3zNYD1t9WVat+tZTiRBmRhFThrY78AErdBKB6UxO2XYhzu7r3GDHZ55iExKBKIoAbuNuTkW1XO4Cof4tlyRtImyGkVS3ES5qvg4zuCWdM8GAvB2H/mhwIDAQAB";
    // 客户code
    private static final String 测试客户1_code = "WINTER_WELAB";
    // Serving服务地址
    private static final String serverUrl = "http://localhost:8080/serving-service-01/"; // TODO
//    private static final String serverUrl = "http://10.11.21.50:18080/serving-service-03/"; // TODO 参考readme.md
    // 的serverUrl
    // private static final String serverUrl =
    // "https://xbd-fat.tianmiantech.com/serving-service-03/"; // TODO
    // Service Api name
    private static final String apiName = "api/111";
//    private static final String apiName = "api/user/query"; // TODO 参考readme.md 的apiName
    private static final String params = "[{\"field\":\"id\",\"operator\":\"md5\"}]"; // TODO 参考readme.md 的params

    private static Map<String, String> clientDatasetMap; // 哈希数据，原数据

    public static void main(String[] args) throws Exception {
        init(args);
        long start = System.currentTimeMillis();
        // params
        Psi psi = PsiFactory.generatePsi();
        psi.setClientDatasetMap(clientDatasetMap);
        CommunicationConfig config = new CommunicationConfig();
        config.setSignPrivateKey(测试客户1_privateKey);// 私钥
        config.setCommercialId(测试客户1_code); // 客户ID
        // 服务地址
        config.setServerUrl(serverUrl);
        config.setApiName(apiName);
        // 是否要返回结果标签
        psi.setNeedReturnFields(true);
        // 如果是续跑 需要带上下面两个参数
        // config.setRequestId("xxx");
        // psi.setContinue(true);
        psi.setUsePirToReturnFields(true);
        psi.setConfuseData(generateConfuseData());
        List<String> result = psi.query(config, new ArrayList<>(clientDatasetMap.keySet()));
        System.out.println("client size = " + clientDatasetMap.size() + ", result size = " + result.size()
                + ", duration = " + (System.currentTimeMillis() - start));
        // config.setRequestId("xxx");
        // psi.returnFields(config);
    }

    /**
     * 生成混淆数据
     */
    private static ConfuseData generateConfuseData() {
        List<String> fieldNames = parseFieldsByParams();
        if (fieldNames == null || fieldNames.isEmpty()) {
            return new ConfuseData();
        }
        int base = 4; // 查一条混几条
        ConfuseData data = new ConfuseData();
        data.setGenerateDataFunc(s -> {
            // 根据s生成混淆数据
            List<String> list = new LinkedList<>();
            if (fieldNames.size() == 1) { // 单值
                data.setJson(false);
                data.setSingleFieldName(fieldNames.get(0)); // 字段名
                // TODO
                for (int i = 0; i < base; i++) {
                    list.add(new Random().nextInt(20000) + 20000 + "");
                }
            } else {// json格式数据
                data.setJson(true);
                data.setMixFieldNames(fieldNames); // 字段名列表
                for (int i = 0; i < base; i++) {
                    JSONObject obj = new JSONObject();
                    // TODO generate json by fieldNames
                    // obj.put("xxx", xxx);
                    list.add(obj.toJSONString());
                }
            }
            return list;
        });
        return data;
    }

    private static List<String> parseFieldsByParams() {
        List<String> fields = new ArrayList<>();
        JSONArray keyCalcRules = JSONArray.parseArray(params);
        int size = keyCalcRules.size();
        for (int i = 0; i < size; i++) {
            JSONObject item = keyCalcRules.getJSONObject(i);
            String[] fieldArr = item.getString("field").split(",");
            fields.addAll(Arrays.asList(fieldArr));
        }
        return fields;
    }

    private static void init(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            throw new Exception("file is empty");
        }
        initClientDatasetByFile(args[0]);
        System.out.println("get client size = " + clientDatasetMap.size());
    }

    private static void initClientDatasetByFile(String file) throws Exception {
        if (file == null || file.trim().equalsIgnoreCase("")) {
            throw new Exception("file is empty");
        }
        AbstractDataSetReader reader = file.endsWith(".csv") ? new CsvDataSetReader(new File(file))
                : new ExcelDataSetReader(new File(file));
        reader.getHeader();
        List<Map<String, Object>> rows = new ArrayList<>();
        reader.readAll(s -> {
            rows.add(s);
        });
        System.out.println("first data = " + rows.get(0));
        clientDatasetMap = new LinkedHashMap<>();
        rows.stream().forEach(s -> calcKey(s));
    }

    public static String calcKey(Map<String, Object> data) {
        JSONArray keyCalcRules = JSONArray.parseArray(params);
        int size = keyCalcRules.size();
        StringBuilder encodeValue = new StringBuilder(""); // 哈希后的数据
        JSONObject originValueJson = new JSONObject(); // 原数据
        String firstField = "";
        for (int i = 0; i < size; i++) {
            JSONObject item = keyCalcRules.getJSONObject(i);
            String operator = item.getString("operator");
            String[] fields = item.getString("field").split(",");
            firstField = fields[0];
            StringBuffer v = new StringBuffer();
            for (String field : fields) {
                v.append(data.get(field));
                originValueJson.put(field, data.get(field));
            }
            if ("md5".equalsIgnoreCase(operator)) {
                encodeValue.append(getMD5String(v.toString()));
            } else if ("sha256".equalsIgnoreCase(operator)) {
                encodeValue.append(getSHA256String(v.toString()));
            } else { // 不作处理
                encodeValue.append(v.toString());
            }
        }
        // 哈希数据，原数据
        if (originValueJson.size() == 1) {
            clientDatasetMap.put(encodeValue.toString(), originValueJson.getString(firstField));
        } else {
            clientDatasetMap.put(encodeValue.toString(), originValueJson.toJSONString());
        }
        return encodeValue.toString();
    }

    public static String getMD5String(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getSHA256String(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // 计算md5函数
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
