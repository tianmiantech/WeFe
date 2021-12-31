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

package com.welab.wefe.serving.service.api.pir;

import java.io.IOException;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.mpc.pir.PrivateInformationRetrievalApiName;
import com.welab.wefe.mpc.pir.request.QueryRandomLegalRequest;
import com.welab.wefe.mpc.pir.server.service.HauckRandomLegalService;
import com.welab.wefe.serving.service.api.pir.PrivateInformationRetrievalForRandomApi.Output;
import com.welab.wefe.serving.service.utils.ModelMapper;

@Api(path = PrivateInformationRetrievalApiName.RANDOM_LEGAL, name = "random_legal", login = false)
public class PrivateInformationRetrievalForRandomLegalApi extends
		AbstractApi<PrivateInformationRetrievalForRandomLegalApi.Input, PrivateInformationRetrievalForRandomApi.Output> {

	@Override
	protected ApiResult<PrivateInformationRetrievalForRandomApi.Output> handle(
			PrivateInformationRetrievalForRandomLegalApi.Input input) throws StatusCodeWithException, IOException {
		HauckRandomLegalService service = new HauckRandomLegalService();
		QueryRandomLegalRequest request = ModelMapper.map(input, QueryRandomLegalRequest.class);

		return success(Output.success(service.handle(request)));
	}

	public static class Input extends AbstractApiInput {
		private String uuid;
		private Boolean sLegal;
		private int attemptCount;
		private String r;

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		public Boolean getsLegal() {
			return sLegal;
		}

		public void setsLegal(Boolean sLegal) {
			this.sLegal = sLegal;
		}

		public int getAttemptCount() {
			return attemptCount;
		}

		public void setAttemptCount(int attemptCount) {
			this.attemptCount = attemptCount;
		}

		public String getR() {
			return r;
		}

		public void setR(String r) {
			this.r = r;
		}

	}

}
