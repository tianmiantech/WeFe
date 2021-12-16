package com.welab.wefe.serving.service.api.client;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;

/**
 * @author ivenn.zheng
 */
@Api(path = "client/query-list", name = "get client list")
public class QueryClientListApi extends AbstractApi<QueryClientListApi.Input, PagingOutput<QueryClientListApi.Output>> {

    @Autowired
    private ClientService clientService;

    @Override
    protected ApiResult<PagingOutput<Output>> handle(Input input) throws StatusCodeWithException, IOException {
        return success(clientService.queryList(input));
    }

    public static class Input extends PagingInput {

        @Check(name = "由谁创建")
        private String createdBy;

        @Check(name = "开始时间")
        private Date startTime;

        @Check(name = "结束时间")
        private Date endTime;

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }

        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }
    }

    public static class Output extends AbstractApiOutput {

        @Check(name = "客户id")
        private String id;

        @Check(name = "客户名称")
        private String name;

        @Check(name = "客户邮箱")
        private String email;

        @Check(name = "ip 地址")
        private String ipAdd;

        @Check(name = "公钥")
        private String pubKey;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

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
    }

}
