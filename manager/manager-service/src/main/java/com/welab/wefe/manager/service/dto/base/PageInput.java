package com.welab.wefe.manager.service.dto.base;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/19
 */
public class PageInput extends BaseInput{
    private int pageIndex = 0;
    private int pageSize = 20;

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
