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

import com.welab.wefe.board.service.constant.DataSetAddMethod;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zane.luo
 */
public class TableDataSetAddInputModel extends TableDataSetUpdateInputModel {
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

    public TableDataSetAddInputModel() {
    }

    public TableDataSetAddInputModel(String dataSourceId, String sql) {
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    //endregion
}
