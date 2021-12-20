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

package com.welab.wefe.board.service.dto.entity.project;

import com.welab.wefe.board.service.dto.entity.AbstractOutputModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.wefe.enums.ComponentType;

/**
 * @author lonnie
 */
public class ProjectModelingOutputModel extends AbstractOutputModel {

    @Check(name = "流程id")
    private String flowId;

    @Check(name = "job_id")
    private String jobId;

    @Check(name = "job名字")
    private String jobName;

    @Check(name = "模型评估任务id")
    private String evaluationTaskId;

    @Check(name = "模型类型")
    private ComponentType modelingType;

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getEvaluationTaskId() {
        return evaluationTaskId;
    }

    public void setEvaluationTaskId(String evaluationTaskId) {
        this.evaluationTaskId = evaluationTaskId;
    }

    public ComponentType getModelingType() {
        return modelingType;
    }

    public void setModelingType(ComponentType modelingType) {
        this.modelingType = modelingType;
    }

}
