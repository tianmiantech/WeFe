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

import com.welab.wefe.board.service.database.repository.data_resource.DataResourceRepository;
import com.welab.wefe.board.service.dto.globalconfig.MemberInfoModel;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.wefe.enums.DataSetPublicLevel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author zane.luo
 */
public class AbstractDataResourceUpdateInputModel extends AbstractApiInput {
    private String id;

    @Check(name = "数据集名称", require = true, regex = "^.{4,30}$", messageOnInvalid = "数据集名称长度不能少于4，不能大于30")
    private String name;
    @Check(name = "描述", regex = "^.{0,3072}$", messageOnInvalid = "你写的描述太多了~")
    private String description;
    @Check(name = "关键词", require = true, regex = "^.{1,128}$", messageOnInvalid = "关键词太多了啦~")
    private List<String> tags;


    @Check(name = "可见级别", require = true)
    private DataSetPublicLevel publicLevel;
    @Check(
            name = "可见成员列表",
            desc = "只有在列表中的联邦成员才可以看到该数据集的基本信息",
            regex = "^.{0,3072}$",
            messageOnInvalid = "你选择的 member 太多了~"
    )
    private String publicMemberList;


    public AbstractDataResourceUpdateInputModel() {
    }

    public AbstractDataResourceUpdateInputModel(String name, List<String> tags, String description) {
        this.name = name;
        this.tags = tags;
        this.description = description;
    }

    @Override
    public void checkAndStandardize() throws StatusCodeWithException {
        super.checkAndStandardize();

        // 当全局拒绝暴露时，禁止选择暴露资源。
        MemberInfoModel member = Launcher.getBean(GlobalConfigService.class).getMemberInfo();
        if (publicLevel != DataSetPublicLevel.OnlyMyself) {
            if (!member.getMemberAllowPublicDataSet()) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("当前联邦成员不允许资源对外可见，请在[全局设置][成员设置]中开启。");
            }

            if (member.getMemberHidden()) {
                StatusCode.PARAMETER_VALUE_INVALID.throwException("当前联邦成员已被管理员隐身，隐身状态下不允许资源可见。");
            }
        }


        if (publicLevel == DataSetPublicLevel.PublicWithMemberList && StringUtils.isEmpty(publicMemberList)) {
            throw new StatusCodeWithException("请指定可见成员", StatusCode.PARAMETER_VALUE_INVALID);
        }

        int countByName = 0;
        DataResourceRepository repository = Launcher.getBean(DataResourceRepository.class);
        if (StringUtil.isEmpty(id)) {
            countByName = repository.countByName(name);
        } else {
            countByName = repository.countByName(name, id);
        }

        if (countByName > 0) {
            throw new StatusCodeWithException("此资源名称已存在，请换一个名称", StatusCode.PARAMETER_VALUE_INVALID);
        }
    }


    // region getter/setter


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
