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

package com.welab.wefe.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;


/**
 * @author yuxin.zhang
 **/
public class ClassPathScanHandler {

    static final Logger log = LoggerFactory.getLogger(ClassPathScanHandler.class);

    /**
     * Whether to exclude inner classes
     */
    private boolean excludeInner = true;

    /**
     * Application of filtering rules: true search matching rules, false search matching rules
     */
    private boolean checkInOrEx = true;

    /**
     * Filtering rule list: if it is null or empty, it means all matches without filtering
     */
    private List<String> classFilters = null;

    public ClassPathScanHandler() {
    }

    public ClassPathScanHandler(Boolean excludeInner, Boolean checkInOrEx, List<String> classFilters) {
        this.excludeInner = excludeInner;
        this.checkInOrEx = checkInOrEx;
        this.classFilters = classFilters;
    }


    public Set<Class<?>> getPackageAllClasses(String basePackage, boolean recursive) {
        log.info("basePackage: " + basePackage);
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        String packageName = basePackage;
        if (packageName.endsWith(".")) {
            packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        }
        String package2Path = packageName.replace('.', '/');
        log.info("package2Path: " + package2Path);

        try {
            Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(package2Path);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    log.info("The file type of the scanned file...");
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    doScanPackageClassesByFile(classes, packageName, filePath, recursive);
                } else if ("jar".equals(protocol)) {
                    log.info("Scan the classes in the jar file....");
                    doScanPackageClassesByJar(packageName, url, recursive, classes);
                }
            }
        } catch (IOException e) {
            log.error("IOException error:", e);
        }

        return classes;
    }

    private void doScanPackageClassesByJar(String basePackage, URL url,
                                           final boolean recursive, Set<Class<?>> classes) {
        String packageName = basePackage;
        String package2Path = packageName.replace('.', '/');

        try {
            JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith(package2Path) || entry.isDirectory()) {
                    continue;
                }

                if (!recursive && name.lastIndexOf('/') != package2Path.length()) {
                    continue;
                }

                if (this.excludeInner && name.indexOf('$') != -1) {
                    log.info("exclude inner class with name:" + name);
                    continue;
                }

                String classSimpleName = name.substring(name.lastIndexOf('/') + 1);
                if (this.filterClassName(classSimpleName)) {
                    String className = name.replace('/', '.');
                    className = className.substring(0, className.length() - 6);
                    try {
                        log.info("find class: " + className);
                        classes.add(Thread.currentThread().getContextClassLoader().loadClass(className));
                    } catch (ClassNotFoundException e) {
                        log.error("Class.forName error:", e);
                    }
                }
            }
        } catch (IOException e) {
            log.error("IOException error:", e);
        }
    }

    private void doScanPackageClassesByFile(Set<Class<?>> classes,
                                            String packageName, String packagePath, boolean recursive) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        final boolean fileRecursive = recursive;
        File[] dirFiles = dir.listFiles(file -> {
            if (file.isDirectory()) {
                return fileRecursive;
            }
            String filename = file.getName();
            if (excludeInner && filename.indexOf('$') != -1) {
                log.info("exclude inner class with name:" + filename);
                return false;
            }
            return filterClassName(filename);
        });

        for (File file : dirFiles) {
            if (file.isDirectory()) {
                doScanPackageClassesByFile(classes, packageName + "."
                        + file.getName(), file.getAbsolutePath(), recursive);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    log.info("find class: " + className);
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    log.error("IOException error:", e);
                }
            }
        }
    }

    private boolean filterClassName(String className) {
        if (!className.endsWith(".class")) {
            return false;
        }
        if (null == this.classFilters || this.classFilters.isEmpty()) {
            return true;
        }
        String tmpName = className.substring(0, className.length() - 6);
        boolean flag = false;
        for (String str : classFilters) {
            String tmpreg = "^" + str.replace("*", ".*") + "$";
            Pattern p = Pattern.compile(tmpreg);
            if (p.matcher(tmpName).find()) {
                flag = true;
                break;
            }
        }
        return (checkInOrEx && flag) || (!checkInOrEx && !flag);
    }

    public boolean isExcludeInner() {
        return excludeInner;
    }

    public boolean isCheckInOrEx() {
        return checkInOrEx;
    }

    public List<String> getClassFilters() {
        return classFilters;
    }

    public void setExcludeInner(boolean pExcludeInner) {
        excludeInner = pExcludeInner;
    }

    public void setCheckInOrEx(boolean pCheckInOrEx) {
        checkInOrEx = pCheckInOrEx;
    }

    public void setClassFilters(List<String> pClassFilters) {
        classFilters = pClassFilters;
    }

}
