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

package com.welab.wefe.common.web.api.base;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Zane
 */
public abstract class AbstractApi<In extends AbstractApiInput, Out> {

    /**
     * The concurrency of the API, used to limit the concurrency of the API
     */
    private static final Map<String, LongAdder> API_PARALLELISM = new HashMap<>();

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());


    /**
     * Concrete implementation of API
     *
     * @param input
     * @return ApiResult<Out>
     * @throws StatusCodeWithException
     * @throws IOException
     */
    protected abstract ApiResult<Out> handle(In input) throws Exception;

    public ApiResult<Out> execute(String method, JSONObject requestParams) {
        return execute(method, requestParams, null, null);
    }

    public ApiResult<Out> execute(String method, JSONObject requestParams, HttpServletRequest request) {
        return execute(method, requestParams, request, null);
    }

    /**
     * To perform this API
     *
     * @param method        Request way：post/get/...
     * @param requestParams The ginseng
     * @param files         File list
     */
    public ApiResult<Out> execute(String method, JSONObject requestParams, HttpServletRequest request, MultiValueMap<String, MultipartFile> files) {

        String apiClassName = this.getClass().getSimpleName();

        // Checking concurrency Limits
        if (!checkParallelism(apiClassName)) {
            return fail("This api has reached the concurrency limit.");
        }

        LongAdder apiRunningCount = API_PARALLELISM.get(apiClassName);

        try {
            apiRunningCount.increment();

            // Create the input parameter object
            Class<In> apiInputClass = getInputClass(this.getClass());
            In apiInput = requestParams.toJavaObject(apiInputClass);
            apiInput.method = method.toUpperCase();
            apiInput.request = request;
            // The parameter checking
            apiInput.checkAndStandardize();

            // Add files
            if (apiInput instanceof AbstractWithFilesApiInput) {
                ((AbstractWithFilesApiInput) apiInput).files = files;
            }

            // Implement the API
            ApiResult<Out> result = handle(apiInput);
            if (result == null) {
                result = fail("null of api result");
            }
            return result;
        } catch (Exception e) {

            // When an API exception occurs, scheduled delegates are first used for hosting.
            if (Launcher.ON_API_EXCEPTION_FUNCTION != null) {
                ApiResult<?> result = null;
                try {
                    result = Launcher.ON_API_EXCEPTION_FUNCTION.accept(this, e);
                    return (ApiResult<Out>) result;
                } catch (Exception exception) {
                    e = exception;
                }
            }

            if (e instanceof StatusCodeWithException) {
                StatusCodeWithException e1 = (StatusCodeWithException) e;

                if (e1.getStatusCode() == StatusCode.PARAMETER_VALUE_INVALID) {
                    LOG.warn(e.getClass().getSimpleName() + " " + e.getMessage());
                } else {
                    LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                }

                return fail(e1.getStatusCode().getCode(), e1.getMessage());
            } else {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                return fail(e.getMessage());

            }
        } finally {
            apiRunningCount.decrement();
        }
    }

    /**
     * Gets the input parameter type of the current API
     */
    private Class<In> getInputClass(Class<?> clazz) {

        while (!(clazz.getGenericSuperclass() instanceof ParameterizedType)) {
            clazz = clazz.getSuperclass();
        }

        Type[] types = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
        if (types.length > 0) {
            Class<?> type = (Class<?>) types[0];
            if (AbstractApiInput.class.isAssignableFrom(type)) {
                return (Class<In>) type;
            }
        }

        return getInputClass(clazz.getSuperclass());
    }

    /**
     * Check the current concurrency of the API
     */
    private synchronized boolean checkParallelism(String apiClassName) {

        if (!API_PARALLELISM.containsKey(apiClassName)) {
            API_PARALLELISM.put(apiClassName, new LongAdder());
        }

        LongAdder longAdder = API_PARALLELISM.get(apiClassName);

        return canParallel() && longAdder.longValue() < parallelism();
    }

    /**
     * Maximum parallelism allowed by an interface
     */
    protected int parallelism() {
        return Integer.MAX_VALUE;
    }

    /**
     * Specifies whether the interface allows concurrency. The default value is yes.
     * <p>
     * Override this method in a subclass if changes are needed.
     */
    public boolean canParallel() {
        return true;
    }


    protected ApiResult<?> fail(StatusCodeWithException e, Object data) {
        ApiResult<Object> result = new ApiResult<>();
        result.code = e.getStatusCode().getCode();
        result.message = e.getMessage();
        result.data = data;
        return result;
    }

    protected ApiResult<Out> fail(String message) {
        return fail(-1, message, null);
    }

    protected ApiResult<Out> fail(StatusCode status) {
        return fail(status.getCode(), status.getMessage(), null);
    }

    public ApiResult<Out> fail(int code, String message) {
        return fail(code, message, null);
    }

    protected ApiResult<Out> fail(int code, String message, Out data) {
        ApiResult<Out> response = new ApiResult<>();
        response.code = code;
        response.message = message;
        response.data = data;
        return response;
    }

    protected ApiResult<Out> success(Out data) {
        ApiResult<Out> response = new ApiResult<>();
        response.data = data;
        return response;
    }

    protected ApiResult<Out> success() {
        return success(null);
    }

    /**
     * Wrap the union API return result as the board API return result.
     */
    protected ApiResult<JSONObject> unionApiResultToBoardApiResult(JSONObject json) {

        ApiResult<JSONObject> result = new ApiResult<>();
        result.code = json.getInteger("code");
        result.message = json.getString("message");
        result.data = json.getJSONObject("data");

        return result;
    }

}
