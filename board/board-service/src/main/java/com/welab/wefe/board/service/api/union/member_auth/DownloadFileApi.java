package com.welab.wefe.board.service.api.union.member_auth;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.util.UrlUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/3
 */
@Api(path = "union/download/file", name = "upload file")
public class DownloadFileApi extends AbstractApi<DownloadFileApi.Input, ResponseEntity<byte[]>> {
    @Autowired
    private Config config;

    @Override
    protected ApiResult<ResponseEntity<byte[]>> handle(DownloadFileApi.Input input) throws StatusCodeWithException, IOException {
        String url = config.getUnionBaseUrl() + "/download/file";

        JObject params = JObject.create();
        String data = JObject.create("file_id", input.fileId).toJSONString();
        String sign;
        try {
            sign = RSAUtil.sign(data, CacheObjects.getRsaPrivateKey(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
        params.put("member_id", CacheObjects.getMemberId());
        params.put("sign", sign);
        params.put("data", data);

        url = UrlUtil.appendQueryParameters(url, params);
        RequestEntity requestEntity = new RequestEntity<>(null, null, HttpMethod.GET, UrlUtil.createUri(url));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.exchange(requestEntity, byte[].class);
        return success(response);
    }

    public static class Input extends AbstractApiInput {
        public String fileId;
    }
}
