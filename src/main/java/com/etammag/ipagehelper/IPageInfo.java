package com.etammag.ipagehelper;

import com.github.pagehelper.PageInfo;

import java.util.List;

public class IPageInfo<T> {
    private long total;
    private List<T> list;

    public IPageInfo() {
    }

    public IPageInfo(List<T> list) {
        PageInfo<T> pageInfo = new PageInfo<>(list);
        this.total = pageInfo.getTotal();
        this.list = pageInfo.getList();
    }

    public IPageInfo(PageInfo<T> pageInfo) {
        this.total = pageInfo.getTotal();
        this.list = pageInfo.getList();
    }


    public void setTotal(long total) {
        this.total = total;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public List<T> getList() {
        return list;
    }
}
