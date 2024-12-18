package com.steven.solomon.dao;

import cn.hutool.json.JSONUtil;
import com.steven.solomon.config.profile.SqlProfile;
import com.steven.solomon.execute.ClazzExecuteSql;
import com.steven.solomon.temple.SqlTemple;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.lang.reflect.ParameterizedType;

public class BaseDao<T> {

    private final SqlTemple temple;

    protected Class<T> modelClass = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];

    public BaseDao(SqlTemple temple) {
        this.temple = temple;
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

        ClazzExecuteSql a = new ClazzExecuteSql(new SqlTemple(new HikariDataSource(config)));
//        System.out.println(FastJsonUtils.formatJsonByFilter(a.executeQuery("select * from admin order by name desc",null,Admin.class,true)));
    }
}
