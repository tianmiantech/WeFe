package com.welab.wefe.common.data.mongodb.entity.common;

import com.welab.wefe.common.data.mongodb.constant.FlowLimitStrategyTypeEnum;
import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.AbstractNormalMongoModel;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author aaron.li
 * @date 2021/10/22 17:16
 **/
@Document(collection = MongodbTable.Common.FLOW_LIMIT)
public class FlowLimit extends AbstractNormalMongoModel {

    /**
     * The access record key must be unique
     */
    private String key;
    /**
     * Number of visits
     */
    private int count;
    /**
     * flow limit strategy type
     */
    private FlowLimitStrategyTypeEnum strategyType;
    /**
     * flow limit strategy value
     */
    private String strategyValue;
    /**
     * Access timing time in milliseconds
     */
    private long startVisitTime;
    /**
     * Last access time in milliseconds
     */
    private long latestVisitTime;
    /**
     * The active time is the effective time. If it exceeds this time, it can be judged as an inactive record,
     * which can be cleared from the database
     */
    private long activeTime;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getStartVisitTime() {
        return startVisitTime;
    }

    public void setStartVisitTime(long startVisitTime) {
        this.startVisitTime = startVisitTime;
    }

    public long getLatestVisitTime() {
        return latestVisitTime;
    }

    public void setLatestVisitTime(long latestVisitTime) {
        this.latestVisitTime = latestVisitTime;
    }

    public FlowLimitStrategyTypeEnum getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(FlowLimitStrategyTypeEnum strategyType) {
        this.strategyType = strategyType;
    }

    public long getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(long activeTime) {
        this.activeTime = activeTime;
    }

    public String getStrategyValue() {
        return strategyValue;
    }

    public void setStrategyValue(String strategyValue) {
        this.strategyValue = strategyValue;
    }
}
