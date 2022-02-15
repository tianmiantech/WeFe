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

package com.welab.wefe.board.service.dto.vo.data_resource;


import com.welab.wefe.board.service.base.file_system.UploadFile;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.DeepLearningJobType;

import java.io.File;

/**
 * @author zane.luo
 */
public class ImageDataSetAddInputModel extends ImageDataSetUpdateInputModel {
    @Check(require = true, messageOnEmpty = "请指定数据集文件")
    public String filename;
    @Check(name = "数据集应用的任务类型", require = true)
    public DeepLearningJobType forJobType;

    @Override
    public void checkAndStandardize() throws StatusCodeWithException {
        super.checkAndStandardize();

        File file = UploadFile.getFilePath(DataResourceType.ImageDataSet, filename).toFile();

        if (!file.exists()) {
            StatusCode
                    .FILE_IO_ERROR
                    .throwException("未找到文件：" + filename + "，请重试刷新页面后重新上传。");
        }
    }

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
