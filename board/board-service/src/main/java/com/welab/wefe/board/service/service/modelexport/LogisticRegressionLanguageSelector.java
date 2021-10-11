/**
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

import com.welab.wefe.common.enums.ModelExportLanguage;

import java.util.HashMap;
import java.util.Map;

/**
 * Language selector for logistic regression
 *
 * @author aaron.li
 **/
public class LogisticRegressionLanguageSelector {
    private final String language;
    private static final Map<String, BaseLogisticRegressionLanguage> LANGUAGE_MAP = new HashMap<>(16);

    static {
        LANGUAGE_MAP.put(ModelExportLanguage.c.name(), new LogisticRegressionCLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.cSharp.name(), new LogisticRegressionCSharpLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.dart.name(), new LogisticRegressionDartLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.go.name(), new LogisticRegressionGoLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.haskell.name(), new LogisticRegressionHaskellLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.java.name(), new LogisticRegressionJavaLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.javaScript.name(), new LogisticRegressionJavaScriptLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.php.name(), new LogisticRegressionPhpLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.powerShell.name(), new LogisticRegressionPowerShellLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.python.name(), new LogisticRegressionPythonLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.r.name(), new LogisticRegressionRLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.ruby.name(), new LogisticRegressionRubyLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.visualBasic.name(), new LogisticRegressionVisualBasicLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.pmml.name(), new LogisticRegressionPmmlLanguage());
    }

    public LogisticRegressionLanguageSelector(String language) {
        this.language = language;
    }

    public BaseLogisticRegressionLanguage getSelector() {
        return LANGUAGE_MAP.get(language);
    }
}
