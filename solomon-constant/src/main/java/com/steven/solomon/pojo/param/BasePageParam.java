package com.steven.solomon.pojo.param;


import ch.qos.logback.core.util.StringCollectionUtil;
import com.steven.solomon.pojo.enums.OrderByEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class BasePageParam implements Serializable {

    /**
     * 第几页
     */
    private int pageNo = 1;

    /**
     * 页数
     */
    private int pageSize = 10;

    /**
     * 是否分页 true 分页 false 不分页
     */
    private boolean isPage = true;

    private List<Sort> sorted;

    public static class Sort implements Serializable{

        private String  orderByField;

        private OrderByEnum orderByMethod = OrderByEnum.DESCEND;

        public Sort(String  orderByField,OrderByEnum orderByMethod){
            this.orderByField = orderByField;
            this.orderByMethod = orderByMethod;
        }

        public String getSort(){
            if(orderByField == null || orderByField == ""){
                return "";
            }
            return orderByField + " " + orderByMethod.label();
        }

        public String getOrderByField() {
            return orderByField;
        }

        public void setOrderByField(String orderByField) {
            this.orderByField = orderByField;
        }

        public OrderByEnum getOrderByMethod() {
            return orderByMethod;
        }

        public void setOrderByMethod(OrderByEnum orderByMethod) {
            this.orderByMethod = orderByMethod;
        }
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

    public boolean isPage() {
        return isPage;
    }

    public void setPage(boolean page) {
        isPage = page;
    }

    public List<Sort> getSorted() {
        return sorted;
    }

    public void setSorted(List<Sort> sorted) {
        this.sorted = sorted;
    }

    public String getSort(){
        if(CollectionUtils.isEmpty(getSorted())){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(Sort sort : getSorted()){
            sb.append(sort.getSort()).append(",");
        }
        return sb.substring(0,sb.lastIndexOf(","));
    }

}
