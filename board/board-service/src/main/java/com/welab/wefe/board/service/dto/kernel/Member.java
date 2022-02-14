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

package com.welab.wefe.board.service.dto.kernel;

import com.welab.wefe.board.service.database.entity.job.JobMemberMySqlModel;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.wefe.enums.JobMemberRole;


/**
 * @author zane.luo
 */
public class Member {

    private String memberId;
    private String memberName;
    private JobMemberRole memberRole;

    public Member() {
    }

    public Member(JobMemberMySqlModel member) {
        this.memberId = member.getMemberId();

        this.memberName = CacheObjects.getMemberName(member.getMemberId());
        this.memberRole = member.getJobRole();
    }

    //region getter/setter

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public JobMemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(JobMemberRole memberRole) {
        this.memberRole = memberRole;
    }


    //endregion
}
