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

package com.welab.wefe.common.http;

import com.alibaba.fastjson.JSON;
import com.welab.wefe.common.util.UrlUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The new HTTP Client wheel.
 * <p>
 * The main purpose of reinventing the wheel is to make unified statistics and monitoring for the calls of third-party interfaces.
 * <p>
 * If you find that the wheel is not enough and need to add features, please contact Zane.
 *
 * @author Zane
 */
public class HttpRequest {

    /**
     * 200 milliseconds is the step value
     */
    public static final long RETRY_DELAY_STEP = 300;

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequest.class);
    private static PoolingHttpClientConnectionManager sConnectionManager = null;
    /**
     * The default User-Agent
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36";

    public static HttpRequest create(String url) {
        return new HttpRequest(url);
    }

    private String url;
    private String contentType;
    private String encoding = StandardCharsets.UTF_8.name();
    private Map<String, String> headers;
    private String body;
    private BasicCookieStore cookieStore = new BasicCookieStore();

    /**
     * You can pass either get or POST key-value parameters
     */
    private Map<String, Object> paramMap = new HashMap<>();

    private int retryCount = 1;
    private long retryDelay = 0;
    private int connectTimeout = 10 * 1000;
    private int socketTimeout = 10 * 1000;
    private boolean needPrintLog = true;
    private Function<HttpResponse, Boolean> validator;

    private HttpRequest(String url) {
        this.url = url;
        headers = new HashMap<>();
        headers.put("USER-AGENT", USER_AGENT);
    }

    /**
     * Set the cookie storage object
     */
    public HttpRequest setCookieStore(BasicCookieStore cookieStore) {
        this.cookieStore = cookieStore;
        return this;
    }

    /**
     * Gets the cookie storage object
     */
    public BasicCookieStore getCookieStore() {
        return this.cookieStore;
    }

    /**
     * Set up the body
     */
    public HttpRequest setBody(String body) {
        this.body = body;
        return this;
    }

    /**
     * Add GET or POST request parameters
     */
    public HttpRequest appendParameter(String key, Object value) {
        this.paramMap.put(key, value);
        return this;
    }

    /**
     * Add multiple GET or POST request parameters
     */
    public HttpRequest appendParameters(Map<String, Object> postParam) {
        this.paramMap.putAll(postParam);
        return this;
    }

    /**
     * Set Content Type with no default value.
     */
    public HttpRequest setContentType(String contentType) {
        this.contentType = contentType;
        this.putHeader("Content-Type", contentType);
        return this;
    }

    /**
     * Set both connectTimeout and socketTimeout in milliseconds. Default is 10 seconds.
     */
    public HttpRequest setTimeout(int timeout) {
        this.connectTimeout = timeout;
        this.socketTimeout = timeout;
        return this;
    }

    /**
     * Set the timeout period for the HTTP connection in milliseconds. The default value is 10 seconds.
     */
    public HttpRequest setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Set the timeout for the HTTP read response in milliseconds. The default is 10 seconds.
     */
    public HttpRequest setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }


    /**
     * Number of retries when a request fails. The default value is not retries.
     */
    public HttpRequest setRetryCount(int retryCount) {
        this.retryCount = retryCount >= 0 ? retryCount : this.retryCount;
        return this;
    }

    /**
     * Retry delay in milliseconds, default 0
     */
    public HttpRequest setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay >= 0 ? retryDelay : this.retryDelay;
        return this;
    }

    /**
     * Add the header
     */
    public HttpRequest putHeader(String key, String value) {
        // header name Use all caps
        key = key.toUpperCase();
        headers.put(key, value);
        return this;
    }

    /**
     * Adding multiple Headers
     */
    public HttpRequest putHeaders(Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            this.headers.put(entry.getKey().toUpperCase(), entry.getValue());
        }
        return this;
    }


    /**
     * The body is encoded in utF-8 when the request is sent.
     */
    public HttpRequest setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * Setting the validator
     */
    public HttpRequest setValidator(Function<HttpResponse, Boolean> validator) {
        this.validator = validator;
        return this;
    }

    public HttpRequest closeLog() {
        needPrintLog = false;
        return this;
    }

    /**
     * Send the request with the method of GET
     */
    public HttpResponse get() {
        return sendRequest(HttpMethod.GET);
    }

    /**
     * Send the request with post's Method
     */
    public HttpResponse post() {
        return sendRequest(HttpMethod.POST);
    }

    /**
     * Set content-Type to Application /json and send a POST request.
     */
    public HttpResponse postJson() {
        setContentType(HttpContentType.JSON);
        return sendRequest(HttpMethod.POST);
    }

    public String getUrl() {
        return url;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public String getBody() {
        return body;
    }

    public HttpResponse sendRequest(HttpMethod method) {
        HttpResponse result = null;
        // retry
        for (int remainingCount = this.retryCount; remainingCount >= 0; remainingCount--) {

            result = doHttp(method);
            if (HttpResponse.CODE_ERROR != result.getCode()) {
                break;
            }

            if (retryDelay > 0) {
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException e) {
                    LOG.warn("thread sleep error , {}", e.getMessage());
                }
            }
        }

        if (result != null && needPrintLog) {
            result.log();
        }

        return result;
    }

    private HttpResponse doHttp(HttpMethod method) {
        HttpResponse response;

        long startTime = System.currentTimeMillis();
        try {
            MyRedirectStrategy myRedirectStrategy = new MyRedirectStrategy();
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(
                            RequestConfig
                                    .custom()
                                    .setSocketTimeout(socketTimeout)
                                    .setConnectTimeout(connectTimeout)

                                    .build()
                    )
                    .setRedirectStrategy(myRedirectStrategy)
                    .setDefaultCookieStore(cookieStore)
                    .setConnectionManager(sConnectionManager)
                    // Keep it for reference 2018.10.12
//                    .setSSLContext(SSLContextBuilder.create()
//                            .loadTrustMaterial(null, (chain, authType) -> true)
//                            .build())
//                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            HttpUriRequest httpUriRequest;
            switch (method) {
                case POST:
                    httpUriRequest = buildPostHttpUriRequest();
                    break;

                case GET:
                default:
                    paramMap.forEach((key, value) -> url = UrlUtil.appendQueryParameter(url, key, String.valueOf(value)));
                    httpUriRequest = new HttpGet(url);
                    break;

            }

            // Supplement the header and do not overwrite it.
            headers.forEach((k, v) -> {
                if (httpUriRequest.getFirstHeader(k) == null) {
                    httpUriRequest.setHeader(k, v);
                }
            });

            StringBuilder sb = new StringBuilder("[");
            paramMap.forEach((key, value) -> sb.append(key).append(" = ").append(value).append(", "));
            sb.append("]");

            LOG.info("Request : URL = {}, headers = {}, encoding = {}, Content-Type = {}, paramMap = {}, body = {}", url, headers, encoding, contentType, sb.toString(), body);
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpUriRequest)) {
                response = HttpResponse.create(this, System.currentTimeMillis() - startTime)
                        .message(httpResponse.getStatusLine().getReasonPhrase())
                        .statusCode(httpResponse.getStatusLine().getStatusCode())
                        .header(httpResponse)
                        .url(myRedirectStrategy.getCurrentLocation())
                        .body(EntityUtils.toByteArray(httpResponse.getEntity()));

                if (validator != null && !validator.apply(response)) {
                    // Verification failed
                    response.error(new IllegalStateException("validate fail"));
                }
            } catch (Exception e) {
                response = HttpResponse.create(this, System.currentTimeMillis() - startTime).error(e);
            }

        } catch (Exception e) {
            response = HttpResponse.create(this, System.currentTimeMillis() - startTime).error(e);
        }

        return response;
    }

    private HttpUriRequest buildPostHttpUriRequest() throws UnsupportedEncodingException {
        HttpPost httpUriRequest = new HttpPost(url);


        if (HttpContentType.MULTIPART.equals(contentType)) {

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            if (CollectionUtils.isNotEmpty(paramMap.keySet())) {

                paramMap.forEach((key, value) -> {

                    if (value instanceof byte[]) {
                        builder.addBinaryBody(key, (byte[]) value);
                    } else if (value instanceof InputStream) {
                        builder.addBinaryBody(key, (InputStream) value);
                    } else if (value instanceof File) {
                        builder.addBinaryBody(key, (File) value);
                    } else {
                        builder.addTextBody(key,
                                String.valueOf(value),
                                ContentType
                                        .create(ContentType.DEFAULT_TEXT.getMimeType(),
                                                Charset.forName(encoding))
                        );
                    }

                });
            }

            HttpEntity entity = builder.build();
            httpUriRequest.setEntity(entity);
            httpUriRequest.setHeader(entity.getContentType());

        } else if (CollectionUtils.isNotEmpty(paramMap.keySet()) && !HttpContentType.JSON.equals(contentType)) {

            // Key value pair parameter
            List<NameValuePair> pairList = new ArrayList<>();
            paramMap.forEach((key, value) -> pairList.add(new BasicNameValuePair(key, String.valueOf(value))));
            httpUriRequest.setEntity(new UrlEncodedFormEntity(pairList, encoding));

        } else {
            // If body is empty, it will be converted from map to JSON string and assigned to body
            if (StringUtils.isEmpty(body) && HttpContentType.JSON.equals(contentType)) {
                body = JSON.toJSONString(paramMap);
            }

            // Normal string parameter
            StringEntity entity = new StringEntity(body, encoding);
            if (StringUtils.isNotEmpty(body)) {

                if (contentType == null) {
                    throw new RuntimeException("set content-type for post request.");
                }
                entity.setContentType(this.contentType);

            }
            httpUriRequest.setEntity(entity);
        }

        return httpUriRequest;
    }

    static {
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();

            sConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            sConnectionManager.setMaxTotal(300);
            sConnectionManager.setDefaultMaxPerRoute(20);

        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOG.error("create HttpClientConnectionManager error", e);
        }
    }


}
