package com.steven.solomon.temple;

import com.steven.solomon.convert.ColumnConvert;
import com.steven.solomon.enums.ConvertEnums;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class SqlTemple {

    /**
     * 数据源
     */
    private DataSource dataSource;

    /**
     * 转换器
     */
    private Map<Integer,ColumnConvert<?>> convertMap;

    public SqlTemple() {
        super();
        this.convertMap = new HashMap<>();
        for(ConvertEnums convertEnum : ConvertEnums.values()){
            convertMap.put(convertEnum.getColumnType(),convertEnum.getConvert());
        }
    }

    public SqlTemple(DataSource dataSource) {
        this();
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setConvertMap(Integer columnType,ColumnConvert<?> columnConvert) {
        convertMap.put(columnType,columnConvert);
    }

    public Map<Integer, ColumnConvert<?>> getConvertMap() {
        return convertMap;
    }

    public ColumnConvert<?> getConvert(Integer clazzType) {
        return convertMap.get(clazzType);
    }
}
