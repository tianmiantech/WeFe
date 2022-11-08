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
package com.welab.wefe.common.test;

import com.alibaba.fastjson.util.TypeUtils;

import java.util.Date;

/**
 * @author zane.luo
 * @date 2022/11/8
 */
public class ConvertDateTimeTest {
    public static void main(String[] args) {
        String[] array = {

                "2022年11月8日 13:45:30",
                "2022/11/8 13:45:30",
                "2022-11-8 13:45:30",

                "2022年11月8日T13:45:30",
                "2022/11/8T13:45:30",
                "2022-11-8T13:45:30",

                "2022年11月8日T13:45",
                "2022/11/8T13:45",
                "2022-11-8T13:45",

                "2022年11月8日",
                "2022/11/8",
                "2022-11-8",
                "2022-1-8",

        };
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            if (i % 10000 == 0) {
                System.out.println(i);
            }
            for (String str : array) {
//                System.out.println(str);

                str = str
                        .replaceAll("T", " ")
                        .replaceAll("/", "-")
                        .replaceAll("(?<=-)(\\d)(?=\\b)", "0$1")
                        .replaceAll("(?<= )(\\d{1,2}:\\d{1,2})(?=$)", "$1:00");
                Date date = TypeUtils.castToDate(str);

//                System.out.println(Convert.toString(date));
//                System.out.println();
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("spend:" + (end - start));
    }

}
