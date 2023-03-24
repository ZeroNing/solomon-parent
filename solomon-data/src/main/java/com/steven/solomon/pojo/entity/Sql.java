package com.steven.solomon.pojo.entity;

import com.steven.solomon.enums.OrderByEnum;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.List;

public class Sql extends BaseSql {

  public Sql() {
    super();
  }

  public Sql(String sql) {
    super(sql);
  }

  public static Sql SelectAll(){
    return new Sql("SELECT *");
  }

  public Sql from(String table){
    this.sql = sql.append(" FROM ").append(table);
    return this;
  }

  public Sql from(String table,String alias){
    this.sql = sql.append(" FROM ").append(table).append(" ").append(alias);
    return this;
  }

  public static Sql New(String sql) {
    return new Sql(sql);
  }

  public Sql append(String sql) {
    this.sql.append(sql);
    return this;
  }

  public Sql appendLine(String sql) {
    this.sql.append(sql).append(System.lineSeparator());
    return this;
  }

  public Sql where(){
    this.sql.append(" WHERE 1=1 ");
    return this;
  }

  public Sql leftJoin(String table,String alias,String on){
    this.sql.append(" LEFT JOIN ").append(table).append(" ").append(alias).append(" ").append("ON ").append(on);
    return this;
  }

  public Sql rightJoin(String table,String alias,String on){
    this.sql.append(" RIGHT JOIN ").append(table).append(" ").append(alias).append(" ").append("ON ").append(on);
    return this;
  }

  public Sql join(String table,String alias,String on){
    this.sql.append(" JOIN ").append(table).append(" ").append(alias).append(" ").append("ON ").append(on);
    return this;
  }

  public Sql fullJoin(String table,String alias,String on){
    this.sql.append(" FULL JOIN ").append(table).append(" ").append(alias).append(" ").append("ON ").append(on);
    return this;
  }

  public Sql groupBy(String fields){
    this.sql.append(" GROUP BY ").append(fields);
    return this;
  }

  public Sql and(Condition condition,boolean isMoreCond){
    if(ValidateUtils.isEmpty(condition) || ValidateUtils.isEmpty(condition.getSql())){
      return this;
    }
    if(isMoreCond){
      this.sql.append(" AND (").append(condition.getSql()).append(")");
    } else {
      this.sql.append(" AND").append(condition.getSql());
    }
    return this;
  }

  public Sql and(Condition condition){
    return and(condition,false);
  }

  public Sql or(Condition condition,boolean isMoreCond){
    if(ValidateUtils.isEmpty(condition) || ValidateUtils.isEmpty(condition.getSql())){
      return this;
    }
    if(isMoreCond){
      this.sql.append(" OR (").append(condition.getSql()).append(")");
    } else {
      this.sql.append(" OR").append(condition.getSql());
    }
    return this;
  }

  public Sql or(Condition condition){
    return or(condition,false);
  }

  public Sql orderBy(String fields){
    return orderBy(fields, OrderByEnum.ASC);
  }

  public Sql orderBy(String fields, OrderByEnum orderByEnum){
    this.sql.append(" ORDER BY ").append(fields).append(" ").append(orderByEnum.label());
    return this;
  }

  public Sql limit(int pageNo,int pageSize){
    this.sql.append(" LIMIT").append(pageNo).append(",").append(pageSize);
    return this;
  }

  public static void main(String[] args) {
    List<String> a = new ArrayList<>();
    a.add("a");
    a.add("b");
    Sql sql = Sql.SelectAll().from("WAREHOUSE","a");
    sql.where();
    sql.and(Condition.eq("a.code",null,false));
    sql.and(Condition.like("a.code","aaaa",true));
    sql.and(Condition.notLike("a.code","aaaa",true));
    sql.or(Condition.ne("a.code",null,true).and(Condition.eq("a.code","b")),true);
    sql.or(Condition.in("a.code",a,true));
    sql.or(Condition.notIn("a.code",a,true));
    System.out.println(sql.toString());

  }
}
