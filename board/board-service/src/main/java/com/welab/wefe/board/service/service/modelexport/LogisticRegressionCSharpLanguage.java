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

package com.welab.wefe.board.service.service.modelexport;

/**
 * C# language
 *
 * @author aaron.li
 **/
public class LogisticRegressionCSharpLanguage extends BaseLogisticRegressionLanguage {

    @Override
    protected String generatePreMethodSignNameCode() {
        StringBuilder preMethodCode = new StringBuilder();
        preMethodCode.append("namespace ML {")
                .append("\n")
                .append(generateIndentationChar(1))
                .append("public static class Model {")
                .append("\n")
                .append(generateIndentationChar(2))
                .append("public static double Score(double[] input) {")
                .append("\n")
                .append(generateIndentationChar(3))
                .append(METHOD_BODY_PLACEHOLDER)
                .append("\n")
                .append("}}}");


        return preMethodCode.toString();
    }
}
