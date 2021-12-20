package com.welab.wefe.serving.service.api.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ServiceService;

@Api(path = "service/online", name = "online service")
public class OnlineApi extends AbstractApi<OnlineApi.Input, OnlineApi.Output> {

	@Autowired
	ServiceService serviceService;

	@Override
	protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
		serviceService.onlineService(input.getId());
		return success();
	}

	public static class Input extends AbstractApiInput {

		@Check(name = "主键id")
		private String id;

		// region getter/setter

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
		// endregion
	}

	public static class Output extends AbstractApiOutput {

	}

}
