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
package com.welab.wefe.common;

import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;

/**
 * @author zane
 * @date 2022/7/14
 */
public class Base64Test {
    public static void main(String[] args) {
        String data = "BAAlPs93VYAxpfbz0nTzua1bqdUsXRJWPwgjjoa3zJQ9BASXIX2BKE/Ik9GwXwk2Fxcz1ntBeyD/mSYHmswGOrcJdG3sSGFeKbyE64HytoxdzwXqFuLaCki8awVVjTRC83PhSA7It9EUbQDiHCWt+twAVZyAb9fO6Bb6Eteup9eHBiEhfLObKe478YQRkdHPcCuYiFmGZld5opNb9YXoaqWjCRew44ZuQLG1HD1BVZHt/5HaBh81gAMUiKhJs/ex9SfDLY/mEoGuhCuBdYpUUHxyWNRN2hmoSaWfeMguGmXgj1DjRQ/SF+4CasDF9AgF3PpeOV4Q4GNl+6QboDwdQw==";
        byte[] decode = Base64Utils.decode(data.getBytes(StandardCharsets.UTF_8));
    }
}
