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

package com.welab.wefe.union.service.dto.dataresource.dataset.image;

import com.welab.wefe.union.service.dto.dataresource.ApiDataResourceQueryOutput;

/**
 * @author yuxin.zhang
 **/
public class ApiImageDataSetQueryOutput extends ApiDataResourceQueryOutput {

    private ExtraData extraData;

    public static class ExtraData {
        private String forJobType;
        private String labelList;
        private int labeledCount;
        private int labelCompleted;
        private String fileSize;

        public String getForJobType() {
            return forJobType;
        }

        public void setForJobType(String forJobType) {
            this.forJobType = forJobType;
        }

        public String getLabelList() {
            return labelList;
        }

        public void setLabelList(String labelList) {
            this.labelList = labelList;
        }

        public int getLabeledCount() {
            return labeledCount;
        }

        public void setLabeledCount(int labeledCount) {
            this.labeledCount = labeledCount;
        }

        public int getLabelCompleted() {
            return labelCompleted;
        }

        public void setLabelCompleted(int labelCompleted) {
            this.labelCompleted = labelCompleted;
        }

        public String getFileSize() {
            return fileSize;
        }

        public void setFileSize(String fileSize) {
            this.fileSize = fileSize;
        }
    }

    public ExtraData getExtraData() {
        return extraData;
    }

    public void setExtraData(ExtraData extraData) {
        this.extraData = extraData;
    }
}
