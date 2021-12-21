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
package com.welab.wefe.common.util;

import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author zane
 * @date 2021/11/10
 */
public class ImageUtil {

    /**
     * Compresses the image with the specified size and precision
     *
     * @param base64Image The source image base64
     * @param desFileSize Specifies the image size, in KB
     * @param accuracy    Accuracy, recursive compression ratio, recommended to be less than 0.9
     * @return Compressed image base64
     */
    public static String compressPicForScale(String base64Image, long desFileSize, double accuracy) {
        try {
            byte[] srcBase = Base64Util.base64ToByteArray(base64Image);
            ByteArrayInputStream stream = new ByteArrayInputStream(srcBase);
            BufferedImage image = ImageIO.read(stream);

            //Get picture information
            int srcWidth = image.getWidth();
            int srcHeight = image.getHeight();

            //Convert to PNG first
            Thumbnails.Builder builder = Thumbnails.of(image).imageType(BufferedImage.TYPE_INT_ARGB).outputFormat("png");
            builder.size(srcWidth, srcHeight);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            builder.toOutputStream(baos);

            byte[] bytes = commpressPicCycle(baos.toByteArray(), desFileSize, accuracy);

            String desBase64String = Base64Util.encode(bytes);
            return desBase64String;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] commpressPicCycle(byte[] bytes, long desFileSize, double accuracy) throws IOException {

        long srcFileSizeJpg = bytes.length;
        // Determine the size to reach the compression target return
        if (srcFileSizeJpg <= desFileSize * 1024) {
            return bytes;
        }
        // Calculate wide high
        BufferedImage bim = ImageIO.read(new ByteArrayInputStream(bytes));
        int srcWidth = bim.getWidth();
        int srcHeight = bim.getHeight();
        int desWidth = new BigDecimal(srcWidth).multiply(
                new BigDecimal(accuracy)).intValue();
        int desHeight = new BigDecimal(srcHeight).multiply(
                new BigDecimal(accuracy)).intValue();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(bytes)).size(desWidth, desHeight).outputQuality(accuracy).imageType(BufferedImage.TYPE_INT_ARGB).outputFormat("jpg").toOutputStream(baos);
        return commpressPicCycle(baos.toByteArray(), desFileSize, accuracy);
    }
}
