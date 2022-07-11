package com.webank.cert.mgr.utils;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wesleywang
 */
public class TransformUtils {

    private static final String SET = "set";
    private static final String GET = "get";
    private static final String IS = "is";

    private static ConcurrentReferenceHashMap<Class,Map<String, Method>> cache = new ConcurrentReferenceHashMap<>();

    public static <T, V> V simpleTransform(T t, Class<V> cla) {
        if (t == null){
            return null;
        }
        V target = null;
        try {
            target = cla.newInstance();
            simpleTransform(t,target);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return target;
    }

    public static <T, V> List<V> simpleTransform(List<T> t, Class<V> cla) {
        List<V> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(t)){
            return list;
        }
        t.forEach(e -> {
            list.add(simpleTransform(e,cla));
        });
        return list;
    }

    public static <T, V> void simpleTransform(T t, V v) {
        Class cls = t.getClass();
        Class cla = v.getClass();
        try {
            Map<String, Method> map = cache.get(cla);
            if (map == null) {
                map = new HashMap<>();
                Method[] methods = cla.getMethods();
                for (Method method : methods) {
                    if (method.getName().startsWith(SET)) {
                        map.put(method.getName().replaceFirst(SET, "").toLowerCase(), method);
                    }
                }
                cache.putIfAbsent(cla,map);
            }
            Method[] methods1 = cls.getMethods();
            for (Method method : methods1) {
                String field;
                if (method.getName().startsWith(IS)){
                    field = method.getName().replaceFirst(IS, "").toLowerCase();
                }else {
                    field = method.getName().replaceFirst(GET, "").toLowerCase();
                }
                if (map.containsKey(field) && map.get(field).getParameterTypes()[0].equals(method.getReturnType())) {
                    map.get(field).invoke(v, method.invoke(t));
                }
            }
        } catch ( IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
