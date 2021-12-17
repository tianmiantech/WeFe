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

	public static String generateSQL(String params, JSONArray dataSourceArr, int index) {
		String tableName = parseTableName(dataSourceArr, index);
		String resultfields = parseReturnFields(dataSourceArr, index);
		String where = parseWhere(dataSourceArr, JObject.create(params), index);
		String sql = "SELECT " + resultfields + " FROM " + tableName + " WHERE " + where;
		System.out.println(sql);
		return sql;
	}

	private static String parseTableName(JSONArray dataSource, int index) {
		JSONObject json = dataSource.getJSONObject(index);
		return json.getString("db") + "." + json.getString("table");
	}

	public static String parseReturnFields(JSONArray dataSource, int index) {
		JSONObject json = dataSource.getJSONObject(index);
		JSONArray returnFields = json.getJSONArray("return_fields");
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

	private static String parseWhere(JSONArray dataSourceArr, JObject params, int index) {
		JSONArray conditionFields = dataSourceArr.getJSONObject(index).getJSONArray("condition_fields");
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
