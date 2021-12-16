package com.welab.wefe.serving.service.api.requeststatistics;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.service.RequestStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;

/**
 * @author ivenn.zheng
 */
@Api(path = "requeststatistics/query-list", name = "query request statistics list")
public class QueryListApi extends AbstractApi<QueryListApi.Input, PagingOutput<QueryListApi.Output>> {


    @Autowired
    private RequestStatisticsService requestStatisticsService;

    @Override
    protected ApiResult<PagingOutput<Output>> handle(Input input) throws StatusCodeWithException, IOException {
        return success(requestStatisticsService.queryList(input));
    }


    public static class Input extends AbstractApiInput {

        /**
         * 开始时间
         */
        private Date startDate;

        /**
         * 结束时间
         */
        private Date endDate;

        /**
         * 服务名称
         */
        private String serviceName;

        /**
         * 客户名称
         */
        private String clientName;

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }
    }

    public static class Output extends AbstractApiOutput {

        /**
         * 总调用次数
         */
        private Integer totalRequestTimes;

        /**
         * 总失败次数
         */
        private Integer totalFailTimes;

        /**
         * 总成功次数
         */
        private Integer totalSuccessTimes;

        /**
         * 服务名称
         */
        private String serviceName;

        /**
         * 客户名称
         */
        private String clientName;

        /**
         * 服务类型
         */
        private Integer serviceType;

        /**
         * 单价
         */
        private Double unitPrice;


        public Integer getTotalRequestTimes() {
            return totalRequestTimes;
        }

        public void setTotalRequestTimes(Integer totalRequestTimes) {
            this.totalRequestTimes = totalRequestTimes;
        }

        public Integer getTotalFailTimes() {
            return totalFailTimes;
        }

        public void setTotalFailTimes(Integer totalFailTimes) {
            this.totalFailTimes = totalFailTimes;
        }

        public Integer getTotalSuccessTimes() {
            return totalSuccessTimes;
        }

        public void setTotalSuccessTimes(Integer totalSuccessTimes) {
            this.totalSuccessTimes = totalSuccessTimes;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public Integer getServiceType() {
            return serviceType;
        }

        public void setServiceType(Integer serviceType) {
            this.serviceType = serviceType;
        }

        public Double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }

    }
}
