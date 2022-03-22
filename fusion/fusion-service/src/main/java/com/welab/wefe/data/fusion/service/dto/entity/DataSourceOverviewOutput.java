package com.welab.wefe.data.fusion.service.dto.entity;

/**
 * @author hunter.zhao
 */
public class DataSourceOverviewOutput {
    private Long dataSetCount;

    private Long bloomFilterCount;



    public static DataSourceOverviewOutput of(Long dataSetCount,
                                              Long bloomFilterCount) {
        DataSourceOverviewOutput output = new DataSourceOverviewOutput();
        output.dataSetCount=dataSetCount;
        output.bloomFilterCount=bloomFilterCount;

        return output;
    }


    public Long getDataSetCount() {
        return dataSetCount;
    }

    public void setDataSetCount(Long dataSetCount) {
        this.dataSetCount = dataSetCount;
    }

    public Long getBloomFilterCount() {
        return bloomFilterCount;
    }

    public void setBloomFilterCount(Long bloomFilterCount) {
        this.bloomFilterCount = bloomFilterCount;
    }
}
