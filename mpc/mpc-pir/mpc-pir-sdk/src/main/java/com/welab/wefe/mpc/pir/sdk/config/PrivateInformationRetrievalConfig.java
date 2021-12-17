
/*
 * *
 *  * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
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
     * 查询的接口名
     */
    private String apiName;
    /**
     * 服务器接口地址
     */
    private String serverUrl;
    /**
     * 真正查询用户在primaryKeys的位置
     */
    private Integer targetIndex = -1;
    /**
     * 混淆查询集
     */
    private List<Object> primaryKeys;
    /**
     * 混淆查询主键数量
     */
    private int confuseCount = 100;
    /**
     * 商户id
     */
    private String commercialId;
    /**
     * 签名私钥
     */
    private String signPrivateKey;
    /**
     * 生成的混淆查询主键接口
     */
    private GenerateConfuse mGenerateConfuse;
    /**
     * 是否需要签名
     */
    private boolean needSign = true;

    /**
     * 是否需要调用生成混淆主键接口
     */
    private boolean needGenerateConfuse = false;


    public PrivateInformationRetrievalConfig(String apiName, String serverUrl, Integer targetIndex, List<Object> primaryKeys, int confuseCount, String commercialId, String signPrivateKey, GenerateConfuse generateConfuse, boolean needSign, boolean needGenerateConfuse) {
        apiName = apiName;
        serverUrl = serverUrl;
        targetIndex = targetIndex;
        primaryKeys = primaryKeys;
        confuseCount = confuseCount;
        commercialId = commercialId;
        signPrivateKey = signPrivateKey;
        mGenerateConfuse = generateConfuse;
        needSign = needSign;
        needGenerateConfuse = needGenerateConfuse;

        init();
    }

    private void init() {
        if (primaryKeys != null) {
            if (primaryKeys.size() == 1) {
                if (mGenerateConfuse != null) {
                    targetIndex = new Random().nextInt(confuseCount);
                    List<Object> value = mGenerateConfuse.generate(confuseCount, primaryKeys.get(0));
                    value.add(targetIndex, primaryKeys.get(0));
                    primaryKeys = value;
                }
            } else {
                confuseCount = primaryKeys.size();
            }
        }
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public Integer getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(Integer targetIndex) {
        this.targetIndex = targetIndex;
    }

    public List<Object> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<Object> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public int getConfuseCount() {
        return confuseCount;
    }

    public void setConfuseCount(int confuseCount) {
        this.confuseCount = confuseCount;
    }

    public String getCommercialId() {
        return commercialId;
    }

    public void setCommercialId(String commercialId) {
        this.commercialId = commercialId;
    }

    public String getSignPrivateKey() {
        return signPrivateKey;
    }

    public void setSignPrivateKey(String signPrivateKey) {
        this.signPrivateKey = signPrivateKey;
    }

    public GenerateConfuse getmGenerateConfuse() {
        return mGenerateConfuse;
    }

    public void setmGenerateConfuse(GenerateConfuse mGenerateConfuse) {
        this.mGenerateConfuse = mGenerateConfuse;
    }

    public boolean isNeedSign() {
        return needSign;
    }

    public void setNeedSign(boolean needSign) {
        this.needSign = needSign;
    }

    public boolean isNeedGenerateConfuse() {
        return needGenerateConfuse;
    }

    public void setNeedGenerateConfuse(boolean needGenerateConfuse) {
        this.needGenerateConfuse = needGenerateConfuse;
    }

}
