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

package com.welab.wefe.board.service.dto.entity.job;

import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.util.ModelMapper;

import java.util.List;

/**
 * @author zane.luo
 */
public class TaskOutputView extends AbstractOutputModel {
    @Check(name = "由组件创建的 task")
    private TaskOutputModel task;
    @Check(name = "task 输出的结果")
    private List<TaskResultOutputModel> results;

    public TaskOutputView() {
    }

    public TaskOutputView(TaskMySqlModel task) {
        this(task, null);
    }

    public TaskOutputView(TaskMySqlModel task, List<TaskResultOutputModel> results) {
        this.task = ModelMapper.map(task, TaskOutputModel.class);
        this.results = results;
    }

    //region getter/setter

    public TaskOutputModel getTask() {
        return task;
    }

    public void setTask(TaskOutputModel task) {
        this.task = task;
    }

    public List<TaskResultOutputModel> getResults() {
        return results;
    }

    public void setResults(List<TaskResultOutputModel> results) {
        this.results = results;
    }


    //endregion
}
