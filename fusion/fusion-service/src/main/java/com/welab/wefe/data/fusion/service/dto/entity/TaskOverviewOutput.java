package com.welab.wefe.data.fusion.service.dto.entity;

/**
 * @author hunter.zhao
 */
public class TaskOverviewOutput {
    private Long allCount;

    private Long promoterCount;

    private Long providerCount;

    private Long pendingCount;

    private Long runningCount;


    public static TaskOverviewOutput of(Long allCount,
                                        Long promoterCount,
                                        Long providerCount,
                                        Long pendingCount,
                                        Long runningCount) {
        TaskOverviewOutput output = new TaskOverviewOutput();
        output.allCount=allCount;
        output.promoterCount=promoterCount;
        output.providerCount=providerCount;
        output.pendingCount=pendingCount;
        output.runningCount=runningCount;

        return output;
    }

    public Long getAllCount() {
        return allCount;
    }

    public void setAllCount(Long allCount) {
        this.allCount = allCount;
    }

    public Long getPromoterCount() {
        return promoterCount;
    }

    public void setPromoterCount(Long promoterCount) {
        this.promoterCount = promoterCount;
    }

    public Long getProviderCount() {
        return providerCount;
    }

    public void setProviderCount(Long providerCount) {
        this.providerCount = providerCount;
    }

    public Long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Long getRunningCount() {
        return runningCount;
    }

    public void setRunningCount(Long runningCount) {
        this.runningCount = runningCount;
    }
}
