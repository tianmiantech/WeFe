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
package com.welab.wefe.common.test.xml;

import com.alibaba.fastjson.JSON;
import com.welab.wefe.common.util.XmlUtil;

import java.util.ArrayList;

/**
 * @author zane
 * @date 2021/11/10
 */
public class XmlTest {

    public static void main(String[] args) throws Exception {
        Model bean = new Model();
        bean.intField = 1;
        bean.stringField = "string";
        bean.listField = new ArrayList<>();
        bean.listField.add(new Model.Item("hello", 2));
        bean.listField.add(new Model.Item("world", 3));

        //XML序列化
        String xml1 = XmlUtil.toXml(bean);
        System.out.println(xml1);

        System.out.println();
        System.out.println("----------------------------------");
        System.out.println();

        //XML反序列化
        bean = XmlUtil.toModel(xml1, Model.class);
        System.out.println(JSON.toJSONString(bean, true));

        System.out.println();
        System.out.println("----------------------------------");
        System.out.println();

        String xml2 = XmlUtil.toXml(bean);
        System.out.println(xml1);

        if (!xml1.equals(xml2)) {
            throw new Exception("序列化或反序列化异常：两次序列化后得到的 xml 不一致。");
        }
    }

}
