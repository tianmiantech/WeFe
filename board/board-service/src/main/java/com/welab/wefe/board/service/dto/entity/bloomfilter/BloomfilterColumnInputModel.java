/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.board.service.dto.entity.bloomfilter;

import com.welab.wefe.common.enums.ColumnDataType;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

/**
 * @author jacky.jiang
 */
public class BloomfilterColumnInputModel {

    /**
     * 字段名称
     */
    private String name;
    /**
     * 数据类型
     */
    private ColumnDataType dataType;
    /**
     * 注释
     */
    @Check(regex = "^.{0,250}$", messageOnInvalid = "注释太长啦~")
    private String comment;

    //region getter/setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColumnDataType getDataType() {
        return dataType;
    }

    public void setDataType(ColumnDataType dataType) {
        this.dataType = dataType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    //endregion
}
