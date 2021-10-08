/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.database.entity.chat;

import com.welab.wefe.board.service.database.entity.base.AbstractMySqlModel;

import javax.persistence.Entity;

/**
 * Recent chat account
 *
 * @author aaron.li
 **/
@Entity(name = "chat_last_account")
public class ChatLastAccountMysqlModel extends AbstractMySqlModel {
    /**
     * Account ID
     */
    private String accountId;
    /**
     * Account name
     */
    private String accountName;

    /**
     * Member ID
     */
    private String memberId;
    /**
     * Member name
     */
    private String memberName;

    /**
     * Contact member ID
     */
    private String liaisonMemberId;
    /**
     * Contact member name
     */
    private String liaisonMemberName;

    /**
     * Contact account ID
     */
    private String liaisonAccountId;
    /**
     * Contact account name
     */
    private String liaisonAccountName;


    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

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

    public String getLiaisonMemberId() {
        return liaisonMemberId;
    }

    public void setLiaisonMemberId(String liaisonMemberId) {
        this.liaisonMemberId = liaisonMemberId;
    }

    public String getLiaisonMemberName() {
        return liaisonMemberName;
    }

    public void setLiaisonMemberName(String liaisonMemberName) {
        this.liaisonMemberName = liaisonMemberName;
    }

    public String getLiaisonAccountId() {
        return liaisonAccountId;
    }

    public void setLiaisonAccountId(String liaisonAccountId) {
        this.liaisonAccountId = liaisonAccountId;
    }

    public String getLiaisonAccountName() {
        return liaisonAccountName;
    }

    public void setLiaisonAccountName(String liaisonAccountName) {
        this.liaisonAccountName = liaisonAccountName;
    }
}
