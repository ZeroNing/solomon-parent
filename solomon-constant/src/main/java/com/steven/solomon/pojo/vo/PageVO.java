package com.steven.solomon.pojo.vo;

import java.io.Serializable;
import java.util.List;

public class PageVO<T> implements Serializable {
    /**
     * 总页数
     */
    private long total;

    /**
     * 第几页
     */
    private int pageNo;

    /**
     * 页数
     */
    private int pageSize;

    /**
     * 是否有下一页
     */
    private boolean isNext;
    /**
     * 数据
     */
    private List<T> data;

    public PageVO(List<T> data, long total, int pageNo, int pageSize) {
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.data = data;
        this.isNext = (pageNo * pageSize) < total;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isNext() {
        return isNext;
    }

    public void setNext(boolean next) {
        isNext = next;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
