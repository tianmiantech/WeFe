/**
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

package com.welab.wefe.board.service.service.modelexport;


import com.welab.wefe.common.wefe.enums.ModelExportLanguage;

import java.util.HashMap;
import java.util.Map;

/**
 * Xgboost language selector
 *
 * @author aaron.li
 **/
public class XgboostLanguageSelector {
    private final String language;
    private static final Map<String, BaseXgboostLanguage> LANGUAGE_MAP = new HashMap<>(16);

    static {
        LANGUAGE_MAP.put(ModelExportLanguage.c.name(), new XgboostCLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.cSharp.name(), new XgboostCSharpLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.dart.name(), new XgboostDartLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.go.name(), new XgboostGoLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.haskell.name(), new XgboostHaskellLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.java.name(), new XgboostJavaLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.javaScript.name(), new XgboostJavaScriptLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.php.name(), new XgboostPhpLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.powerShell.name(), new XgboostPowerShellLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.python.name(), new XgboostPythonLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.r.name(), new XgboostRLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.ruby.name(), new XgboostRubyLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.visualBasic.name(), new XgboostVisualBasicLanguage());
        LANGUAGE_MAP.put(ModelExportLanguage.pmml.name(), new XgboostPmmlLanguage());

    }

    public XgboostLanguageSelector(String language) {
        this.language = language;
    }

    public BaseXgboostLanguage getSelector() {
        return LANGUAGE_MAP.get(language);
    }
}
