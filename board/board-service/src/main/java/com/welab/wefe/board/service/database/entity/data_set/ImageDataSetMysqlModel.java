/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.database.entity.data_set;

import javax.persistence.Entity;

/**
 * @author Zane
 */
@Entity(name = "image_data_set")
public class ImageDataSetMysqlModel extends AbstractDataSetMysqlModel {

    /**
     * 任务类型（物体检测...）
     */
    private String forJobType;
    /**
     * label 列表
     */
    private String labelList;
    /**
     * 样本数量
     */
    private int sampleCount;
    /**
     * 已标注数量
     */
    private int labeledCount;
    /**
     * 是否已标注完毕
     */
    private boolean labelCompleted;
    /**
     * 数据集大小
     */
    private long filesSize;


    // region getter/setter

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

    public int getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }

    public int getLabeledCount() {
        return labeledCount;
    }

    public void setLabeledCount(int labeledCount) {
        this.labeledCount = labeledCount;
    }

    public boolean isLabelCompleted() {
        return labelCompleted;
    }

    public void setLabelCompleted(boolean labelCompleted) {
        this.labelCompleted = labelCompleted;
    }

    public long getFilesSize() {
        return filesSize;
    }

    public void setFilesSize(long filesSize) {
        this.filesSize = filesSize;
    }


    // endregion
}
