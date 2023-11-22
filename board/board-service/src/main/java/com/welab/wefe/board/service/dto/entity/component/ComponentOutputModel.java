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

package com.welab.wefe.board.service.dto.entity.component;

import com.welab.wefe.common.fieldvalidate.annotation.Check;

/**
 * @author aaron.li
 **/
public class ComponentOutputModel {
    @Check(name = "组件唯一标识")
    private String id;
    @Check(name = "组件中文名称")
    private String name;
    @Check(name = "描述")
    private String desc;

    public ComponentOutputModel(String id, String name, String desc) {
        this.id = id;
        this.name = name;
        this.desc = desc;
    }

    //region getter/setter


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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    //endregion

}
