package com.etammag.ipagehelper;

public class IPage implements com.github.pagehelper.IPage {

    private Integer pageNum;
    private Integer pageSize;
    private String orderBy;

    public IPage() {
    }

    public IPage(Integer pageNum, Integer pageSize, String orderBy) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.orderBy = orderBy;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
