package com.welab.wefe.serving.service.api.apirequestrecord;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ApiRequestRecordService;
import org.springframework.beans.factory.annotation.Autowired;



@Api(path = "apirequestrecord/save", name = "save api request record",login = false)
public class SaveApi extends AbstractNoneOutputApi<SaveApi.Input> {

    @Autowired
    private ApiRequestRecordService apiRequestRecordService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {

        apiRequestRecordService.save(input.getServiceId(), input.getClientId(), input.getSpend(), input.getIpAdd(), input.getRequestResult());
        return success();
    }


    public static class Input extends AbstractApiInput{

        @Check(name = "service_id")
        private String serviceId;

        @Check(name = "client_id")
        private String clientId;

        /**
         * 请求地址
         */
        @Check(name = "ip_add")
        private String ipAdd;

        /**
         * 耗时
         */
        private Long spend;

        /**
         * 请求结果：1 成功、0 失败
         */
        @Check(name = "request_result")
        private Integer requestResult;

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getIpAdd() {
            return ipAdd;
        }

        public void setIpAdd(String ipAdd) {
            this.ipAdd = ipAdd;
        }

        public Long getSpend() {
            return spend;
        }

        public void setSpend(Long spend) {
            this.spend = spend;
        }

        public Integer getRequestResult() {
            return requestResult;
        }

        public void setRequestResult(Integer requestResult) {
            this.requestResult = requestResult;
        }
    }
}
