/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.common.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * homepage: https://github.com/x-stream/xstream
 * demo: https://www.cnblogs.com/LiZhiW/p/4313493.html
 *
 * @author zane
 * @date 2021/11/10
 * @see com.welab.wefe.common.test.xml.XmlTest#main(String[])
 */
public class XmlUtil {
    /**
     * The XStream instance is thread-safe.
     * That is, once the XStream instance has been created and configured,
     * it may be shared across multiple threads allowing objects to be serialized/deserialized concurrently.
     */
    private static final XStream XSTREAM = new XStream();
    /**
     * 对象复用，提升性能。
     */
    private static final Map<Class, XStream> XSTREAM_MAP = new HashMap<>();

    static {
        XSTREAM.autodetectAnnotations(true);
    }

    /**
     * {@link XmlUtil#toModel(String, Class)}
     */
    public static <T> T toModel(File xmlFile, Class<T> clazz) throws IOException {
        String xmlString = FileUtil.readAllText(xmlFile);
        return toModel(xmlString, clazz);
    }

    /**
     * xml string to model
     * <p>
     * the model must set @XStreamAlias
     *
     * @see com.welab.wefe.common.test.xml.XmlTest#main(String[])
     */
    public static <T> T toModel(String xmlString, Class<T> clazz) {
        // 对象复用，提升性能。
        XStream xStream = XSTREAM_MAP.get(clazz);
        if (xStream == null) {
            xStream = new XStream(new StaxDriver());
            xStream.autodetectAnnotations(true);
            xStream.addPermission(AnyTypePermission.ANY);
            xStream.processAnnotations(clazz);

            XSTREAM_MAP.put(clazz, xStream);
        }

        T model = null;
        try {
            model = clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
        return (T) xStream.fromXML(xmlString, model);
    }

    /**
     * model to xml string
     */
    public static String toXml(Object obj) {
        return XSTREAM.toXML(obj);
    }
}
