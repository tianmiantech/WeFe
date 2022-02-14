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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.enums.DatabaseType;
import com.welab.wefe.serving.service.service.DataSourceService;

/**
 * @author Jacky.jiang
 */
@Api(path = "data_source/query_tables", name = "查询数据源的所有表")
public class QueryTablesApi extends AbstractApi<QueryTablesApi.Input, QueryTablesApi.Output> {

	@Autowired
	DataSourceService dataSourceService;

	@Override
	protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
		return success(dataSourceService.queryTables(input.getId()));
	}

	public static class Input extends AbstractApiInput{
		@Check(name = "数据源名称")
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

	}

	public static class Output extends AbstractApiOutput {

		private String databaseName;

		private DatabaseType databaseType;

		private List<String> tables;

		public String getDatabaseName() {
			return databaseName;
		}

		public void setDatabaseName(String databaseName) {
			this.databaseName = databaseName;
		}

		public List<String> getTables() {
			return tables;
		}

		public void setTables(List<String> tables) {
			this.tables = tables;
		}

		public DatabaseType getDatabaseType() {
			return databaseType;
		}

		public void setDatabaseType(DatabaseType databaseType) {
			this.databaseType = databaseType;
		}

	}

	public static class TableOutput {
		private String name;
		private List<FieldOutput> fields;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<FieldOutput> getFields() {
			return fields;
		}

		public void setFields(List<FieldOutput> fields) {
			this.fields = fields;
		}
	}

	public static class FieldOutput {
		private String name;
		private String type;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	}
}
