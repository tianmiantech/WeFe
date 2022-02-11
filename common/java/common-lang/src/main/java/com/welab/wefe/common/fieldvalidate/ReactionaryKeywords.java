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
package com.welab.wefe.common.fieldvalidate;

import com.welab.wefe.common.util.StringUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 舆情词库
 *
 * @author zane
 * @date 2021/12/30
 */
public class ReactionaryKeywords {
    private static final String[] 色情词库 = {"二奶"};
    private static final String[] 民生词库 = {"打人"};
    private static final String[] 暴恐词库 = {"枪出售"};
    private static final String[] 政治类 = {"习近平"};
    private static final String[] 政治类_反动词库 = {"共产党"};

    private static final Set<String> KEYWORDS = new HashSet<>();

    static {
        String[][] list = {色情词库, 民生词库, 暴恐词库, 政治类, 政治类_反动词库};
        for (String[] dic : list) {
            KEYWORDS.addAll(Arrays.asList(dic));
        }
    }

    /**
     * 检查文本中是否包含舆情关键字
     */
    public static boolean contains(String str) {
        if (StringUtil.isEmpty(str)) {
            return false;
        }

        return KEYWORDS
                .parallelStream()
                .anyMatch(x -> str.contains(x));
    }

    public static String match(String str) {
        if (StringUtil.isEmpty(str)) {
            return null;
        }

        return KEYWORDS
                .parallelStream()
                .filter(x -> str.contains(x))
                .findFirst()
                .orElse(null);
    }

    public static void main(String[] args) {
        System.out.println(KEYWORDS);
        System.out.println(KEYWORDS.size());

        contains("罢工");

        for (int i = 0; i < 20; i++) {
            String str = UUID.randomUUID().toString();
            long start = System.currentTimeMillis();
            contains(str);
            long spend = System.currentTimeMillis() - start;
            System.out.println("spend:" + spend);
        }
    }

}
