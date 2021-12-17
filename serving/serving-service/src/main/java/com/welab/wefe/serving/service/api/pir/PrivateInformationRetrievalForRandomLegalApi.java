package com.welab.wefe.serving.service.api.pir;

import java.io.IOException;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.mpc.pir.PrivateInformationRetrievalApiName;
import com.welab.wefe.mpc.pir.request.QueryRandomLegalRequest;
import com.welab.wefe.mpc.pir.request.QueryRandomLegalResponse;
import com.welab.wefe.mpc.pir.server.service.HauckRandomLegalService;

@Api(path = PrivateInformationRetrievalApiName.RANDOM_LEGAL, name = "random_legal")
public class PrivateInformationRetrievalForRandomLegalApi
		extends AbstractApi<PrivateInformationRetrievalForRandomLegalApi.Input, QueryRandomLegalResponse> {

	@Override
	protected ApiResult<QueryRandomLegalResponse> handle(PrivateInformationRetrievalForRandomLegalApi.Input input)
			throws StatusCodeWithException, IOException {
		HauckRandomLegalService service = new HauckRandomLegalService();
		QueryRandomLegalRequest request = new QueryRandomLegalRequest();
		// TODO input to request
		return success(service.handle(request));
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
