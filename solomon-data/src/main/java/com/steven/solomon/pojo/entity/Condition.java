package com.steven.solomon.pojo.entity;

import com.steven.solomon.verification.ValidateUtils;
import java.util.Collection;

public class Condition extends BaseSql {

  private Condition() {}

  private Condition(String sql) {
    super(sql);
  }

  /**
   * 条件模糊(不包含)
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Condition notLike(String field,Object value){
    return notLike(field, value, false);
  }

  /**
   * 条件模糊(不包含)
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Condition notLike(String field,Object value,boolean isRequired){
    String sql = new StringBuilder(" ").append(field).append(" NOT LIKE '").append(value).append("'").toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Condition(sql);
  }

  /**
   * 条件不包含
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Condition notIn(String field,Collection value){
    return notIn(field, value, false);
  }

  /**
   * 条件不包含
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Condition notIn(String field, Collection value,boolean isRequired){
    if(ValidateUtils.isNotEmpty(value) && value.size() == 1){
      return eq(field,value.iterator().next(),isRequired);
    }

    String sql = new StringBuilder(" ").append(field).append(" NOT IN ").append(listHandler(value)).toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Condition(sql);
  }

  /**
   * 条件模糊（包含）
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Condition like(String field,Object value){
    return like(field, value, false);
  }

  /**
   * 条件模糊（包含）
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Condition like(String field,Object value,boolean isRequired){
    String sql = new StringBuilder(" ").append(field).append(" LIKE '").append(value).append("'").toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Condition(sql);
  }

  /**
   * 条件包含
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Condition in(String field,Collection value){
    return in(field, value, false);
  }

  /**
   * 条件包含
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Condition in(String field, Collection value,boolean isRequired){
    if(ValidateUtils.isNotEmpty(value) && value.size() == 1){
      return eq(field,value.iterator().next(),isRequired);
    }

    String sql = new StringBuilder(" ").append(field).append(" IN ").append(listHandler(value)).toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Condition(sql);
  }


  /**
   * 条件不相等
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Condition ne(String field,Object value,boolean isRequired){
    String sql = new StringBuilder(" ").append(field).append(" <> '").append(value).append("'").toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Condition(sql);
  }

  /**
   * 条件不相等
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Condition ne(String field,Object value){
    return ne(field, value, false);
  }

  /**
   * 条件相等
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Condition eq(String field,Object value,boolean isRequired){
    String sql = new StringBuilder(" ").append(field).append(" = '").append(value).append("'").toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Condition(sql);
  }

  public Condition and(Condition cond){
    if (ValidateUtils.isEmpty(cond) || ValidateUtils.isEmpty(cond.getSql())){
      return this;
    }
    this.sql.append(" AND ").append(cond.getSql());
    return this;
  }

  public Condition or(Condition cond){
    if (ValidateUtils.isEmpty(cond) || ValidateUtils.isEmpty(cond.getSql())){
      return this;
    }
    this.sql.append(" OR ").append(cond.getSql());
    return this;
  }

  /**
   * 条件相等
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Condition eq(String field,Object value){
    return eq(field, value, false);
  }

  public static String listHandler(Collection values){
    if(ValidateUtils.isEmpty(values)){
      return "()";
    }
    StringBuilder sql = new StringBuilder("(");
    for (Object value : values) {
      sql.append("'").append(value).append("',");
    }
    sql.append(")");
    sql.deleteCharAt(sql.lastIndexOf(","));
    return sql.toString();
  }

}
