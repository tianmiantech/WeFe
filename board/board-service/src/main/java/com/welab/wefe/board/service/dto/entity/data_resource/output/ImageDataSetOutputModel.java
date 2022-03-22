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
package com.welab.wefe.board.service.dto.entity.data_resource.output;


import com.alibaba.fastjson.annotation.JSONField;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.DeepLearningJobType;

import java.util.List;
import java.util.TreeSet;

/**
 * @author zane
 * @date 2021/12/1
 */
public class ImageDataSetOutputModel extends DataResourceOutputModel {
    @Check(name = "任务类型;物体检测...）")
    private DeepLearningJobType forJobType;
    @Check(name = "label;列表")
    private String labelList;
    @Check(name = "已标注数量")
    private Long labeledCount;
    @Check(name = "是否已标注完毕")
    private boolean labelCompleted;
    @Check(name = "数据集大小")
    private Long filesSize;

    @JSONField(serialize = false)
    public TreeSet<String> getLabelSet() {
        TreeSet<String> labelSet = new TreeSet<>();
        if (StringUtil.isEmpty(labelList)) {
            return labelSet;
        }

        List<String> list = StringUtil.splitWithoutEmptyItem(labelList, ",");
        for (String label : list) {
            labelSet.add(label);
        }
        return labelSet;
    }

    // region getter/setter


    public DeepLearningJobType getForJobType() {
        return forJobType;
    }

    public void setForJobType(DeepLearningJobType forJobType) {
        this.forJobType = forJobType;
    }

    public String getLabelList() {
        return labelList;
    }

    public void setLabelList(String labelList) {
        this.labelList = labelList;
    }

    public Long getLabeledCount() {
        return labeledCount;
    }

    public void setLabeledCount(Long labeledCount) {
        this.labeledCount = labeledCount;
    }

    public boolean isLabelCompleted() {
        return labelCompleted;
    }

    public void setLabelCompleted(boolean labelCompleted) {
        this.labelCompleted = labelCompleted;
    }

    public Long getFilesSize() {
        return filesSize;
    }

    public void setFilesSize(Long filesSize) {
        this.filesSize = filesSize;
    }


    // endregion
}
