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

package com.welab.wefe.serving.service.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.util.JObject;

public class ServiceUtil {

	public static byte[] fileToBytes(File file) throws IOException {
		byte[] buffer = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;

		try {
			fis = new FileInputStream(file);
			bos = new ByteArrayOutputStream();

			byte[] b = new byte[1024];

			int n;

			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}

			buffer = bos.toByteArray();
		} catch (IOException ex) {
			throw ex;

		} finally {
			try {
				if (null != bos) {
					bos.close();
				}
			} catch (IOException ex) {
			} finally {
				try {
					if (null != fis) {
						fis.close();
					}
				} catch (IOException ex) {
				}
			}
		}

		return buffer;
	}

	public static String generateSQL(String params, JSONObject dataSource) {
		String tableName = dataSource.getString("db") + "." + dataSource.getString("table");
		String resultfields = parseReturnFields(dataSource);
		String where = parseWhere(dataSource, JObject.create(params));
		String sql = "SELECT " + resultfields + " FROM " + tableName + " WHERE " + where;
		System.out.println(sql);
		return sql;
	}

	public static String parseReturnFields(JSONObject dataSource) {
		JSONArray returnFields = dataSource.getJSONArray("return_fields");
		if (returnFields.isEmpty()) {
			return "*";
		} else {
			List<String> fields = new ArrayList<>();
			for (int i = 0; i < returnFields.size(); i++) {
				fields.add(returnFields.getJSONObject(i).getString("name"));
			}
			return StringUtils.join(fields, ",");
		}
	}

	private static String parseWhere(JSONObject dataSource, JObject params) {
		JSONArray conditionFields = dataSource.getJSONArray("condition_fields");
		String where = "";
		if (conditionFields.isEmpty()) {
			where = "1=1";
			return where;
		} else {
			int size = conditionFields.size();
			for (int i = 0; i < conditionFields.size(); i++) {
				JSONObject tmp = conditionFields.getJSONObject(i);
				where += (" " + tmp.getString("field_on_table") + "=\""
						+ params.getString(tmp.getString("field_on_param")) + "\" " + " "
						+ (size - 1 == i ? "" : tmp.getString("operator")));
			}
			return where;
		}
	}
}
