/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.dto.vo;

import com.welab.wefe.board.service.dto.entity.data_set.DataSetColumnInputModel;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.ColumnDataType;
import com.welab.wefe.common.enums.DataSetPublicLevel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author zane.luo
 */
public class DataSetBaseInputModel extends AbstractApiInput {
    @Check(name = "可见级别", require = true)
    private DataSetPublicLevel publicLevel;
    @Check(
            name = "可见成员列表",
            desc = "只有在列表中的联邦成员才可以看到该数据集的基本信息",
            regex = "^.{0,3072}$",
            messageOnInvalid = "你选择的 member 太多了~"
    )
    private String publicMemberList;
    @Check(require = true)
    private List<DataSetColumnInputModel> metadataList;

    @Override
    public void checkAndStandardize() throws StatusCodeWithException {
        super.checkAndStandardize();

        if (publicLevel == DataSetPublicLevel.PublicWithMemberList && StringUtils.isEmpty(publicMemberList)) {
            throw new StatusCodeWithException("请指定可见成员", StatusCode.PARAMETER_VALUE_INVALID);
        }

        if (CollectionUtils.isEmpty(metadataList)) {
            throw new StatusCodeWithException("请设置该数据集的元数据", StatusCode.PARAMETER_VALUE_INVALID);
        }

        for (DataSetColumnInputModel item : metadataList) {
            if (item.getDataType() == null) {
                throw new StatusCodeWithException("请给字段【" + item.getName() + "】设置数据类型", StatusCode.PARAMETER_VALUE_INVALID);
            }

            if (item.getDataType() == ColumnDataType.String || item.getDataType() == ColumnDataType.Enum) {
                throw new StatusCodeWithException("目前暂不支持 String/Enum 数据类型，如有需要，请将该特征进行编码后使用。", StatusCode.PARAMETER_VALUE_INVALID);
            }
        }
    }


    // region getter/setter

    public DataSetPublicLevel getPublicLevel() {
        return publicLevel;
    }

    public void setPublicLevel(DataSetPublicLevel publicLevel) {
        this.publicLevel = publicLevel;
    }

    public String getPublicMemberList() {
        return publicMemberList;
    }

    public void setPublicMemberList(String publicMemberList) {
        this.publicMemberList = publicMemberList;
    }

    public List<DataSetColumnInputModel> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<DataSetColumnInputModel> metadataList) {
        this.metadataList = metadataList;
    }


    // endregion
}
