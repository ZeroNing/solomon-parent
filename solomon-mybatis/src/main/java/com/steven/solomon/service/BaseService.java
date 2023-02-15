package com.steven.solomon.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.steven.solomon.verification.ValidateUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BaseService<T> extends IService<T> {

    @Override
    default List<T> listByIds(Collection<? extends Serializable> idList) {
        if(ValidateUtils.isEmpty(idList)){
            return null;
        }
        return getBaseMapper().selectBatchIds(idList);
    }

    /**
     * 查询（根据 columnMap 条件）
     *
     * @param columnMap 表字段 map 对象
     */
    @Override
    default List<T> listByMap(Map<String, Object> columnMap) {
        if(ValidateUtils.isEmpty(columnMap)){
            return null;
        }
        return getBaseMapper().selectByMap(columnMap);
    }

    /**
     * 根据 Wrapper 条件，查询总记录数
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    @Override
    default long count(Wrapper<T> queryWrapper) {
        if(queryWrapper.isEmptyOfWhere()){
            return 0;
        }
        return SqlHelper.retCount(getBaseMapper().selectCount(queryWrapper));
    }

    /**
     * 查询列表
     *
     * @param queryWrapper 实体对象封装操作类 {@link com.baomidou.mybatisplus.core.conditions.query.QueryWrapper}
     */
    @Override
    default List<T> list(Wrapper<T> queryWrapper) {
        if(queryWrapper.isEmptyOfWhere()){
            return null;
        }
        return getBaseMapper().selectList(queryWrapper);
    }
}
