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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.util.JObject;

public class ServiceUtil {
    
    protected static final Logger LOG = LoggerFactory.getLogger(ServiceUtil.class);
    
	public static String getIpAddr(HttpServletRequest request) {
		String ipAddress = request.getHeader("x-forwarded-for");
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
				// 根据网卡取本机配置的IP
				InetAddress inet = null;
				try {
					inet = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				ipAddress = inet.getHostAddress();
			}
		}
		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length() = 15
			if (ipAddress.indexOf(",") > 0) {
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
			}
		}
		return ipAddress;
	}

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

	public static String generateSQL(String params, JSONObject dataSource, String dbName) {
		String tableName = dbName + "." + dataSource.getString("table");
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
                where += (" " + tmp.getString("field_on_table")
                        + (StringUtils.isNotBlank(tmp.getString("condition"))
                                ? (tmp.getString("condition").equalsIgnoreCase("gt") ? ">"
                                        : (tmp.getString("condition").equalsIgnoreCase("lt") ? "<" : "="))
                                : "=")
                        + "\"" + params.getString(tmp.getString("field_on_param")) + "\" " + " "
                        + (size - 1 == i ? "" : tmp.getString("operator")));
            }
            return where;
        }
    }
	
	/**
	 * 前面保留 index 位明文，后面保留 end 位明文,如：[身份证号] 110****58，前面保留3位明文，后面保留2位明文
	 */
	public static String around(String str, int index, int end) {
		if (StringUtils.isBlank(str)) {
			return "";
		}
		return StringUtils.left(str, index).concat(StringUtils
				.removeStart(StringUtils.leftPad(StringUtils.right(str, end), StringUtils.length(str), "*"), "***"));
	}
}
