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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.database.entity.flow.FlowActionQueueMySqlModel;
import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.repository.FlowActionQueueRepository;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.wefe.enums.FlowActionType;
import com.welab.wefe.common.wefe.enums.ProducerType;
import com.welab.wefe.common.wefe.enums.ProjectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane.luo
 */
@Service
public class FlowActionQueueService extends AbstractService {

    @Autowired
    private FlowActionQueueRepository flowActionQueueRepository;
    @Autowired
    private JobService jobService;

    public void runJob(AbstractApiInput input, String jobId, ProjectType projectType) {
        JObject params =
                projectType == ProjectType.DeepLearning
                        ? JObject.create("type", "visualfl")
                        : null;

        notifyFlow(
                input,
                jobId,
                FlowActionType.run_job,
                params
        );
    }

    public void notifyFlow(AbstractApiInput input, String jobId, FlowActionType actionType) {

        notifyFlow(input, jobId, actionType, null);
    }

    /**
     * send a action message to flow service
     */
    public void notifyFlow(AbstractApiInput input, String jobId, FlowActionType actionType, JObject params) {

        if (params == null) {
            params = new JObject();
        }

        for (JobMySqlModel job : jobService.listByJobId(jobId)) {

            FlowActionQueueMySqlModel action = new FlowActionQueueMySqlModel();
            action.setAction(actionType);
            action.setProducer(input.fromGateway() ? ProducerType.gateway : ProducerType.board);
            action.setPriority(0);
            action.setParams(
                    params
                            .put("jobId", job.getJobId())
                            .put("dstRole", job.getMyRole().name())
                            .toStringWithNull()
            );
            flowActionQueueRepository.save(action);

        }
    }
}
