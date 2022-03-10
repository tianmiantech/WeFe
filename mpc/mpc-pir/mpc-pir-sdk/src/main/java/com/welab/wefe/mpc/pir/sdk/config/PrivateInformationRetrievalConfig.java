
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

package com.welab.wefe.mpc.pir.sdk.config;

import com.welab.wefe.mpc.pir.sdk.confuse.GenerateConfuse;

import java.util.List;
import java.util.Random;

/**
 * @Author eval
 * @Date 2021/12/15
 **/
public class PrivateInformationRetrievalConfig {

    /**
     * 真正查询用户在primaryKeys的位置，必须大于0
     */
    private int targetIndex = -1;
    /**
     * 混淆查询集，不能为null和空
     */
    private List<Object> primaryKeys;
    /**
     * 混淆查询主键数量
     * 当primaryKeys大小为1时生效
     */
    private int confuseCount = 100;

    /**
     * 生成的混淆查询主键接口
     * 当primaryKeys大小为1时生效
     */
    private GenerateConfuse generateConfuse;

    public PrivateInformationRetrievalConfig(List<Object> primaryKeys, GenerateConfuse generateConfuse) {
        this(primaryKeys, 0, 10, generateConfuse);
    }

    public PrivateInformationRetrievalConfig(List<Object> primaryKeys, int targetIndex, int confuseCount, GenerateConfuse generateConfuse) {
        if (targetIndex < 0) {
            throw new IllegalArgumentException("Illegal initial target index:" + targetIndex);
        }
        if (primaryKeys == null || primaryKeys.isEmpty()) {
            throw new IllegalArgumentException("Illegal initial query keys");
        }
        if (primaryKeys.size() == 1 && generateConfuse == null) {
            throw new IllegalArgumentException("Illegal initial primaryKeys size:" + primaryKeys.size() + ", generate confuse is null");
        }
        if (primaryKeys.size() == 1 && confuseCount <= 1) {
            throw new IllegalArgumentException("Illegal initial primaryKeys size:" + primaryKeys.size() + ",confuse count:" + confuseCount);
        }
        this.targetIndex = targetIndex;
        this.primaryKeys = primaryKeys;
        this.generateConfuse = generateConfuse;
        this.confuseCount = primaryKeys.size() > 1 ? primaryKeys.size() : confuseCount;
        init();
    }

    private void init() {
        if (primaryKeys.size() == 1) {
            targetIndex = new Random().nextInt(confuseCount);
            List<Object> value = generateConfuse.generate(confuseCount - 1, primaryKeys.get(0));
            value.add(targetIndex, primaryKeys.get(0));
            primaryKeys = value;
        }
    }

    public Integer getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }

    public List<Object> getPrimaryKeys() {
        return primaryKeys;
    }

}
