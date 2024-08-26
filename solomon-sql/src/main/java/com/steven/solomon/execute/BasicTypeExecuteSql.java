package com.steven.solomon.execute;

import com.steven.solomon.convert.ColumnConvert;
import com.steven.solomon.temple.SqlTemple;
import com.steven.solomon.verification.ValidateUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 八大基础类型数据库转换结果集
 */

public class BasicTypeExecuteSql extends AbstractExecuteSql {

    protected BasicTypeExecuteSql(SqlTemple temple) {
        super(temple);
    }

    @Override
    protected List<Object> execute(Class<?> clazz, ResultSetMetaData metaData, ResultSet resultSet, int columnCount) throws Exception {
        List<Object> list = new ArrayList<>();
        while (resultSet.next()) {
            for(int i = 1; i <= columnCount; i++){
                //字段别名
                String columnLabelName = metaData.getColumnLabel(i);
                //获取字段的key
                String objectKey = ValidateUtils.isEmpty(columnLabelName) ? metaData.getColumnName(i) : columnLabelName;
                //获取字段ClassName
                String columnClassName = metaData.getColumnClassName(i);
                //获取转换器
                ColumnConvert<?> convert = temple.getConvert(columnClassName);
                //获取数据库值
                Object value = resultSet.getObject(objectKey);
                //设置值
                list.add(ValidateUtils.isNotEmpty(convert) ? convert.convert(value) : value);
            }
        }
        return list;
    }
}
