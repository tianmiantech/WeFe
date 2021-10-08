/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.gateway.base;

import io.grpc.BindableService;
import io.grpc.ServerInterceptor;

import java.util.*;

/**
 * RPC annotation entity class
 *
 * @author aaron.li
 **/
public class RpcServerAnnotate {
    /**
     * All entity objects marked with @RpcServer annotation（Key：Full path of entity class marked with @Rpcserver annotation,
     * Value：Mark the entity class object annotated by @RpcServer）
     */
    public static Map<String, RpcServerAnnotate> RPC_SERVER_MAP = new HashMap<>(16);

    /**
     * Mark the entity class object annotated by @RpcServer
     */
    private BindableService rpcBean;

    /**
     * Full path of entity class marked with @Rpcserver annotation
     */
    private String classFullName;
    /**
     * Interceptor list
     */
    private List<Class<? extends ServerInterceptor>> interceptors = new ArrayList<>();
    /**
     * Method name intercepted by interceptor; If empty, all methods of the entity class are intercepte
     */
    private List<String> interceptMethods;


    /**
     * Explain and add to constants
     */
    public static void addAnnotate(Object rpcBean, Class<?> rpcClass) {
        RpcServer rpcServerAnnotation = rpcClass.getAnnotation(RpcServer.class);
        // Since the grpc interceptor can only get the parent class path and has a name without grpc suffix, the grpc suffix is removed
        String superFullName = rpcClass.getSuperclass().getName().split("\\$")[0];
        superFullName = superFullName.substring(0, superFullName.length() - 4);

        Class<? extends ServerInterceptor>[] interceptorClassList = rpcServerAnnotation.interceptors();
        String[] interceptMethods = rpcServerAnnotation.interceptMethods();
        // Create annotation entities and add to constants
        RpcServerAnnotate rpcServerAnnotate = new RpcServerAnnotate();
        rpcServerAnnotate.setRpcBean((BindableService) rpcBean);
        rpcServerAnnotate.setClassFullName(superFullName);
        rpcServerAnnotate.interceptors.addAll(Arrays.asList(interceptorClassList));
        rpcServerAnnotate.setInterceptMethods(new ArrayList<>(Arrays.asList(interceptMethods)));

        RpcServerAnnotate.RPC_SERVER_MAP.put(superFullName, rpcServerAnnotate);
    }

    public String getClassFullName() {
        return classFullName;
    }

    public void setClassFullName(String classFullName) {
        this.classFullName = classFullName;
    }


    public List<String> getInterceptMethods() {
        return interceptMethods;
    }

    public void setInterceptMethods(List<String> interceptMethods) {
        this.interceptMethods = interceptMethods;
    }

    public BindableService getRpcBean() {
        return rpcBean;
    }

    public void setRpcBean(BindableService rpcBean) {
        this.rpcBean = rpcBean;
    }

    public List<Class<? extends ServerInterceptor>> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<Class<? extends ServerInterceptor>> interceptors) {
        this.interceptors = interceptors;
    }
}


