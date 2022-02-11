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

package com.welab.wefe.union.service.dto.dataresource.dataset.table;

import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryOutput;

/**
 * @author yuxin.zhang
 **/
public class ApiTableDataSetQueryOutput extends ApiDataResourceQueryOutput {

    private ExtraData extraData;

    public static class ExtraData {
        private int containsY;
        private int columnCount;
        private String columnNameList;
        private int featureCount;
        private String featureNameList;

        public int getContainsY() {
            return containsY;
        }

        public void setContainsY(int containsY) {
            this.containsY = containsY;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }

        public String getColumnNameList() {
            return columnNameList;
        }

        public void setColumnNameList(String columnNameList) {
            this.columnNameList = columnNameList;
        }

        public int getFeatureCount() {
            return featureCount;
        }

        public void setFeatureCount(int featureCount) {
            this.featureCount = featureCount;
        }

        public String getFeatureNameList() {
            return featureNameList;
        }

        public void setFeatureNameList(String featureNameList) {
            this.featureNameList = featureNameList;
        }
    }


    public ExtraData getExtraData() {
        return extraData;
    }

    public void setExtraData(ExtraData extraData) {
        this.extraData = extraData;
    }
}
