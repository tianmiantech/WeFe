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

import java.util.List;

/**
 * C#
 *
 * @author aaron.li
 **/
public class XgboostCSharpLanguage extends BaseXgboostLanguage {

    @Override
    protected String preBuild2ClassificationsMethodSignNameCode(int treeNum, String initScore) {
        StringBuilder preMethodCode = new StringBuilder();
        preMethodCode.append("using static System.Math;")
                .append("\n")
                .append("namespace ML {")
                .append("\n")
                .append("    public static class Model {")
                .append("\n")
                .append("        public static double[] Score(double[] input) {")
                .append("\n")
                .append(METHOD_BODY_PLACEHOLDER)
                .append("\n")
                .append(indentationNum(2) + "}")
                .append("\n")
                .append(indentationNum(1) + "}")
                .append("\n")
                .append("}");
        return preMethodCode.toString();
    }

    @Override
    protected String build2ClassificationsResultLogicCode(int treeNum, String initScore) {
        StringBuilder methodCalcCode = new StringBuilder();
        String summaryVar = "s1";
        methodCalcCode.append(indentationByNodeLayer(1, true));
        methodCalcCode.append(generateVarDef(summaryVar))
                .append("\n")
                .append(indentationByNodeLayer(1, true))
                .append(summaryVar)
                .append(" = 1 / (1 + Exp(0 - (")
                .append(generateTreeSum(treeNum, initScore))
                .append(")));")
                .append("\n")
                .append(build2ClassificationsReturnCode(summaryVar, initScore));

        return methodCalcCode.toString();
    }

    @Override
    protected String build2ClassificationsReturnCode(String varName, String initScore) {
        StringBuilder methodResultCode = new StringBuilder();
        methodResultCode.append(indentationByNodeLayer(1, true))
                .append("return new double[2] {1 - " + varName + ", " + varName + "}" + lineEndSymbol());

        return methodResultCode.toString();
    }

    @Override
    protected String buildExpFunction(List<String> treeVarNameList, String initScore) {
        StringBuilder code = new StringBuilder();
        code.append("Exp(0 - (")
                .append(generateTreeSum(treeVarNameList, initScore))
                .append("))");

        return code.toString();
    }

    @Override
    protected String buildMultipleClassificationsReturnCode(int classificationsNum) {
        StringBuilder code = new StringBuilder();
        code.append(resultIndentationNum(2))
                .append("return new double[" + classificationsNum + "] {");
        for (int i = 0; i < classificationsNum; i++) {
            code.append(generateResultVarName(i))
                    .append(" / (")
                    .append(generateTreeClassificationsResultSum(classificationsNum))
                    .append(")");
            if (i < classificationsNum - 1) {
                code.append(", ");
            }
        }
        code.append("}").append(lineEndSymbol());
        return code.toString();
    }

}
