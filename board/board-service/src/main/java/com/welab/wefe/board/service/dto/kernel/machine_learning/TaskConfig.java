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

package com.welab.wefe.board.service.dto.kernel.machine_learning;


import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.ComponentType;

import java.util.Map;

/**
 * @author zane.luo
 */
public class TaskConfig {
    private KernelJob job;
    private ComponentType module;

    @Check(name = "组件的输入相关信息")
    private Map<String, Object> input;
    @Check(name = "组件的输出相关信息")
    private Map<String, Object> output;

    /**
     * 组件参数
     * <p>
     * 此处迁就 kernel，需要大写字母开头。
     */
    private Map<String, Object> params;

    private KernelTask task;

    //region getter/setter

    public KernelJob getJob() {
        return job;
    }

    public void setJob(KernelJob job) {
        this.job = job;
    }

    public ComponentType getModule() {
        return module;
    }

    public void setModule(ComponentType module) {
        this.module = module;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public void setInput(Map<String, Object> input) {
        this.input = input;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public KernelTask getTask() {
        return task;
    }

    public void setTask(KernelTask task) {
        this.task = task;
    }

//endregion
}
