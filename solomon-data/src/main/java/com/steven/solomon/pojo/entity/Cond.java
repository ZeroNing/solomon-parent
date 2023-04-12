package com.steven.solomon.pojo.entity;

import com.steven.solomon.verification.ValidateUtils;
import java.util.Collection;

public class Cond extends BaseSql {

  private Cond() {}

  private Cond(String sql) {
    super(sql);
  }

  /**
   * 条件范围
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Cond between(String field, Object value, Object value1){
    if(ValidateUtils.isEmpty(value) && ValidateUtils.isEmpty(value1)){
      return null;
    }
    if(ValidateUtils.isNotEmpty(value) && ValidateUtils.isEmpty(value1)){
      return ge(field,value);
    }
    if(ValidateUtils.isEmpty(value) && ValidateUtils.isNotEmpty(value1)){
      return le(field,value1);
    }

    String sql = new StringBuilder(" ").append(field).append(" BETWEEN '").append(value).append("' AND '").append(value1).append("'").toString();
    return new Cond(sql);
  }

  /**
   * 条件大于等于
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Cond le(String field, Object value){
    return le(field, value, false);
  }

  /**
   * 条件大于等于
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Cond le(String field, Object value, boolean isRequired){
    String sql = new StringBuilder(" ").append(field).append(" <= '").append(value).append("'").toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Cond(sql);
  }

  /**
   * 条件大于
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Cond lt(String field, Object value){
    return lt(field, value, false);
  }

  /**
   * 条件大于
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Cond lt(String field, Object value, boolean isRequired){
    String sql = new StringBuilder(" ").append(field).append(" < '").append(value).append("'").toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Cond(sql);
  }

  /**
   * 条件大于等于
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Cond ge(String field, Object value){
    return ge(field, value, false);
  }

  /**
   * 条件大于等于
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Cond ge(String field, Object value, boolean isRequired){
    String sql = new StringBuilder(" ").append(field).append(" >= '").append(value).append("'").toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Cond(sql);
  }

  /**
   * 条件大于
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Cond gt(String field, Object value){
    return gt(field, value, false);
  }

  /**
   * 条件大于
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Cond gt(String field, Object value, boolean isRequired){
    String sql = new StringBuilder(" ").append(field).append(" > '").append(value).append("'").toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Cond(sql);
  }

  /**
   * 条件不为空
   * @param field 字段
   * @return
   */
  public static Cond isNotNull(String field){
    if(ValidateUtils.isEmpty(field)){
      return null;
    }
    String sql = new StringBuilder(" ").append(field).append(" IS NOT NULL ").toString();
    return new Cond(sql);
  }

  /**
   * 条件为空
   * @param field 字段
   * @return
   */
  public static Cond isNull(String field){
    if(ValidateUtils.isEmpty(field)){
      return null;
    }
    String sql = new StringBuilder(" ").append(field).append(" IS NULL ").toString();
    return new Cond(sql);
  }

  /**
   * 条件模糊(不包含)
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Cond notLike(String field, Object value){
    return notLike(field, value, false);
  }

  /**
   * 条件模糊(不包含)
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Cond notLike(String field, Object value, boolean isRequired){
    String sql = new StringBuilder(" ").append(field).append(" NOT LIKE '").append(value).append("'").toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Cond(sql);
  }

  /**
   * 条件不包含
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Cond notIn(String field, Collection value){
    return notIn(field, value, false);
  }

  /**
   * 条件不包含
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Cond notIn(String field, Collection value, boolean isRequired){
    if(ValidateUtils.isNotEmpty(value) && value.size() == 1){
      return eq(field,value.iterator().next(),isRequired);
    }

    String sql = new StringBuilder(" ").append(field).append(" NOT IN ").append(listHandler(value)).toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Cond(sql);
  }

  /**
   * 条件模糊（包含）
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Cond like(String field, Object value){
    return like(field, value, false);
  }

  /**
   * 条件模糊（包含）
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Cond like(String field, Object value, boolean isRequired){
    String sql = new StringBuilder(" ").append(field).append(" LIKE '").append(value).append("'").toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Cond(sql);
  }

  /**
   * 条件包含
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Cond in(String field, Collection value){
    return in(field, value, false);
  }

  /**
   * 条件包含
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Cond in(String field, Collection value, boolean isRequired){
    if(ValidateUtils.isNotEmpty(value) && value.size() == 1){
      return eq(field,value.iterator().next(),isRequired);
    }

    String sql = new StringBuilder(" ").append(field).append(" IN ").append(listHandler(value)).toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Cond(sql);
  }


  /**
   * 条件不相等
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Cond ne(String field, Object value, boolean isRequired){
    String sql = new StringBuilder(" ").append(field).append(" <> '").append(value).append("'").toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Cond(sql);
  }

  /**
   * 条件不相等
   * @param field 字段
   * @param value 值
   * @return
   */
  public static Cond ne(String field, Object value){
    return ne(field, value, false);
  }

  /**
   * 条件相等
   * @param field 字段
   * @param value 值
   * @param isRequired 是否必填(true 必填,false 非必填)
   * @return
   */
  public static Cond eq(String field, Object value, boolean isRequired){
    String sql = new StringBuilder(" ").append(field).append(" = '").append(value).append("'").toString();
    if(!isRequired && ValidateUtils.isEmpty(value)){
      return null;
    }
    return new Cond(sql);
  }

  public Cond and(Cond cond){
    if (ValidateUtils.isEmpty(cond) || ValidateUtils.isEmpty(cond.getSql())){
      return this;
    }
    this.sql.append(" AND ").append(cond.getSql());
    return this;
  }

  public Cond or(Cond cond){
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
  public static Cond eq(String field, Object value){
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
