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

/**
 * @author Zane
 */
public class TableDataSetAddOutput {
    private String id;
    private long repeatDataCount;


    public TableDataSetAddOutput() {
    }

    public TableDataSetAddOutput(String id, int repeatDataCount) {
        this.id = id;
        this.repeatDataCount = repeatDataCount;
    }


    //region getter/setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getRepeatDataCount() {
        return repeatDataCount;
    }

    public void setRepeatDataCount(long repeatDataCount) {
        this.repeatDataCount = repeatDataCount;
    }

    //endregion
}