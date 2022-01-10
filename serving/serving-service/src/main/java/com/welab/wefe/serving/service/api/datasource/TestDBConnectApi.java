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

package com.welab.wefe.serving.service.api.datasource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.service.DataSourceService;

/**
 * @author Jacky.jiang
 */
@Api(path = "data_source/test_db_connect", name = "测试数据库是否能正常连接")
public class TestDBConnectApi extends AbstractApi<AddApi.DataSourceAddInput, TestDBConnectApi.Output> {

	@Autowired
	DataSourceService dataSourceService;

	@Override
	protected ApiResult<Output> handle(AddApi.DataSourceAddInput input) throws StatusCodeWithException {
		if (StringUtils.isBlank(input.getId())) {
			return success(dataSourceService.testDBConnect(input.getDatabaseType(), input.getHost(), input.getPort(),
					input.getUserName(), input.getPassword(), input.getDatabaseName()));
		} else {
			DataSourceMySqlModel dataSource = dataSourceService.getDataSourceById(input.getId());
			return success(dataSourceService.testDBConnect(dataSource.getDatabaseType(), dataSource.getHost(),
					dataSource.getPort(), dataSource.getUserName(), dataSource.getPassword(),
					dataSource.getDatabaseName()));
		}
	}

	public static class Output extends AbstractApiOutput {
		private Boolean result;

		public Output() {

		}

		public Output(Boolean result) {
			this.result = result;
		}

		public Boolean getResult() {
			return result;
		}

		public void setResult(Boolean result) {
			this.result = result;
		}
	}
}
