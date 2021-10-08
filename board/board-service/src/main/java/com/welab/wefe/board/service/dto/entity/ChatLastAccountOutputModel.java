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

package com.welab.wefe.board.service.dto.entity;

/**
 * Recent chat account object output entity
 *
 * @author aaron.li
 **/
public class ChatLastAccountOutputModel extends AbstractOutputModel {

    /**
     * account id
     */
    private String accountId;
    /**
     * account name
     */
    private String accountName;

    /**
     * member id
     */
    private String memberId;
    /**
     * member name
     */
    private String memberName;

    /**
     * liaison member id
     */
    private String liaisonMemberId;
    /**
     * liaison member name
     */
    private String liaisonMemberName;

    /**
     * liaison account id
     */
    private String liaisonAccountId;
    /**
     * liaison account name
     */
    private String liaisonAccountName;

    /**
     * unread num
     */
    private Integer unreadNum = 0;

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

    public Integer getUnreadNum() {
        return unreadNum;
    }

    public void setUnreadNum(Integer unreadNum) {
        this.unreadNum = unreadNum;
    }
}
