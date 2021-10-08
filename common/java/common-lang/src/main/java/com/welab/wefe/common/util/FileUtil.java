/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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
import java.io.*;
import java.math.BigDecimal;

/**
 * @author zane.luo
 */
public class FileUtil {

    /**
     * Create a directory
     *
     * @param dirPath Directory path
     */
    public static void createDir(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }


    public static String readAll(String path) throws IOException {
        StringBuilder content = new StringBuilder(32);
        BufferedReader in = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            in = new BufferedReader(isr);
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line.trim());
            }
        } catch (IOException e) {
            throw e;
        } finally {
            in.close();
        }
        return content.toString();
    }

    /**
     * Reading file contents
     *
     * @return String
     */
    public static String readAll(String path, String encoding) throws IOException {
        StringBuilder content = new StringBuilder(32);
        BufferedReader in = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(fis, encoding);
            in = new BufferedReader(isr);
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line + "\n");
            }
        } catch (IOException e) {
            throw e;
        } finally {
            in.close();
        }
        return content.toString();
    }

    /**
     * Delete files or folders
     *
     * @param file File or folder
     */
    public static void deleteFile(File file) {
        if (null == file) {
            return;
        }
        if (!file.isDirectory()) {
            file.delete();
            return;
        }
        File[] subFile = file.listFiles();
        for (File f : subFile) {
            deleteFile(f);
        }
        file.delete();
    }

    public static void deleteFile(String filePath) {
        deleteFile(new File(filePath));
    }

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
