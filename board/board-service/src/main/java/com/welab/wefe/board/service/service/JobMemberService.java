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

package com.welab.wefe.board.service.service;

import com.alibaba.fastjson.JSONArray;
import com.welab.wefe.board.service.database.entity.job.JobMemberMySqlModel;
import com.welab.wefe.board.service.database.repository.JobMemberRepository;
import com.welab.wefe.board.service.dto.entity.job.JobMemberOutputModel;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zane
 */
@Service
public class JobMemberService extends AbstractService {

    @Autowired
    JobMemberRepository repo;

    /**
     * Get the list of participating members of the job
     */
    public List<JobMemberOutputModel> list(String jobId) {
        return list(jobId, true);
    }

    /**
     * Get the list of participating members of the job
     */
    public List<JobMemberOutputModel> list(String jobId, boolean includeArbiter) {
        Where where = Where
                .create()
                .equal("jobId", jobId);

        if (!includeArbiter) {
            where.notEqual("jobRole", JobMemberRole.arbiter);
        }

        return repo
                .findAll(where.build(JobMemberMySqlModel.class), Sort.by("jobRole"))
                .parallelStream()
                .map(x -> ModelMapper.map(x, JobMemberOutputModel.class))
                .collect(Collectors.toList());
    }

    public JobMemberMySqlModel findIdByMemberInfo(String jobId, JobMemberRole myRole, String memberId) {
        List<Object[]> query = repo.query("select t.id from job_member t left join job j on t.business_id = j.id and t.business_type = 'Job' where j.job_id = '" + jobId + "' and t.job_role = '" + myRole + "' and t.member_id = '" + memberId + "' limit 1");
        if (query != null && !query.isEmpty()) {
            String s = JSONArray.toJSONString(query);
            JSONArray objects = JSONArray.parseArray(s);
            if (objects != null && objects.size() == 1) {
                return repo.findById(objects.get(0).toString()).orElse(null);
            }
        }
        return null;
    }

    /**
     * Query JobMember based on the combination of conditions
     */
    public JobMemberMySqlModel findOneByJobRole(String businessId, JobMemberRole jobRole, String memberId) {
        Where where = Where
                .create()
                .equal("businessId", businessId)
                .equal("jobRole", jobRole)
                .equal("memberId", memberId);

        Specification<JobMemberMySqlModel> jobMemberWhere = where.build(JobMemberMySqlModel.class);

        return repo.findOne(jobMemberWhere).orElse(null);
    }

    /**
     * query JobMember list by job_id
     */
    public List<JobMemberMySqlModel> findListByJobId(String jobId) {
        Where where = Where
                .create()
                .equal("jobId", jobId);

        Specification<JobMemberMySqlModel> jobMemberWhere = where.build(JobMemberMySqlModel.class);

        return repo.findAll(jobMemberWhere);
    }
}
