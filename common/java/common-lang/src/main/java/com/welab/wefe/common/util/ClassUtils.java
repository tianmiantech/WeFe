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

package com.welab.wefe.common.util;

import com.alibaba.fastjson.JSONObject;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Zane
 */
public class ClassUtils {

    /**
     * build a readable type name for field
     */
    public static String getTypeSimpleName(Class<?> type) {
        String name = type.getCanonicalName();
        name = name.contains(".") ? StringUtil.substringAfterLast(name, ".") : name;
        name = name.replace("$", ".");
        return name;
    }

    public static Type getListFieldGenericType(Field field) {
        Class<?> type = field.getType();

        if (!type.equals(List.class)) {
            throw new UnsupportedOperationException();
        }

        Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        return genericType;
    }

    /**
     * Create a class based on the class name
     */
    public static Class<?> createClassFromName(String name) {
        try {

            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * overloading
     *
     * @see ClassUtils#listFields(Class, boolean)
     */
    public static Set<Field> listFields(Class clazz) {
        return listFields(clazz, true);
    }

    /**
     * Lists all fields in Class, including those in the parent Class.
     *
     * @param clazz              type
     * @param excludeStaticField Whether to exclude static fields
     */
    public static Set<Field> listFields(Class clazz, boolean excludeStaticField) {

        if (clazz == null) {
            return null;
        }

        List<Field> fields = new ArrayList<>();

        // Loop through all fields (including parent classes)
        do {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();

        } while (clazz != null && clazz != Object.class);

        Set<Field> result = new HashSet<>();

        for (Field field : fields) {
            if (excludeStaticField && Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            result.add(field);
        }

        return result;
    }

    /**
     * Gets the generic type of the type
     */
    public static Class<?> getGenericClass(Class<?> clazz, int index) {
        Type genericSuperclass = clazz.getGenericSuperclass();

        // 接口没有 GenericSuperclass，需要取 GenericInterfaces。
        if (genericSuperclass == null) {
            Type[] types = clazz.getGenericInterfaces();
            if (types.length > 0) {
                genericSuperclass = types[0];
            }
        }

        while (true) {
            if (genericSuperclass == null) {
                return null;
            }

            if (genericSuperclass instanceof ParameterizedType) {
                Type actualTypeArgument = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[index];
                if (actualTypeArgument instanceof TypeVariableImpl) {
                    return null;
                } else {
                    return (Class<?>) actualTypeArgument;
                }

            } else {
                try {
                    genericSuperclass = Class.forName(genericSuperclass.getTypeName()).getGenericSuperclass();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Static class to Json
     */
    public static final JSONObject staticClassToJson(Class clazz) throws IllegalAccessException, InstantiationException {
        Object instance = clazz.newInstance();

        Map<String, Object> stringObjectMap = staticClassToJson(clazz, instance);
        return new JSONObject(stringObjectMap);
    }

    private static final Map<String, Object> staticClassToJson(Class clazz, Object instance) throws IllegalAccessException {
        Map<String, Object> result = new HashMap<>(16);

        // Adding a root Node
        result.putAll(staticClassToJsonWithoutChild(clazz, instance));

        // Adding child Nodes
        for (Class<?> subClazz : clazz.getDeclaredClasses()) {

            Map<String, Object> subResult = new HashMap<>(16);
            subResult.putAll(staticClassToJson(subClazz, instance));

            result.put(subClazz.getSimpleName(), subResult);
        }

        return result;
    }

    private static final Map<String, String> staticClassToJsonWithoutChild(Class clazz, Object instance) throws IllegalAccessException {
        Map<String, String> result = new HashMap<>(16);

        // Adding a root Node
        Set<Field> fields = ClassUtils.listFields(clazz, false);
        for (Field field : fields) {
            field.setAccessible(true);
            result.put(field.getName(), String.valueOf(field.get(instance)));
        }

        return result;
    }

    public static boolean isAbstract(Class clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

}
