package com.welab.wefe.serving.service.api.clientservice;

import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ClientServiceService;

@Api(path = "clientservice/service_url_test", name = "service url test")
public class ServiceUrlTestApi extends AbstractApi<ServiceUrlTestApi.Input, ServiceUrlTestApi.Output> {

    @Autowired
    private ClientServiceService clientServiceService;

    @Override
    protected ApiResult<Output> handle(Input input) {
        try {
            int code = clientServiceService.serviceUrlTest(input);
            Output output = new Output();
            output.setCode(code);
            return success(output);
        } catch (ClientProtocolException e) {
            return fail(-1, "url非法");
        } catch (Exception e) {
            return fail(e);
        }
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "url", require = true)
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

    }

    public static class Output extends AbstractApiOutput {
        private int code;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

    }

}
