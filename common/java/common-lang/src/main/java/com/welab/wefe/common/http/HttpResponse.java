/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.fastjson.LoggerSerializeConfig;
import com.welab.wefe.common.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zane
 */
public class HttpResponse implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(HttpResponse.class);
    public static final int CODE_ERROR = -1;
    /**
     * Elapsed time (in milliseconds) from the beginning of sending the request to the end of the response
     */
    private long spend;

    /**
     * http code
     */
    private int code;

    /**
     * Exception when request failed
     */
    private Exception error;
    private String message;
    private HttpRequest request;
    private HttpEntity bodyEntity;
    /**
     * 禁止直接访问这个字段，请使用 getBodyAsString()
     */
    private String bodyAsString;
    /**
     * 禁止直接访问这个字段，请使用 getBodyAsBytes()
     */
    private byte[] bodyBytes;
    private Map<String, String> headers = new HashMap<>();
    private String encoding;
    private String contentType;
    private long contentLength;
    private String url;
    private CloseableHttpResponse rawResponse;
    private static final Pattern PATTERN_MATCH_CHARSET = Pattern.compile("(?<=charset=)[a-z0-9\\-]+", Pattern.CASE_INSENSITIVE);

    private HttpResponse(CloseableHttpResponse rawResponse) {
        this.rawResponse = rawResponse;
        if (rawResponse != null) {
            contentLength = rawResponse.getEntity().getContentLength();
            bodyEntity = rawResponse.getEntity();
        }
    }

    public static HttpResponse create(CloseableHttpResponse rawResponse) {
        return create(null, rawResponse, 0);
    }

    static HttpResponse create(HttpRequest httpRequest, CloseableHttpResponse rawResponse, long spendTime) {
        HttpResponse httpResponse = new HttpResponse(rawResponse);
        httpResponse.setRequest(httpRequest);
        httpResponse.setSpend(spendTime);
        return httpResponse;
    }

    HttpResponse message(String message) {
        this.message = message;
        return this;
    }

    HttpResponse statusCode(int statusCode) {
        this.code = statusCode;
        return this;
    }

    HttpResponse url(String url) {
        this.url = url;
        return this;
    }

    HttpResponse header(org.apache.http.HttpResponse response) {
        for (Header item : response.getAllHeaders()) {
            headers.put(item.getName(), item.getValue());
            // Get the encoding from the content type declaration of the header
            if (item.getName().equalsIgnoreCase(HeaderKey.CONTENT_TYPE)) {
                this.contentType = StringUtil.substringBefore(item.getValue(), ";");
                Matcher matcher = PATTERN_MATCH_CHARSET.matcher(item.getValue());
                if (matcher.find()) {
                    this.encoding = matcher.group();
                }
            }
        }
        return this;
    }

    HttpResponse error(Exception error) {
        this.code = CODE_ERROR;
        this.error = error;
        this.message = error.getLocalizedMessage();
        return this;
    }

    /**
     * Read the body as a string
     */
    public String getBodyAsString() {

        if (bodyAsString != null) {
            return bodyAsString;
        }

        if (StringUtils.isEmpty(this.encoding)) {

            try {
                bodyAsString = new String(getBodyAsBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            try {
                bodyAsString = new String(getBodyAsBytes(), this.encoding);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }

        return bodyAsString;
    }

    public String getBodyAsBase64() {
        return Base64.encodeBase64String(getBodyAsBytes());
    }

    public byte[] getBodyAsBytes() {
        if (bodyBytes != null) {
            return bodyBytes;
        }

        if (bodyEntity == null) {
            return null;
        }

        try {
            bodyBytes = EntityUtils.toByteArray(bodyEntity);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            close();
        }

        return bodyBytes;
    }

    public JSONObject getBodyAsJson() {
        if (success()) {
            return JSONObject.parseObject(getBodyAsString());
        }
        return new JSONObject();
    }

    public File getBodyAsFile(String filePath) {
        if (!success()) {
            return null;
        }
        if (bodyEntity == null) {
            return null;
        }
        try {
            InputStream is = bodyEntity.getContent();


            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            file.getParentFile().mkdirs();
            FileOutputStream fileout = new FileOutputStream(file);
            /**
             * 根据实际运行效果 设置缓冲区大小
             */
            byte[] buffer = new byte[10 * 1024];
            int ch = 0;
            while ((ch = is.read(buffer)) != -1) {
                fileout.write(buffer, 0, ch);
            }
            is.close();
            fileout.flush();
            fileout.close();

            close();
            return file;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public String getContentType() {
        return contentType;
    }

    /**
     * Is the response successful
     */
    public boolean success() {
        return error == null;
    }

    /**
     * Whether the current entity is a cache entity (CACHE entities are generally constructed through create())
     */
    public boolean isCacheEntity() {
        return request == null;
    }

    /**
     * Outputs the current response object to the log
     */
    public void log() {
        String content = null;

        if (contentType != null && contentType.toLowerCase().contains("stream")) {
            content = "Binary data, length:" + contentLength;
        } else if (contentType != null && contentType.toLowerCase().contains("json")) {
            try {
                JSONObject json = getBodyAsJson();
                content = JSON.toJSONString(json.getInnerMap(), LoggerSerializeConfig.instance());
            } catch (Exception e) {
            }
        } else {
            content = getBodyAsString();
        }

        if (content != null) {
            content = content.replace(System.lineSeparator(), "");
        }

        if (success()) {
            LOG.info("http success({} ms) for {} ({} : {}), Body : {}", spend, request.getUrl(), code, message, content);
        } else {
            LOG.error("http fail({} ms) for {} ({} : {}), Body : {}", spend, request.getUrl(), code, message, content, error);
        }
    }

    public long getSpend() {
        return spend;
    }

    public int getCode() {
        return code;
    }

    public Exception getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public void setSpend(long spend) {
        this.spend = spend;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void close() {
        try {
            if (rawResponse != null) {
                rawResponse.close();
            }
        } catch (Exception e) {
            rawResponse = null;
        } finally {
            rawResponse = null;
        }

    }

    public static final class HeaderKey {
        public static final String CONTENT_TYPE = "Content-Type";
    }
}
