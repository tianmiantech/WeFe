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

import com.welab.wefe.board.service.database.repository.ImageDataSetRepository;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.DeepLearningJobType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;

/**
 * @author zane
 * @date 2021/11/8
 */
public class ImageDataSetUpdateInputModel extends AbstractDataSetUpdateInputModel {
    @Check(name = "数据集应用的任务类型", require = true)
    public DeepLearningJobType forJobType;

    @Override
    public void checkAndStandardize() throws StatusCodeWithException {
        super.checkAndStandardize();

        int countByName = 0;
        ImageDataSetRepository repository = Launcher.CONTEXT.getBean(ImageDataSetRepository.class);
        if (StringUtil.isEmpty(getId())) {
            countByName = repository.countByName(super.getName());
        } else {
            countByName = repository.countByName(super.getName(), getId());
        }

        if (countByName > 0) {
            throw new StatusCodeWithException("此数据集名称已存在，请换一个数据集名称", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    // region getter/setter

    public DeepLearningJobType getForJobType() {
        return forJobType;
    }

    public void setForJobType(DeepLearningJobType forJobType) {
        this.forJobType = forJobType;
    }

    // endregion
}
