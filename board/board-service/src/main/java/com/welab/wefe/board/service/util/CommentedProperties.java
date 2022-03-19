/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.util;

import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;

import java.io.*;
import java.util.*;

/**
 * File reading classes that support annotation retention
 *
 * @author aaron.li
 * @date 2021/12/21 10:58
 **/
public class CommentedProperties {
    private final static String COMMENT_CHAR = "#";
    private final static String ATTRIBUTE_SPLIT_CHAR = "=";
    private final Map<String, String> properties = new HashMap<>(16);
    private final List<LineAttribute> lineAttributeList = new ArrayList<>();

    public void load(String path) throws IOException {
        List<String> lineStrList = FileUtil.readAllForLine(path, "UTF-8");
        LineAttribute lineAttribute = null;
        for (int i = 0; i < lineStrList.size(); i++) {
            String lineStr = lineStrList.get(i);
            lineAttribute = new LineAttribute();
            lineAttribute.setIndex(i);
            lineAttribute.setContent(lineStr);
            if (isAttributeLine(lineStr)) {
                lineAttribute.setComment(false);
                Attribute attribute = getAttribute(lineStr);
                lineAttribute.setAttribute(getAttribute(lineStr));
                properties.put(attribute.getKey(), attribute.getValue());
            }
            lineAttributeList.add(lineAttribute);
        }
    }

    public void setProperty(String key, String value) {
        if (StringUtil.isEmpty(key) || !properties.containsKey(key)) {
            return;
        }
        value = (StringUtil.isNotEmpty(value) ? value : "");
        properties.put(key, value);
        for (LineAttribute lineAttribute : lineAttributeList) {
            Attribute attribute = lineAttribute.getAttribute();
            if (null != attribute && key.equals(attribute.getKey())) {
                attribute.setValue(value);
                lineAttribute.setContent(key + ATTRIBUTE_SPLIT_CHAR + value);
                break;
            }
        }
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return properties.entrySet();
    }

    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }


    public void append(String key, String value, String comment) {
        if (StringUtil.isEmpty(key) || properties.containsKey(key)) {
            return;
        }
        properties.put(key, value);
        if (StringUtil.isNotEmpty(comment)) {
            LineAttribute commentLineAttribute = new LineAttribute();
            commentLineAttribute.setIndex(lineAttributeList.size());
            commentLineAttribute.setContent(COMMENT_CHAR + " " + comment);
            commentLineAttribute.setComment(true);
            lineAttributeList.add(commentLineAttribute);
        }

        LineAttribute lineAttribute = new LineAttribute();
        lineAttribute.setIndex(lineAttributeList.size());
        lineAttribute.setContent(key + ATTRIBUTE_SPLIT_CHAR + value);
        lineAttribute.setComment(false);
        Attribute attribute = new Attribute();
        attribute.setKey(key);
        attribute.setValue(value);
        lineAttribute.setAttribute(attribute);
        lineAttributeList.add(lineAttribute);
    }

    public void store(String path) throws IOException {
        Writer outputStream = null;
        StringBuilder fileContent = new StringBuilder();
        try {
            for (LineAttribute lineAttribute : lineAttributeList) {
                fileContent.append(lineAttribute.content).append("\n");
            }
            outputStream = new FileWriter(path, false);
            outputStream.write(fileContent.toString());
            outputStream.flush();
        } finally {
            if (null != outputStream) {
                outputStream.close();
            }
        }
    }

    public static void writeTxtFile(String path, String text, boolean append) throws IOException {
        FileWriter writer = new FileWriter(path, append);
        writer.append(text);
        writer.close();
    }


    private boolean isAttributeLine(String lineStr) {
        if (StringUtil.isEmpty(lineStr) || StringUtil.isEmpty(lineStr = lineStr.trim())) {
            return false;
        }
        if (lineStr.startsWith(COMMENT_CHAR)) {
            return false;
        }
        return true;
    }

    private Attribute getAttribute(String lineStr) {
        lineStr = lineStr.trim();
        Attribute attribute = new Attribute();
        if (lineStr.contains(ATTRIBUTE_SPLIT_CHAR)) {
            String[] attributeArray = lineStr.split(ATTRIBUTE_SPLIT_CHAR, 2);
            attribute.setKey(attributeArray[0]);
            attribute.setValue(attributeArray[1]);
        } else {
            attribute.setKey(lineStr);
            attribute.setValue("");
        }

        return attribute;
    }


    private class LineAttribute {
        private int index;
        private String content;
        private boolean isComment = true;
        private Attribute attribute;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isComment() {
            return isComment;
        }

        public void setComment(boolean comment) {
            isComment = comment;
        }

        public Attribute getAttribute() {
            return attribute;
        }

        public void setAttribute(Attribute attribute) {
            this.attribute = attribute;
        }
    }

    private class Attribute {
        private String key;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
