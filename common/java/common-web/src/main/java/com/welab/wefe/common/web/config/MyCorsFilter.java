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
package com.welab.wefe.common.web.config;

import com.welab.wefe.common.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author zane
 * @date 2022/5/6
 */
public class MyCorsFilter extends OncePerRequestFilter {
    private final CorsConfiguration corsConfiguration;
    private final CorsConfigurationSource configSource;
    private final CorsProcessor processor = new DefaultCorsProcessor();
    private final List<String> allowedOrigins;
    private final String allowedOriginString;
    private boolean needCheckCors;

    /**
     * Constructor accepting a {@link CorsConfigurationSource} used by the filter
     * to find the {@link CorsConfiguration} to use for each incoming request.
     *
     * @see UrlBasedCorsConfigurationSource
     */
    public MyCorsFilter(CorsConfiguration corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        this.configSource = source;
        this.allowedOrigins = corsConfiguration.getAllowedOrigins();
        this.allowedOriginString = StringUtil.joinByComma(allowedOrigins);
        this.needCheckCors = CollectionUtils.isNotEmpty(allowedOrigins) && !allowedOrigins.contains(CorsConfiguration.ALL);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equals(request.getMethod().toUpperCase())) {
            setCorsInfoIntoResponseHeader(response);
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        if (CorsUtils.isCorsRequest(request)) {
            if (needCheckCors) {
                if (blockRequest(request, response)) {
                    setCorsInfoIntoResponseHeader(response);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "request origin not allowed null");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setCorsInfoIntoResponseHeader(HttpServletResponse response) {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowedOriginString);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, StringUtil.joinByComma(corsConfiguration.getAllowedHeaders()));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, StringUtil.joinByComma(corsConfiguration.getAllowedMethods()));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, corsConfiguration.getMaxAge() + "");
    }

    private boolean blockRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        // 禁止 origin 为空
//        if (request.getHeader(HttpHeaders.ORIGIN) == null) {
//            return true;
//        }

        CorsConfiguration corsConfiguration = this.configSource.getCorsConfiguration(request);
        if (corsConfiguration != null) {
            boolean isValid = this.processor.processRequest(corsConfiguration, request, response);
            if (!isValid || CorsUtils.isPreFlightRequest(request)) {
                return true;
            }
        }

        return false;
    }

}
