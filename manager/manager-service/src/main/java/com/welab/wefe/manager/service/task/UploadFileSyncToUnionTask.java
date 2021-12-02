package com.welab.wefe.manager.service.task;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.http.HttpContentType;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.common.util.UrlUtil;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public class UploadFileSyncToUnionTask extends Thread {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private String baseUrl;
    private String api;
    private JObject params;
    private MultiValueMap<String, MultipartFile> files;

    public UploadFileSyncToUnionTask(String baseUrl, String api, JObject params, MultiValueMap<String, MultipartFile> files) {
        this.baseUrl = baseUrl;
        this.params = params;
        this.api = api;
        this.files = files;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 3; i++) {
            long retryInterval = 300 * i;
            HttpResponse response;
            String url = baseUrl + "/" + api;

            url = UrlUtil.appendQueryParameters(url, params);
            HttpRequest request = HttpRequest
                    .create(url)
                    .setContentType(HttpContentType.MULTIPART);

            for (Map.Entry<String, MultipartFile> item : files.toSingleValueMap().entrySet()) {
                try {
                    MultipartFile file = item.getValue();
                    ContentType contentType = StringUtil.isEmpty(file.getContentType())
                            ? ContentType.DEFAULT_BINARY
                            : ContentType.create(file.getContentType());

                    InputStreamBody streamBody = new InputStreamBody(
                            file.getInputStream(),
                            contentType,
                            file.getOriginalFilename()
                    );


                    request.appendParameter(item.getKey(), streamBody);
                } catch (IOException e) {
                    LOG.error("File read / write failed", e);
                }
            }

            response = request.post();


            if (!response.success()) {
                LOG.error("UploadFileSyncToUnion error,union response fail," + response.getMessage());
                ThreadUtil.sleep(retryInterval);
                continue;
            }

            JSONObject json;
            try {
                json = response.getBodyAsJson();
            } catch (JSONException e) {
                LOG.error("UploadFileSyncToUnion error,union response fail,", e);
                ThreadUtil.sleep(retryInterval);
                continue;
            }


            Integer code = json.getInteger("code");
            if (code == null || !code.equals(0)) {
                LOG.error("UploadFileSyncToUnion error,union response fail:" + json.toJSONString());
                ThreadUtil.sleep(retryInterval);
                continue;
            }
            break;
        }
    }
}
