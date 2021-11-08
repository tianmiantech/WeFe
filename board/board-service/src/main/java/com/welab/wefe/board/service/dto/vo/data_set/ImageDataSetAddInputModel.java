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

package com.welab.wefe.board.service.dto.vo.data_set;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zane.luo
 */
public class ImageDataSetAddInputModel extends AbstractDataSetUpdateInputModel {
    @Check(messageOnEmpty = "请指定数据集文件")
    public String filename;

    @Override
    public void checkAndStandardize() throws StatusCodeWithException {
        super.checkAndStandardize();

        if (StringUtils.isEmpty(filename)) {
            throw new StatusCodeWithException("请指定数据集文件", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
        }
    }

    // region getter/setter

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    // endregion

}
