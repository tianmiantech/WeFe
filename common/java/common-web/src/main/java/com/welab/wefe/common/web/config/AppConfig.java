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

package com.welab.wefe.common.web.config;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.welab.wefe.common.TimeSpan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.MediaType;
import org.springframework.web.cors.CorsConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zane.luo
 */
@Configuration
public class AppConfig implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private CommonConfig commonConfig;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

    }

    /**
     * Set cross domain policy
     */
    @Bean
    public FilterRegistrationBean corsFilter() {


        CorsConfiguration corsConfiguration = new CorsConfiguration();

        String[] corsAllowedOrigins = commonConfig.getCorsAllowedOrigins();
        if (corsAllowedOrigins != null && corsAllowedOrigins.length > 0) {
            for (String item : corsAllowedOrigins) {
                corsConfiguration.addAllowedOrigin(item.trim());
            }
        } else {
            corsConfiguration.addAllowedOrigin("*");
        }
        corsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        corsConfiguration.addAllowedMethod(CorsConfiguration.ALL);
        corsConfiguration.setMaxAge(TimeSpan.fromMinute(10).toSeconds());
        corsConfiguration.setAllowCredentials(true);

        FilterRegistrationBean bean = new FilterRegistrationBean(new MyCorsFilter(corsConfiguration));
        bean.setOrder(0);
        return bean;
    }

    /**
     * Use fastjson as the API response object instead to serialize the component
     */
    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {

        // Globally set the naming policy for fastjson serialization to underline
        SerializeConfig.getGlobalInstance()
                .propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;

        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();

        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        converter.setSupportedMediaTypes(mediaTypes);

        FastJsonConfig config = new FastJsonConfig();
        config.setSerializerFeatures(
                // sort
                SerializerFeature.SortField,
                // Output null field
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.IgnoreErrorGetter
        );
        converter.setFastJsonConfig(config);

        return new HttpMessageConverters(converter);
    }

}
