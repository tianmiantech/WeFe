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

import com.wf.captcha.SpecCaptcha;

import java.awt.*;

/**
 * @author zane
 * @date 2022/2/23
 */
public class WeSpecCaptcha extends SpecCaptcha {
    @Override
    protected Color color() {
        int[] color = COLOR[num(COLOR.length)];
        return new Color(color[0], color[1], color[2]);
    }

    public static void main(String[] args) {
        WeSpecCaptcha captcha = new WeSpecCaptcha();
//        captcha.toBase64()
//        captcha.write("/Users/zane/Desktop/test.png");
    }
}
