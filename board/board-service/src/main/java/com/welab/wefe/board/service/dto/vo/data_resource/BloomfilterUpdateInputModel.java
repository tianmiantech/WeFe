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


import com.welab.wefe.board.service.database.repository.data_resource.BloomFilterRepository;
import com.welab.wefe.board.service.dto.fusion.BloomFilterColumnInputModel;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author jacky.jiang
 * @date 2021/12/2
 */
public class BloomfilterUpdateInputModel extends AbstractDataResourceUpdateInputModel {
    @Check(require = true)
    private List<BloomFilterColumnInputModel> metadataList;

    @Override
    public void checkAndStandardize() throws StatusCodeWithException {
        super.checkAndStandardize();

        if (CollectionUtils.isEmpty(metadataList)) {
            throw new StatusCodeWithException("请设置该过滤器的元数据", StatusCode.PARAMETER_VALUE_INVALID);
        }

        for (BloomFilterColumnInputModel item : metadataList) {
            item.checkAndStandardize();
        }

        int countByName = 0;
        BloomFilterRepository repository = Launcher.CONTEXT.getBean(BloomFilterRepository.class);
        if (StringUtil.isEmpty(super.getId())) {
            countByName = repository.countByName(super.getName());
        } else {
            countByName = repository.countByName(super.getName(), super.getId());
        }

        if (countByName > 0) {
            throw new StatusCodeWithException("此过滤器名称已存在，请换一个数据集名称", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }

    // region getter/setter

    public List<BloomFilterColumnInputModel> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<BloomFilterColumnInputModel> metadataList) {
        this.metadataList = metadataList;
    }


    // endregion
}
