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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author Zane
 */
public class ReflectionsUtil {


    /**
     * Scans the class under the specified package path that contains the specified annotation
     */
    public static List<Class<?>> getClassesWithAnnotation(String path, Class<? extends Annotation> annotation) {

        try (
                ScanResult scanResult = new ClassGraph()
                        .enableAllInfo()
                        .whitelistPackages(path)
                        .scan()
        ) {
            return scanResult.getClassesWithAnnotation(annotation.getName()).loadClasses();
        }
    }

    /**
     * Scans the class that contains the specified annotation under the specified root path
     */
    public static List<Class<?>> getClassesWithAnnotation(Class<? extends Annotation> annotation) {

        try (
                ScanResult scanResult = new ClassGraph()
                        .enableAllInfo()
                        .scan()
        ) {
            return scanResult.getClassesWithAnnotation(annotation.getName()).loadClasses();
        }
    }

    public static <T> List<Class<?>> getClassesImplementing(Class<T> clazz) {
        try (
                ScanResult scanResult = new ClassGraph()
                        .enableAllInfo()
                        .scan()
        ) {
            return scanResult.getClassesImplementing(clazz.getName())
                    .loadClasses();
        }
    }
}
