package com.welab.wefe.serving.service.api.pir;

import java.io.IOException;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.mpc.pir.PrivateInformationRetrievalApiName;
import com.welab.wefe.mpc.pir.request.QueryResultsRequest;
import com.welab.wefe.mpc.pir.request.QueryResultsResponse;
import com.welab.wefe.mpc.pir.server.service.HuackResultsService;
import com.welab.wefe.serving.service.utils.ModelMapper;

@Api(path = PrivateInformationRetrievalApiName.RESULTS, name = "results")
public class PrivateInformationRetrievalForResultsApi
		extends AbstractApi<PrivateInformationRetrievalForResultsApi.Input, QueryResultsResponse> {

	@Override
	protected ApiResult<QueryResultsResponse> handle(Input input) throws StatusCodeWithException, IOException {
		HuackResultsService service = new HuackResultsService();
		QueryResultsRequest request = ModelMapper.map(input, QueryResultsRequest.class);
		return success(service.handle(request));
	}

	public static class Input extends AbstractApiInput {

		private String uuid;

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
	}

}
