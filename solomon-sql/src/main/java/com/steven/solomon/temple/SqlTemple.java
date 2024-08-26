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
    private Map<String,ColumnConvert<?>> convertMap;

    public SqlTemple() {
        super();
        this.convertMap = new HashMap<>();
        for(ConvertEnums convertEnum : ConvertEnums.values()){
            convertMap.put(convertEnum.getClazz().getName(),convertEnum.getConvert());
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

    public void setConvertMap(Class<?> clazz,ColumnConvert<?> columnConvert) {
        convertMap.put(clazz.getName(),columnConvert);
    }

    public Map<String, ColumnConvert<?>> getConvertMap() {
        return convertMap;
    }

    public ColumnConvert getConvert(String clazzName) {
        return convertMap.get(clazzName);
    }
}
