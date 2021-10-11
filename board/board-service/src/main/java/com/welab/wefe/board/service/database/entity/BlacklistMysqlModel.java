/**
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

package com.welab.wefe.board.service.database.entity;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author lonnie
 */
@Entity(name = "blacklist")
public class BlacklistMysqlModel extends AbstractBaseMySqlModel {

    /**
     * 当前成员id
     */
    @Column(name = "member_id")
    private String memberId;

    /**
     * 加入黑名单的成员id
     */
    @Column(name = "blacklist_member_id")
    private String blacklistMemberId;

    /**
     * 备注
     */
    private String remark;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getBlacklistMemberId() {
        return blacklistMemberId;
    }

    public void setBlacklistMemberId(String blacklistMemberId) {
        this.blacklistMemberId = blacklistMemberId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
