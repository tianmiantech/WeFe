package com.welab.wefe.union.service.dto.base;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/9/27
 */
public class PageInput extends BaseInput{
    public Integer pageSize = 10;
    private Integer pageIndex = 0;

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }
}
