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

package com.welab.wefe.serving.service.api.account;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.util.Masker;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.AccountService;

@Api(path = "account/query", name = "query account by pagination")
public class QueryApi extends AbstractApi<QueryApi.Input, PagingOutput<QueryApi.Output>> {

	@Autowired
	private AccountService accountService;

	@Override
	protected ApiResult<PagingOutput<Output>> handle(Input input) throws Exception {
		return success(accountService.query(input));
	}

	public static class Input extends PagingInput {
		private String phoneNumber;

		private String nickname;
		private AuditStatus auditStatus;

		// region getter/setter

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public AuditStatus getAuditStatus() {
			return auditStatus;
		}

		public void setAuditStatus(AuditStatus auditStatus) {
			this.auditStatus = auditStatus;
		}

		// endregion
	}

	public static class Output extends AbstractApiOutput {

		private String phoneNumber;
		private String nickname;
		private String email;
		private Boolean superAdminRole;
		private Boolean adminRole;
		private AuditStatus auditStatus;
		private String auditComment;
		private Boolean enable;
		private String id;
		private String createdBy;
		private Date createdTime;
		private String updatedBy;
		private Date updatedTime;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getCreatedBy() {
			return createdBy;
		}

		public void setCreatedBy(String createdBy) {
			this.createdBy = createdBy;
		}

		public Date getCreatedTime() {
			return createdTime;
		}

		public void setCreatedTime(Date createdTime) {
			this.createdTime = createdTime;
		}

		public String getUpdatedBy() {
			return updatedBy;
		}

		public void setUpdatedBy(String updatedBy) {
			this.updatedBy = updatedBy;
		}

		public Date getUpdatedTime() {
			return updatedTime;
		}

		public void setUpdatedTime(Date updatedTime) {
			this.updatedTime = updatedTime;
		}

		public String getEmail() {
			if (!CurrentAccount.isAdmin()) {
				return "";
			} else {
				return Masker.maskEmail(email);
			}
		}

		public String getPhoneNumber() {
			if (!CurrentAccount.isAdmin()) {
				return "";
			} else {
				return Masker.maskPhoneNumber(phoneNumber);
			}
		}

		public void setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public Boolean getSuperAdminRole() {
			return superAdminRole;
		}

		public void setSuperAdminRole(Boolean superAdminRole) {
			this.superAdminRole = superAdminRole;
		}

		public Boolean getAdminRole() {
			return adminRole;
		}

		public void setAdminRole(Boolean adminRole) {
			this.adminRole = adminRole;
		}

		public AuditStatus getAuditStatus() {
			return auditStatus;
		}

		public void setAuditStatus(AuditStatus auditStatus) {
			this.auditStatus = auditStatus;
		}

		public String getAuditComment() {
			return auditComment;
		}

		public void setAuditComment(String auditComment) {
			this.auditComment = auditComment;
		}

		public Boolean getEnable() {
			return enable;
		}

		public void setEnable(Boolean enable) {
			this.enable = enable;
		}
	}
}
