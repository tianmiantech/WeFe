/*
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
package com.welab.wefe.board.service.dto.entity.data_resource.output;

import com.welab.wefe.board.service.constant.BloomfilterAddMethod;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

/**
 * @author zane
 * @date 2021/12/1
 */
public class BloomFilterOutputModel extends DataResourceOutputModel {
    @Check(name = "数据源id")
    private String dataSourceId;
    @Check(name = "数据源地址")
    private String sourcePath;
    @Check(name = "主键hash生成方法")
    private String hashFunction;
    @Check(name = "布隆过滤器添加方式")
    private BloomfilterAddMethod addMethod;
    @Check(name = "sql语句")
    private String sqlScript;

    // region getter/setter

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getHashFunction() {
        return hashFunction;
    }

    public void setHashFunction(String hashFunction) {
        this.hashFunction = hashFunction;
    }

    public BloomfilterAddMethod getAddMethod() {
        return addMethod;
    }

    public void setAddMethod(BloomfilterAddMethod addMethod) {
        this.addMethod = addMethod;
    }

    public String getSqlScript() {
        return sqlScript;
    }

    public void setSqlScript(String sqlScript) {
        this.sqlScript = sqlScript;
    }


    // endregion
}
