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

package com.welab.wefe.board.service.service.modelexport;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Basic classes of logistic regression language
 *
 * @author aaron.li
 **/
public class BaseLogisticRegressionLanguage {

    /**
     * Method body placeholder
     */
    public static final String METHOD_BODY_PLACEHOLDER = "#body#";

    /**
     * Code indent unit character
     */
    public static final String INDENTATION_UNIT_CHAR = "    ";


    /**
     * Generate complete code
     *
     * @param headers   Header information
     * @param weightMap Weight information
     * @param intercept intercept
     */
    public String generateMethodCode(List<String> headers, Map<String, String> weightMap, String intercept) {
        // Method pre signature code
        String preMethodSignNameCode = generatePreMethodSignNameCode();
        // Method body code
        String methodBodyCode = generateMethodBodyCode(headers, weightMap, intercept);

        return preMethodSignNameCode.replace(METHOD_BODY_PLACEHOLDER, methodBodyCode);
    }


    /**
     * Generate method pre signature code
     */
    protected String generatePreMethodSignNameCode() {
        StringBuilder preMethodCode = new StringBuilder();
        preMethodCode.append("public class Model {")
                .append("\n")
                .append(INDENTATION_UNIT_CHAR)
                .append("public static double score(double[] input) {")
                .append("\n")
                .append(INDENTATION_UNIT_CHAR)
                .append(INDENTATION_UNIT_CHAR)
                .append(METHOD_BODY_PLACEHOLDER)
                .append("\n")
                .append(INDENTATION_UNIT_CHAR)
                .append("}")
                .append("\n")
                .append("}");

        return preMethodCode.toString();
    }

    /**
     * Generate method body code
     */
    protected String generateMethodBodyCode(List<String> headers, Map<String, String> weightMap, String intercept) {
        StringBuilder bodyCode = new StringBuilder();
        if (CollectionUtils.isEmpty(headers)) {
            return bodyCode.toString();
        }
        bodyCode.append(generateReturnChar())
                .append("((")
                .append(intercept)
                .append(") + ");
        for (int i = 0; i < headers.size(); i++) {
            String feature = headers.get(i);
            bodyCode.append("((")
                    .append(generateCompareVarName(i))
                    .append(") * (")
                    .append(weightMap.get(feature))
                    .append("))");
            if (i < headers.size() - 1) {
                bodyCode.append(" + ");
            }
        }
        bodyCode.append(")")
                .append(lineEndSymbol());
        return bodyCode.toString();
    }

    /**
     * Line end symbol
     */
    protected String lineEndSymbol() {
        return ";";
    }

    /**
     * Generate indent symbol
     *
     * @param num Indent number
     */
    protected String generateIndentationChar(int num) {
        StringBuilder blankCharSb = new StringBuilder();
        // Root node
        for (int i = 1; i <= num; i++) {
            blankCharSb.append(INDENTATION_UNIT_CHAR);
        }

        return blankCharSb.toString();
    }


    /**
     * Generate comparison variable name
     *
     * @param index index
     */
    protected String generateCompareVarName(int index) {
        return "input[" + index + "]";
    }

    /**
     * Generate return value characters
     */
    protected String generateReturnChar() {
        return "return ";
    }

}
