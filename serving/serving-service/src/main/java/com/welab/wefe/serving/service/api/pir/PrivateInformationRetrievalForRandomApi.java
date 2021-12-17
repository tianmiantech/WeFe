package com.welab.wefe.serving.service.api.pir;

import java.io.IOException;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.mpc.pir.PrivateInformationRetrievalApiName;
import com.welab.wefe.mpc.pir.request.QueryRandomRequest;
import com.welab.wefe.mpc.pir.request.QueryRandomResponse;
import com.welab.wefe.mpc.pir.server.service.HauckRandomService;
import com.welab.wefe.serving.service.utils.ModelMapper;

@Api(path = PrivateInformationRetrievalApiName.RANDOM, name = "random")
public class PrivateInformationRetrievalForRandomApi
		extends AbstractApi<PrivateInformationRetrievalForRandomApi.Input, QueryRandomResponse> {

	@Override
	protected ApiResult<QueryRandomResponse> handle(Input input) throws StatusCodeWithException, IOException {
		HauckRandomService service = new HauckRandomService();
		QueryRandomRequest request = ModelMapper.map(input, QueryRandomRequest.class);
		return success(service.handle(request));
	}

	public static class Input extends AbstractApiInput {

		private String uuid;
		private int attemptCount;

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		public int getAttemptCount() {
			return attemptCount;
		}

		public void setAttemptCount(int attemptCount) {
			this.attemptCount = attemptCount;
		}
	}

}
