package com.etammag.ipagehelper;

public class IPageExample<T> extends IPage{
    private T example;

    public IPageExample(Integer pageNum, Integer pageSize, String orderBy, T example) {
        super(pageNum, pageSize, orderBy);
        this.example = example;
    }

    public T getExample() {
        return example;
    }

    public void setExample(T example) {
        this.example = example;
    }
}
