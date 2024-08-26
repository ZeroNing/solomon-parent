package com.steven.solomon.execute;

import com.steven.solomon.annotation.Column;
import com.steven.solomon.config.profile.SqlProfile;
import com.steven.solomon.convert.ColumnConvert;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.temple.SqlTemple;
import com.steven.solomon.verification.ValidateUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 实体类数据库转换结果集
 */
public class ClazzExecuteSql extends AbstractExecuteSql {

    public ClazzExecuteSql(SqlTemple temple) {
        super(temple);
    }

    @Override
    protected List<Object> execute(Class<?> clazz, ResultSetMetaData metaData, ResultSet resultSet, int columnCount) throws Exception {
        List<Object> list = new ArrayList<>();
        Constructor<?> constructor = clazz.getConstructor();
        Map<String,Field> fieldMap = new HashMap<>();
        for(Field field : clazz.getDeclaredFields() ){
            Column column = field.getAnnotation(Column.class);
            if(ValidateUtils.isNotEmpty(column) && ValidateUtils.isNotEmpty(column.name())){
                fieldMap.put(column.name(), field);
            } else {
                fieldMap.put(Lambda.toSnakeCase(field.getName(),ValidateUtils.isNotEmpty(column) ? column.regex() : "_"), field);
            }
        }
        while (resultSet.next()) {
            Object obj = constructor.newInstance();
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
                //获取Class内的字段
                Field field = fieldMap.get(objectKey);
                if(ValidateUtils.isEmpty(field)){
                    logger.info("{}在{}的Class对象中不存在",objectKey,clazz.getName());
                    continue;
                }
                field.setAccessible(true);
                field.set(obj, ValidateUtils.isNotEmpty(convert) ? convert.convert(value) : value);
            }
            list.add(obj);
        }
        return list;
    }
}
