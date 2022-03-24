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

package com.welab.wefe.serving.service.utils;

import java.math.BigInteger;
import java.security.MessageDigest;

public class MD5Util {

    public static String getMD5String(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    public static void main(String[] args) {
//        PythonInterpreter interpreter = new PythonInterpreter();
////		interpreter.exec("a=[5,2,3,9,4,0]");
////		interpreter.exec("print(sorted(a));");
////		interpreter.exec("print sorted(a);");
//
////        interpreter.execfile("/Users/hunter.zhao/Documents/temp/test.py");
//        interpreter.execfile("/Users/hunter.zhao/Desktop/workspace/code/Serving-0.5.0/python/examples/yolov4/test_client.py");
//
//        PyFunction pyFunction = interpreter.get("predict", PyFunction.class);
//        int a = 5, b = 6;
//        PyObject pyobj = pyFunction.__call__(new PyInteger(a), new PyInteger(b));
//        System.out.println(pyobj);
//    }
}
