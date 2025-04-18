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
package com.welab.wefe.board.service.dto.kernel.deep_learning;

import com.welab.wefe.board.service.dto.kernel.Member;
import com.welab.wefe.common.wefe.enums.JobMemberRole;

import java.util.List;

/**
 * @author zane
 * @date 2021/11/22
 */
public class KernelJob {
    public String projectId;
    public String jobId;
    public String taskId;
    public String jobType = "paddle_fl";
    public JobMemberRole role;
    public String memberId;
    public Env env;
    public List<Member> members;
}
