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

package com.welab.wefe.common.web.service;

import com.welab.wefe.common.WeSpecCaptcha;
import com.welab.wefe.common.web.dto.Captcha;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author hunter.zhao
 */
public class CaptchaService {
    private static final Logger LOG = LoggerFactory.getLogger(CaptchaService.class);
    /**
     * Verification code aging 60s
     */
    private static ExpiringMap<String, String> captchaMap = ExpiringMap
            .builder()
            .expiration(60, TimeUnit.SECONDS)
            .maxSize(10000)
            .build();

    public static void init() {
    }

    /**
     * Get verification code
     */
    public static Captcha get() {
        return get(85, 35, 5);
    }


    /**
     * Get verification code
     */
    public static synchronized Captcha get(int width, int height, int len) {
        WeSpecCaptcha specCaptcha = new WeSpecCaptcha(width, height, len);
        specCaptcha.setFont(new Font("楷体", Font.PLAIN, 24));

        String code = specCaptcha.text().toLowerCase();
        String key = UUID.randomUUID().toString();
        captchaMap.put(key, code);

        return Captcha.of(key, specCaptcha.toBase64());
    }

    /**
     * Verification code
     */
    public synchronized static Boolean verify(String key, String code) {

        try {
            String verCode = captchaMap.get(key);

            if (verCode == null) {
                return false;
            }
            
            // Judgment verification code
            if (verCode.equals(code.trim().toLowerCase())) {
                return true;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            if (captchaMap.containsKey(key)) {
                captchaMap.remove(key);
            }
        }

        return false;
    }
}
