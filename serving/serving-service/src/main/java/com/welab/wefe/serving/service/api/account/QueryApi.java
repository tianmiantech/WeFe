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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiInput;
import com.welab.wefe.serving.service.service.AccountService;

@Api(path = "account/query", name = "account/query", login = false)
public class QueryApi extends AbstractApi<NoneApiInput, List<QueryApi.Output>> {

	@Autowired
	private AccountService accountService;

	@Override
	protected ApiResult<List<Output>> handle(NoneApiInput input) throws Exception {
		return success(accountService.query());
	}

	public static class Output extends AbstractApiOutput {

		private String id;

		private String nickname;

		// region getter/setter

		public String getNickname() {
			return nickname;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		// endregion
	}
}
