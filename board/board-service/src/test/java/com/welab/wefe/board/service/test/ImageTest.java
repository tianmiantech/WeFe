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
package com.welab.wefe.board.service.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author zane
 * @date 2021/11/9
 */
public class ImageTest {
    public static void main(String[] args) throws IOException {
        String path = "/Users/zane/Downloads/test.png";
//        String path = "/Users/zane/data/wefe_file_upload_dir/fl_fruit/JPEGImages/apple_1.jpg";
        File picture = new File(path);

        BufferedImage sourceImg = ImageIO.read(new FileInputStream(picture));

        System.out.println(sourceImg.getRaster().getNumDataElements());
        System.out.println(sourceImg.getWidth());
        System.out.println(sourceImg.getHeight());

    }
}
