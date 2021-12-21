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

package com.welab.wefe.gateway.util;

import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.base.Processor;
import com.welab.wefe.gateway.base.ProcessorAnnotate;
import com.welab.wefe.gateway.base.RpcServer;
import com.welab.wefe.gateway.base.RpcServerAnnotate;

import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Class tools
 *
 * @author aaron.li
 **/
public class ClassUtil {
    /**
     * Grpc service basic class name
     */
    private static final String RPC_SERVERS_INTERFACE_SIMPLE_NAME = "BindableService";
    /**
     * Java basic object class name
     */
    private static final String OBJECT_CLASS_SIMPLE_NAME = "Object";
    /**
     * Processor base parent class name
     */
    private static final String ABSTRACT_PROCESSOR_SIMPLE_NAME = "AbstractProcessor";


    /**
     * Load all processor classes marked with @RpcServer annotation
     * <p>
     * Return value structure description:
     * Key：Full path of grpc service class
     * Value：Grpc service class annotation configuration information
     * </p>
     *
     * @return All grpc service class information
     */
    public static Map<String, RpcServerAnnotate> loadRpcClassBeans() {
        String[] beanNames = GatewayServer.CONTEXT.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = GatewayServer.CONTEXT.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            if (isRpcServer(beanClass)) {
                // Load into constant and save
                RpcServerAnnotate.addAnnotate(bean, beanClass);
            }
        }
        return RpcServerAnnotate.RPC_SERVER_MAP;
    }


    /**
     * Load all processor classes marked with @processor annotation
     *
     * <p>
     * Return value structure description:
     * Key：Processor annotation attribute name value
     * Value：Processor annotation class annotation configuration information
     * </p>
     *
     * @return All Processor class information
     */
    public static Map<String, ProcessorAnnotate> loadProcessorClass() {
        String[] beanNames = GatewayServer.CONTEXT.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = GatewayServer.CONTEXT.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            if (isProcessor(beanClass)) {
                // Load into constant and save
                ProcessorAnnotate.addAnnotate(bean);
            }
        }

        return ProcessorAnnotate.PROCESSOR_MAP;
    }


    /**
     * Determine whether a class is a grpc service class
     *
     * @param c Class to be judged
     * @return true：yes；false：no
     */
    private static boolean isRpcServer(Class<?> c) {
        if (null == c || !Modifier.isPublic(c.getModifiers())) {
            return false;
        }

        RpcServer rpcServerAnnotation = c.getAnnotation(RpcServer.class);
        if (null == rpcServerAnnotation) {
            return false;
        }

        Class<?> currentClass = c;
        while (null != currentClass) {
            if (isImplementRpcService(currentClass)) {
                return true;
            }
            String classSimpleName = currentClass.getSimpleName();
            if (OBJECT_CLASS_SIMPLE_NAME.equals(classSimpleName)) {
                return false;
            }
            currentClass = currentClass.getSuperclass();
        }
        return false;
    }


    /**
     * Judge whether the class implements grpc service interface
     */
    private static boolean isImplementRpcService(Class<?> c) {
        Class<?>[] interfaces = c.getInterfaces();
        for (Class<?> itf : interfaces) {
            if (RPC_SERVERS_INTERFACE_SIMPLE_NAME.equals(itf.getSimpleName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Judge whether this class is a processor class
     *
     * @param c Class to be judged
     * @return true：yes；false：no
     */
    private static boolean isProcessor(Class<?> c) {
        if (null == c || !Modifier.isPublic(c.getModifiers())) {
            return false;
        }

        Processor processorAnnotation = c.getAnnotation(Processor.class);
        if (null == processorAnnotation) {
            return false;
        }

        Class<?> currentClass = c;
        while (null != currentClass) {
            if (isProcessorClass(currentClass)) {
                return true;
            }
            if (OBJECT_CLASS_SIMPLE_NAME.equals(currentClass.getSimpleName())) {
                return false;
            }
            currentClass = currentClass.getSuperclass();
        }
        return false;
    }

    /**
     * Judge whether this class is AbstractProcessor class
     */
    private static boolean isProcessorClass(Class<?> c) {
        return ABSTRACT_PROCESSOR_SIMPLE_NAME.equals(c.getSimpleName());
    }

}
