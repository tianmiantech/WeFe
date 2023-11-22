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

package com.welab.wefe.data.fusion.service.utils.primarykey;

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.util.Sha1;

import java.util.List;

/**
 * The primary key tool
 *
 * @author hunter.zhao
 */
public class PrimaryKeyUtils {

    /**
     * Generate primary keys according to the given rules
     */
    public static String create(JObject data, List<FieldInfo> fieldInfos) {

        StringBuilder builder = new StringBuilder();
        fieldInfos.forEach(x -> {
            switch (x.getOptions()) {
                case MD5:
                    builder.append(md5(data, x.getColumnList()));
                    break;
                case SHA1:
                    builder.append(sha1(data, x.getColumnList()));
                    break;
                case SUBSTRING:
                    builder.append(subString(data, x.getColumnList(), x.getFristIndex(), x.getEndIndex()));
                    break;
                case NONE:
                    builder.append(none(data, x.getColumnList()));
                    break;
                default:
                    break;
            }
        });

        return builder.toString();
    }

    /**
     * MD5 processing
     *
     * @param data
     * @param columns
     * @return
     */
    private static String md5(JObject data, List<String> columns) {
        StringBuilder builder = new StringBuilder();

        columns.forEach(x -> builder.append(data.getString(x)));

        return Md5.of(builder.toString());
    }

    /**
     * SHA1
     *
     * @param data
     * @param columns
     * @return
     */
    private static String sha1(JObject data, List<String> columns) {
        StringBuilder builder = new StringBuilder();

        columns.forEach(x -> builder.append(data.getString(x)));

        return Sha1.of(builder.toString());
    }

    /**
     * Intercept processing
     *
     * @param data
     * @param columns
     * @param fristIndex
     * @param endIndex
     * @return
     */
    private static String subString(JObject data, List<String> columns, int fristIndex, int endIndex) {
        StringBuilder builder = new StringBuilder();

        columns.forEach(x -> builder.append(data.getString(x)));

        return builder.toString().substring(fristIndex, endIndex);
    }

    /**
     * none
     *
     * @param data
     * @param columns
     * @return
     */
    private static String none(JObject data, List<String> columns) {
        StringBuilder builder = new StringBuilder();

        columns.forEach(x -> builder.append(data.getString(x)));

        return builder.toString();
    }

    public static String hashFunction(List<FieldInfo> fieldInfos) {
        StringBuilder builder = new StringBuilder();
        fieldInfos.forEach(x -> {
            switch (x.getOptions()) {
                case MD5:
                    builder.append(md5HashStr(x.getColumnList()) + "+");
                    break;
                case SHA1:
                    builder.append(shaHashStr(x.getColumnList()) + "+");
                    break;
                case NONE:
                    builder.append(noneHashStr(x.getColumnList()) + "+");
                    break;
                default:
                    break;
            }
        });

        return builder.substring(0, builder.length() - 1);
    }


    private static String md5HashStr(List<String> columnList) {
        StringBuilder builder = new StringBuilder(16);
        columnList.forEach(
                x -> builder.append(x + "+")
        );
        return "MD5(" + builder.substring(0, builder.length() - 1) + ")";
    }

    private static String shaHashStr(List<String> columnList) {
        StringBuilder builder = new StringBuilder(16);
        columnList.forEach(
                x -> builder.append(x + "+")
        );
        return "SHA(" + builder.substring(0, builder.length() - 1) + ")";
    }

    private static String noneHashStr(List<String> columnList) {
        StringBuilder builder = new StringBuilder(16);
        columnList.forEach(
                x -> builder.append(x + "+")
        );
        return builder.substring(0, builder.length() - 1);
    }
}
