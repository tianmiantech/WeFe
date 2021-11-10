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
        XStream xstream = new XStream(new StaxDriver());
        xstream.autodetectAnnotations(true);
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.processAnnotations(clazz);

        T model = null;
        try {
            model = clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
        return (T) xstream.fromXML(xmlString, model);
    }

    /**
     * model to xml string
     */
    public static String toXml(Object obj) {
        XStream xstream = new XStream();
        xstream.autodetectAnnotations(true);
        return xstream.toXML(obj);
    }
}
