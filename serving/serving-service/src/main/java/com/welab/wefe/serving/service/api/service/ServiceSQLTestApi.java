package com.welab.wefe.serving.service.api.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ServiceService;

@Api(path = "service/sql_test", name = "service sql test")
public class ServiceSQLTestApi extends AbstractApi<ServiceSQLTestApi.Input, ServiceSQLTestApi.Output> {

	@Autowired
	ServiceService serviceService;

	@Override
	protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
		return success(serviceService.sqlTest(input));
	}

	public static class Output extends AbstractApiOutput {
		private JObject result;

		public JObject getResult() {
			return result;
		}

		public void setResult(JObject result) {
			this.result = result;
		}

	}

	public static class Input extends AbstractApiInput {
		@Check(name = "查询参数配置")
		private String queryParams;// json
		@Check(name = "SQL配置")
		private String dataSource;// json
		@Check(name = "查询字段")
		private String conditionFields;// json
		@Check(name = "查询参数")
		private String params;

		public String getQueryParams() {
			return queryParams;
		}

		public void setQueryParams(String queryParams) {
			this.queryParams = queryParams;
		}

		public String getDataSource() {
			return dataSource;
		}

		public void setDataSource(String dataSource) {
			this.dataSource = dataSource;
		}

		public String getConditionFields() {
			return conditionFields;
		}

		public void setConditionFields(String conditionFields) {
			this.conditionFields = conditionFields;
		}

		public String getParams() {
			return params;
		}

		public void setParams(String params) {
			this.params = params;
		}

	}

}
