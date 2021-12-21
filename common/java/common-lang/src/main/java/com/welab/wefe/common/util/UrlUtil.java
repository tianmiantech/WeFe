/*
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

package com.welab.wefe.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zane
 */
public class UrlUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(StringUtil.class);
    private static final Pattern PATTERN_MATCH_QUERY_STRING = Pattern.compile("(?<name>[^?&]+)=(?<value>[^?&]+)");

    /**
     * @param url Get Specifies the URL of the request
     * @param key Get Specifies the key of the request parameter
     * @return eg:http://data.stats.gov.cn/easyquery.htm?id=A01&dbcode=hgyd&wdcode=zb&m=getTree
     */
    public static String getValue(String url, String key) {
        String[] leftRight = url.split("\\?");

        String[] keyValues = null;
        if (leftRight.length == 1) {
            keyValues = leftRight[0].split("&");
        } else if (leftRight.length == 2) {
            keyValues = leftRight[1].split("&");
        }
        if (keyValues != null) {
            for (String keyValue : keyValues) {
                String[] split = keyValue.split("=");
                if (split[0].equals(key) && split.length == 2) {
                    return split[1];
                }
            }
        }
        return "";
    }

    /**
     * @param url   The original URL
     * @param key   Get Specifies the key of the request parameter
     * @param value The key corresponding to the value
     * @return Adds or modifies the URL of the request parameter
     */
    public static String setValue(String url, String key, String value) {
        if (StringUtils.isNotEmpty(getValue(url, key))) {
            String src = key + "=" + getValue(url, key);
            String des = key + "=" + value;
            return url.replace(src, des);
        } else {
            if (url.contains("=")) {
                return url + "&" + key + "=" + value;
            } else {
                return url + "?" + key + "=" + value;
            }
        }
    }

    /**
     * Add the GET parameter to the URL
     *
     * @return Returns the assembled URL
     */
    public static String appendQueryParameter(String url, String key, String value) {
        if (url.contains("?")) {
            if (!url.endsWith("&")) {
                url += "&";
            }
        } else {
            url += "?";
        }

        url += key + "=" + encode(value);

        return url;
    }

    /**
     * Splice multiple get parameters for URL
     */
    public static String appendQueryParameters(String url, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }

        StringBuilder result = new StringBuilder(url);

        if (url.contains("?")) {
            if (!url.endsWith("&")) {
                result.append("&");
            }
        } else {
            result.append("?");
        }

        return result.append(params2QueryString(params)).toString();
    }


    /**
     * Concatenate multiple GET parameters for the URL
     */
    public static String appendQueryParameters(String url, Map<String, Object> params, boolean needEncode) {
        if (params == null || params.isEmpty()) {
            return url;
        }

        StringBuilder result = new StringBuilder(url);

        if (url.contains("?")) {
            if (!url.endsWith("&")) {
                result.append("&");
            }
        } else {
            result.append("?");
        }

        return result.append(params2QueryString(params, needEncode)).toString();
    }

    /**
     * Concatenate the parameter list into a Query String
     */
    public static String params2QueryString(Map<String, Object> params, boolean needEncode) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
            result.append("&");
            result.append(entry.getKey());
            result.append("=");

            if (needEncode) {
                result.append(encode(value));
            } else {
                result.append(value);
            }
        }
        result.deleteCharAt(0);
        return result.toString();
    }

    /**
     * Splice the parameter list into
     *
     * @author Zane
     */
    public static String params2QueryString(Map<String, Object> params) {
        return params2QueryString(params, true);
    }

    /**
     * Encode URL parameters
     */
    public static String encode(String str, String charsetName) {
        try {
            return URLEncoder.encode(str, charsetName);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }


    /**
     * heavy load
     */
    public static Map<String, String> queryString2Map(String queryString) {
        return queryString2Map(queryString, false);
    }

    /**
     * Query string resolves to map
     *
     * @param queryString get Parameter string
     * @param needDecode  Need de coding
     * @author Zane
     */
    public static Map<String, String> queryString2Map(String queryString, boolean needDecode) {
        Map<String, String> result = new HashMap<>(16);
        if (StringUtil.isEmpty(queryString)) {
            return result;
        }

        Matcher matcher = PATTERN_MATCH_QUERY_STRING.matcher(queryString);
        while (matcher.find()) {
            String name = matcher.group("name");
            String value = matcher.group("value");

            if (needDecode) {
                value = decode(value);
            }

            result.put(name, value);
        }
        return result;
    }

    /**
     * Use utf8 encoding to encode URL parameters
     */
    public static String encode(String str) {
        return encode(str, StandardCharsets.UTF_8.name());
    }

    /**
     * Decoding URL parameters
     */
    public static String decode(String str, String charsetName) {
        try {
            return URLDecoder.decode(str, charsetName);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * Decode URL parameters using UTF8 encoding
     */
    public static String decode(String str) {
        return decode(str, StandardCharsets.UTF_8.name());
    }

    public static URI createUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
