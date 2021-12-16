package com.welab.wefe.serving.service.api.client;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Column;
import java.io.IOException;

/**
 * @author ivenn.zheng
 */
@Api(path = "client/query-one", name = "get client")
public class QueryClientApi extends AbstractApi<QueryClientApi.Input,QueryClientApi.Output> {

    @Autowired
    private ClientService clientService;


    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException, IOException {
        return success((clientService.queryById(input.getId())));
    }

    public static class Input extends AbstractApiInput {


        @Check(name = "client id")
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }


    public static class Output {
        /**
         * name
         */
        private String name;

        /**
         * email
         */
        private String email;

        /**
         * ip_add
         */
        private String ipAdd;

        /**
         * ip_add
         */
        private String pubKey;

        /**
         * remark
         */
        private String remark;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getIpAdd() {
            return ipAdd;
        }

        public void setIpAdd(String ipAdd) {
            this.ipAdd = ipAdd;
        }

        public String getPubKey() {
            return pubKey;
        }

        public void setPubKey(String pubKey) {
            this.pubKey = pubKey;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
