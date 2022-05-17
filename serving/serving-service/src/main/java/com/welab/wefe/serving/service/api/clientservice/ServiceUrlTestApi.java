package com.welab.wefe.serving.service.api.clientservice;

import org.springframework.beans.factory.annotation.Autowired;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ClientServiceService;

@Api(path = "clientservice/service_url_test", name = "service url test")
public class ServiceUrlTestApi extends AbstractApi<ServiceUrlTestApi.Input, ServiceUrlTestApi.Output> {

    @Autowired
    private ClientServiceService clientServiceService;

    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {
        clientServiceService.serviceUrlTest(input);
        return success();
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

    public static class Output {

    }

}
