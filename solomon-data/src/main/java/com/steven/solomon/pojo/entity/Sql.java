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

  public static Sql SelectAll(String table){
    return new Sql("SELECT * FROM " + table);
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

  public Sql and(Cond cond, boolean isMoreCond){
    if(ValidateUtils.isEmpty(cond) || ValidateUtils.isEmpty(cond.getSql())){
      return this;
    }
    if(isMoreCond){
      this.sql.append(" AND (").append(cond.getSql()).append(")");
    } else {
      this.sql.append(" AND ").append(cond.getSql());
    }
    return this;
  }

  public Sql and(Cond cond){
    return and(cond,false);
  }

  public Sql or(Cond cond, boolean isMoreCond){
    if(ValidateUtils.isEmpty(cond) || ValidateUtils.isEmpty(cond.getSql())){
      return this;
    }
    if(isMoreCond){
      this.sql.append(" OR (").append(cond.getSql()).append(")");
    } else {
      this.sql.append(" OR ").append(cond.getSql());
    }
    return this;
  }

  public Sql or(Cond cond){
    return or(cond,false);
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
    sql.and(Cond.eq("a.code",null,false));
    sql.and(Cond.like("a.code","aaaa",true));
    sql.and(Cond.notLike("a.code","aaaa",true));
    sql.or(Cond.ne("a.code",null,true).and(Cond.eq("a.code","b")),true);
    sql.or(Cond.in("a.code",a,true));
    sql.or(Cond.notIn("a.code",a,true));
    sql.and(Cond.between("a.code","a","a"));
    System.out.println(sql.toString());

  }
}
