package com.steven.solomon.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.steven.solomon.date.DateTimeUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author starrysky
 * @title: MyMataHandler
 * @projectName mybaits_plus_final
 * @description: Meta处理器，当对应pojo的字段上设置@TableField值时，
 * 触发了inster/update时间都会更新createTime或者updateTime两个字段
 * @date 2021/1/3123:28
 */

@Component
public class MyMataHandler implements MetaObjectHandler {

  @Override
  public void insertFill(MetaObject metaObject) {
    this.setFieldValByName("create_date", DateTimeUtils.getLocalDateTime(), metaObject);
    this.setFieldValByName("update_date", DateTimeUtils.getLocalDateTime(), metaObject);
  }

  @Override
  public void updateFill(MetaObject metaObject) {
    this.setFieldValByName("update_date", DateTimeUtils.getLocalDateTime(), metaObject);
  }

  /**
   * 逻辑删除插件
   */
  @Bean
  public ISqlInjector injector(){
    return new DefaultSqlInjector();
  }
}