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
 * 由于父类中默认的取色方案是随机的，导致验证码每个字符都是单独的颜色，这削弱了验证码的安全性。
 * 子类继承后重写取色相关方法，控制为单一颜色。
 *
 * @author zane
 * @date 2022/2/23
 */
public class WeSpecCaptcha extends SpecCaptcha {
    /**
     * 画笔颜色
     */
    private int[] color = null;

    public WeSpecCaptcha() {
    }

    public WeSpecCaptcha(int width, int height) {
        super(width, height);
    }

    public WeSpecCaptcha(int width, int height, int len) {
        super(width, height, len);
    }

    public WeSpecCaptcha(int width, int height, int len, Font font) {
        super(width, height, len, font);
    }


    /**
     * 由于默认的取色方案是随机的，导致验证码每个字符都是单独的颜色，这削弱了验证码的安全性，所以这里控制为单一颜色。
     */
    @Override
    protected Color color() {
        if (color == null) {
            color = COLOR[num(COLOR.length)];
        }
        return new Color(color[0], color[1], color[2]);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            WeSpecCaptcha captcha = new WeSpecCaptcha(85, 35, 5);
            captcha.setFont(new Font("楷体", Font.PLAIN, 24));
            System.out.println(captcha.toBase64());
        }
    }
}
