package com.welab.wefe.board.service.api.union;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.common.exception.StatusCodeWithException;
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
        String url = config.getUNION_BASE_URL() + "/download/file?file_id=" + input.fileId;
        RequestEntity requestEntity = new RequestEntity<>(null, null, HttpMethod.GET, UrlUtil.createUri(url));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restTemplate.exchange(requestEntity, byte[].class);
        return success(response);
    }

    public static class Input extends AbstractApiInput {
        public String fileId;
    }
}
