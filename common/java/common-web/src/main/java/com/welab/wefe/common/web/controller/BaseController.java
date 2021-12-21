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

package com.welab.wefe.common.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.UrlUtil;
import com.welab.wefe.common.web.ApiExecutor;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.catalina.connector.RequestFacade;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Zane
 */
@RestController
public class BaseController {


    @GetMapping(value = "/download")
    public ResponseEntity<FileSystemResource> download(HttpServletRequest httpServletRequest) {
        File file = new File("");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + file.getName());
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", file.lastModified() + "");
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new FileSystemResource(file));

    }

    @GetMapping(value = "**", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> get(HttpServletRequest httpServletRequest) throws IOException {
        // If static resources are requested, return the file.
        if (httpServletRequest.getServletPath().startsWith("/static/")) {

            ClassPathResource resource = new ClassPathResource(httpServletRequest.getServletPath());
            byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());

            return ResponseEntity
                    .ok()
                    .contentLength(data.length)
                    .contentType(MediaType.IMAGE_PNG)
                    .body(data);
        }


        return post(httpServletRequest);
    }

    @PostMapping(value = "**", produces = "application/json; charset=UTF-8")
    public ResponseEntity<ApiResult<?>> post(HttpServletRequest httpServletRequest) throws IOException {
        long start = System.currentTimeMillis();

        // Retrieve the token from the header
        String token = httpServletRequest.getHeader("token");
        if (StringUtil.isEmpty(token)) {
            token = httpServletRequest.getParameter("token");
        }
        CurrentAccount.token(token);

        JSONObject params;
        MultiValueMap<String, MultipartFile> files = null;

        // Ordinary request
        if (httpServletRequest instanceof RequestFacade) {
            RequestFacade request = (RequestFacade) httpServletRequest;
            JSONObject bodyParams = getBodyParamsFromHttpRequest(request);
            params = buildRequestParams(request.getParameterMap(), bodyParams);
        }

        // A request to include a file
        else if (httpServletRequest instanceof StandardMultipartHttpServletRequest) {
            StandardMultipartHttpServletRequest request = (StandardMultipartHttpServletRequest) httpServletRequest;
            params = buildRequestParams(request.getParameterMap(), null);
            files = request.getMultiFileMap();
        }

        // Other requests are not supported
        else {
            throw new UnsupportedOperationException("Unsupported request types：" + httpServletRequest.getClass().getSimpleName());
        }

        String path = httpServletRequest.getPathInfo() == null ? httpServletRequest.getServletPath() : httpServletRequest.getPathInfo();
        // Multi-level paths under/Tools are supported. Eg: the tools/a/b/c
        path = StringUtil.trim(path, '/');

        ApiResult<?> response = ApiExecutor.execute(httpServletRequest, start, path, params, files);
        response.spend = System.currentTimeMillis() - start;

        if (response.data instanceof ResponseEntity) {
            return (ResponseEntity) response.data;
        } else {
            return ResponseEntity.status(response.httpCode).body(response);
        }
    }


    /**
     * Merge get arguments with POST
     * In case of merge conflicts, the POST parameter prevails.
     */
    private JSONObject buildRequestParams(Map<String, String[]> queryStringParams, JSONObject bodyParams) {
        TreeMap<String, Object> getParams = new TreeMap<>();

        if (queryStringParams != null && !queryStringParams.isEmpty()) {
            queryStringParams.forEach((key, values) -> {
                for (int i = 0; i < values.length; i++) {
                    values[i] = UrlUtil.decode(values[i]);
                }

                if (values.length == 1) {
                    getParams.put(key, "null" .equalsIgnoreCase(values[0]) ? null : values[0]);
                } else {
                    getParams.put(key, values);
                }
            });
        }

        JSONObject result = new JSONObject(getParams);

        // Fill the body parameter
        if (bodyParams != null) {
            result.putAll(bodyParams);
        }

        return result;
    }

    /**
     * Gets the list of parameters from the request object
     */
    private JSONObject getBodyParamsFromHttpRequest(RequestFacade request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return JSON.parseObject(body.toString());
    }


}
