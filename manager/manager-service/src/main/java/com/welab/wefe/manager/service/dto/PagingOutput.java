package com.welab.wefe.manager.service.dto;

import com.welab.wefe.common.web.dto.AbstractApiOutput;

import java.util.List;

/**
 * @Author Jervis
 * @Date 2020-05-27
 **/
public class PagingOutput<T> extends AbstractApiOutput {

    private int totalPage;
    private long total;
    private List<T> list;

    public PagingOutput(long total, List<T> list) {
        setTotal(total);
        setList(list);
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
