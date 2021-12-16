package com.welab.wefe.serving.service.api.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ServiceService;

@Api(path = "api", name = "api service", forward = true)
public class RouteApi extends AbstractApi<RouteApi.Input, RouteApi.Output> {

	@Autowired
	private ServiceService service;

	@Override
	protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
		String uri = input.request.getRequestURI();
		String serviceUrl = uri.substring(uri.lastIndexOf("api/") + 4);
		return success(service.executeService(serviceUrl, input));
	}

	public static class Input extends AbstractApiInput {

	}

	public static class Output extends AbstractApiOutput {
		private int code;
		private String message;
		private JObject result;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public JObject getResult() {
			return result;
		}

		public void setResult(JObject result) {
			this.result = result;
		}

	}

}
