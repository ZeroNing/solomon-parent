package com.steven.solomon.dao;

import com.steven.solomon.convert.ColumnConvert;
import com.steven.solomon.temple.SqlTemple;
import com.steven.solomon.verification.ValidateUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseDao<T> {

    private final SqlTemple temple;

    protected Class<T> modelClass = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    public BaseDao(SqlTemple temple) {
        this.temple = temple;
    }

//    public Object execute(String sql, Class clazz) throws Exception {
//        ResultSet resultSet = getResultSet(sql);
//        if(!resultSet.next()){
//            return new Object();
//        }
//        //获取sql里的字段以及别名
//        ResultSetMetaData metaData = resultSet.getMetaData();
//        if(ClassUtils.isPrimitiveOrWrapper(clazz) || clazz == BigDecimal.class){
//            ColumnConvert convert = temple.getConvert(clazz);
//            if(ValidateUtils.isNotEmpty(convert)){
//                while (resultSet.next()) {
//
//                }
//            }
//        }
//    }

    public Object executeMap(String sql) throws Exception {

        Connection connection = null;
        try {
            connection = temple.getDataSource().getConnection();
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            Map<String,Object> map = new HashMap<>();
            //获取sql里的字段以及别名
            ResultSetMetaData metaData = resultSet.getMetaData();
            //获取有多少个字段
            int columnCount = metaData.getColumnCount();
            List<Map<String,Object>> list = new ArrayList<>();

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
                    map.put(objectKey, ValidateUtils.isNotEmpty(convert) ? convert.convert(value) : value);
                }
                list.add(map);
            }
            return list;
        } finally {
            if(ValidateUtils.isNotEmpty(connection)){
                connection.close();
            }
        }
    }

    private ResultSet getResultSet(String sql) throws Exception {
        Connection connection = null;
        try {
            connection = temple.getDataSource().getConnection();
            return connection.createStatement().executeQuery(sql);
        } finally {
            if(ValidateUtils.isNotEmpty(connection)){
                connection.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/admin");
        config.setUsername("root");
        config.setPassword("root");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        HikariDataSource dataSource =  new HikariDataSource(config);
        SqlTemple temple = new SqlTemple(dataSource);
        TestDao dao = new TestDao(temple);
        System.out.println(dao.executeMap("select * from admin order by id desc"));
    }
}
