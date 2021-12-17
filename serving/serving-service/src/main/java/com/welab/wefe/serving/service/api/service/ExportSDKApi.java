package com.welab.wefe.serving.service.api.service;

import java.io.IOException;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;

@Api(path = "service/export_sdk", name = "add service")
public class ExportSDKApi extends AbstractApi<ExportSDKApi.Input, ExportSDKApi.Output> {

	@Override
	protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
		return null;
	}

	public static class Input extends AbstractApiInput {

	}

	public static class Output extends AbstractApiOutput {

	}

}
