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

import com.welab.wefe.board.service.constant.DataSetAddMethod;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author zane.luo
 */
public class DataSetAddInputModel extends DataSetBaseInputModel {
    @Check(name = "数据集名称", require = true, regex = "^.{4,30}$", messageOnInvalid = "数据集名称长度不能少于4，不能大于30")
    private String name;

    @Check(name = "关键词", require = true, regex = "^.{1,128}$", messageOnInvalid = "关键词太多了啦~")
    private List<String> tags;

    @Check(name = "描述", regex = "^.{0,3072}$", messageOnInvalid = "你写的描述太多了~")
    private String description;

    @Check(messageOnEmpty = "请指定数据集文件")
    private String filename;

    @Check(require = true)
    private DataSetAddMethod dataSetAddMethod;

    @Check(require = true, name = "是否需要去重")
    private boolean deduplication;

    @Check(name = "数据源id")
    private String dataSourceId;

    @Check(name = "sql脚本")
    private String sql;

    public DataSetAddInputModel() {
    }

    public DataSetAddInputModel(String dataSourceId, String sql) {
        this.dataSourceId = dataSourceId;
        this.sql = sql;
    }

    @Override
    public void checkAndStandardize() throws StatusCodeWithException {
        super.checkAndStandardize();

        // 如果来源是数据库，则要求dataSourceId、sql不能为空
        if (DataSetAddMethod.Database.equals(dataSetAddMethod)) {
            if (StringUtils.isEmpty(dataSourceId)) {
                throw new StatusCodeWithException("dataSourceId在数据库不存在", StatusCode.DATA_NOT_FOUND);
            }

            if (StringUtils.isEmpty(sql)) {
                throw new StatusCodeWithException("请填入sql查询语句", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
            }
        } else {
            if (StringUtils.isEmpty(filename)) {
                throw new StatusCodeWithException("请指定数据集文件", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
            }
        }
    }

    //region getter/setter

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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public DataSetAddMethod getDataSetAddMethod() {
        return dataSetAddMethod;
    }

    public void setDataSetAddMethod(DataSetAddMethod dataSetAddMethod) {
        this.dataSetAddMethod = dataSetAddMethod;
    }

    public boolean isDeduplication() {
        return deduplication;
    }

    public void setDeduplication(boolean deduplication) {
        this.deduplication = deduplication;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    //endregion
}
