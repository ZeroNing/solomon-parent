package com.steven.solomon.pojo.param;

import com.steven.solomon.pojo.enums.OrderByEnum;
import com.steven.solomon.verification.ValidateUtils;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通用分页参数。
 *
 * <p>分页参数默认第一页、每页 10 条；排序字段会统一过滤空值，避免拼接出非法 SQL 片段。</p>
 */
public class BasePageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码。
     */
    private int pageNo = 1;

    /**
     * 每页条数。
     */
    private int pageSize = 10;

    /**
     * 是否分页，true 表示分页，false 表示不分页。
     */
    private boolean isPage = true;

    /**
     * 排序字段列表。
     */
    private List<Sort> sorted;

    /**
     * 单个排序规则。
     */
    public static class Sort implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 排序字段名。
         */
        private String orderByField;

        /**
         * 排序方向，默认倒序。
         */
        private OrderByEnum orderByMethod = OrderByEnum.DESCEND;

        public Sort(String orderByField, OrderByEnum orderByMethod) {
            this.orderByField = orderByField;
            this.orderByMethod = ValidateUtils.getOrDefault(orderByMethod, OrderByEnum.DESCEND);
        }

        public String getSort() {
            if (ValidateUtils.isEmpty(orderByField)) {
                return "";
            }
            return orderByField + " " + ValidateUtils.getOrDefault(orderByMethod, OrderByEnum.DESCEND).label();
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
            this.orderByMethod = ValidateUtils.getOrDefault(orderByMethod, OrderByEnum.DESCEND);
        }
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = Math.max(pageNo, 1);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(pageSize, 1);
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

    /**
     * 获取 SQL 排序片段。
     *
     * <p>空排序规则会被过滤，避免历史实现中全部为空时 substring 越界。</p>
     */
    public String getSort() {
        if (ValidateUtils.isEmpty(sorted)) {
            return "";
        }
        return sorted.stream()
            .filter(ValidateUtils::isNotEmpty)
            .map(Sort::getSort)
            .filter(ValidateUtils::isNotEmpty)
            .collect(Collectors.joining(","));
    }
}
