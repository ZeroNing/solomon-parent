package com.steven.solomon.execute;

import com.steven.solomon.convert.ColumnConvert;
import com.steven.solomon.temple.SqlTemple;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Map;

public abstract class AbstractExecuteSql {

    protected final SqlTemple temple;

    protected final DataSource datasource;

    protected final Map<Integer,ColumnConvert<?>> columnConvertMap;

    protected final Logger logger = LoggerUtils.logger(getClass());

    protected AbstractExecuteSql(SqlTemple temple) {
        this.temple = temple;
        this.datasource = temple.getDataSource();
        this.columnConvertMap = temple.getConvertMap();
    }

    public Object executeQuery(String sql,List<Object> paramList,Class<?> clazz,boolean isList) throws Exception {
        try (Connection connection = temple.getDataSource().getConnection();PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            if(ValidateUtils.isNotEmpty(paramList)) {
                for(int index = 0; index < paramList.size(); index++) {
                    preparedStatement.setObject(index + 1, paramList.get(index));
                }
            }
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                // 获取sql里的字段以及别名
                ResultSetMetaData metaData = resultSet.getMetaData();
                // 获取有多少个字段
                int columnCount = metaData.getColumnCount();
                // 执行sql
                List<Object> result = executeQuery(clazz,metaData,resultSet,columnCount);
                return isList ? result : result.get(0);
            }
        } catch (Throwable e) {
            logger.error("sql执行异常",e);
            throw e;
        }
    }

    /**
     * 通用查询执行逻辑
     * @param clazz 需要转换的对象
     * @param metaData 获取sql里的字段以及别名
     * @param resultSet 结果集
     * @param columnCount 字段数
     */
    protected abstract List<Object> executeQuery(Class<?> clazz,ResultSetMetaData metaData,ResultSet resultSet, int columnCount) throws Exception;

}
