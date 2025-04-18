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

package com.welab.wefe.manager.service.dto.dataresource;

/**
 * @author yuxin.zhang
 **/
public class ApiBloomFilterQueryOutput extends ApiDataResourceQueryOutput {

    private ExtraData extraData;

    public static class ExtraData {
        private String hashFunction;

        public String getHashFunction() {
            return hashFunction;
        }

        public void setHashFunction(String hashFunction) {
            this.hashFunction = hashFunction;
        }
    }

    public ExtraData getExtraData() {
        return extraData;
    }

    public void setExtraData(ExtraData extraData) {
        this.extraData = extraData;
    }
}
