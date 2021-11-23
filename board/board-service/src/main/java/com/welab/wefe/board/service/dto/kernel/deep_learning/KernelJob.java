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
package com.welab.wefe.board.service.dto.kernel.deep_learning;

import com.welab.wefe.common.enums.JobMemberRole;

/**
 * @author zane
 * @date 2021/11/22
 */
public class KernelJob {
    public String jobId;
    public String task_id;
    public String job_type;
    public JobMemberRole role;
    public String member_id;
    public JobConfig jobConfig;
}
