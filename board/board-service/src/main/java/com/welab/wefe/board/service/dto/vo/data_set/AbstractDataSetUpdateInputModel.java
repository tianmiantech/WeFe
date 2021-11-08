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
import com.welab.wefe.common.enums.DataSetPublicLevel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author zane.luo
 */
public class AbstractDataSetUpdateInputModel extends AbstractApiInput {
    @Check(name = "数据集名称", require = true, regex = "^.{4,30}$", messageOnInvalid = "数据集名称长度不能少于4，不能大于30")
    protected String name;

    @Check(name = "关键词", require = true, regex = "^.{1,128}$", messageOnInvalid = "关键词太多了啦~")
    protected List<String> tags;

    @Check(name = "描述", regex = "^.{0,3072}$", messageOnInvalid = "你写的描述太多了~")
    protected String description;

    @Check(name = "可见级别", require = true)
    private DataSetPublicLevel publicLevel;
    @Check(
            name = "可见成员列表",
            desc = "只有在列表中的联邦成员才可以看到该数据集的基本信息",
            regex = "^.{0,3072}$",
            messageOnInvalid = "你选择的 member 太多了~"
    )
    private String publicMemberList;


    public AbstractDataSetUpdateInputModel() {
    }

    public AbstractDataSetUpdateInputModel(String name, List<String> tags, String description) {
        this.name = name;
        this.tags = tags;
        this.description = description;
    }

    @Override
    public void checkAndStandardize() throws StatusCodeWithException {
        super.checkAndStandardize();

        if (publicLevel == DataSetPublicLevel.PublicWithMemberList && StringUtils.isEmpty(publicMemberList)) {
            throw new StatusCodeWithException("请指定可见成员", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }


    // region getter/setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

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


    // endregion
}
