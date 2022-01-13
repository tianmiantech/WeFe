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

package com.welab.wefe.common.wefe.enums;

/**
 * Languages supported by the model export language
 *
 * @author aaron.li
 **/
public enum ModelExportLanguage {

    c,

    cSharp,

    dart,

    go,

    haskell,

    java,

    javaScript,

    php,

    powerShell,

    python,

    r,

    ruby,

    visualBasic,

    pmml;


    public static boolean isExist(String language) {
        ModelExportLanguage[] modelExportLanguages = ModelExportLanguage.values();
        for (ModelExportLanguage modelExportLanguage : modelExportLanguages) {
            if (modelExportLanguage.name().equals(language)) {
                return true;
            }
        }
        return false;
    }

    public static ModelExportLanguage get(String language) {
        ModelExportLanguage[] modelExportLanguages = ModelExportLanguage.values();
        for (ModelExportLanguage modelExportLanguage : modelExportLanguages) {
            if (modelExportLanguage.name().equals(language)) {
                return modelExportLanguage;
            }
        }
        return null;
    }

    public static String getLanguageSuffix(ModelExportLanguage modelExportLanguage) {
        if (null == modelExportLanguage) {
            return "";
        }
        return getLanguageSuffix(modelExportLanguage.name());
    }

    public static String getLanguageSuffix(String language) {
        ModelExportLanguage modelExportLanguage = get(language);
        if (null == modelExportLanguage) {
            return "";
        }
        switch (modelExportLanguage) {
            case c:
                return "c";
            case cSharp:
                return "cs";
            case dart:
                return "dart";
            case go:
                return "go";
            case haskell:
                return "hs";
            case java:
                return "java";
            case javaScript:
                return "js";
            case php:
                return "php";
            case powerShell:
                return "ps1";
            case python:
                return "py";
            case r:
                return "R";
            case ruby:
                return "rb";
            case visualBasic:
                return "vb";
            case pmml:
                return "pmml";
            default:
                return "";
        }

    }

}
