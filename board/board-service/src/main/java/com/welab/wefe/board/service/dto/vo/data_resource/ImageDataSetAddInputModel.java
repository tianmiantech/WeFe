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

package com.welab.wefe.board.service.dto.vo.data_resource;

import com.welab.wefe.common.enums.DeepLearningJobType;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

/**
 * @author zane.luo
 */
public class ImageDataSetAddInputModel extends ImageDataSetUpdateInputModel {
    @Check(require = true, messageOnEmpty = "请指定数据集文件")
    public String filename;
    @Check(name = "数据集应用的任务类型", require = true)
    public DeepLearningJobType forJobType;

    // region getter/setter

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public DeepLearningJobType getForJobType() {
        return forJobType;
    }

    public void setForJobType(DeepLearningJobType forJobType) {
        this.forJobType = forJobType;
    }

    // endregion

}
