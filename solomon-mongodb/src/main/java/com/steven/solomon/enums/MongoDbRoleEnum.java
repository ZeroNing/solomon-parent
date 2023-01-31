package com.steven.solomon.enums;

public enum MongoDbRoleEnum {
  /**
   * 数据库用户角色
   */
  READ("read","只读"),
  READ_WRITE("readWrite","读写"),
  /**
   * 数据库管理角色
   */
  DB_ADMIN("dbAdmin","一些数据库对象的管理操作，但是没有数据库的读写权限"),
  DB_OWNER("dbOwner","该数据库的所有者，具有该数据库的全部权限"),
  USER_ADMIN("userAdmin","为当前用户创建、修改用户和角色。拥有userAdmin权限的用户可以将该数据库的任意权限赋予任意的用户。"),
  /**
   * 集群管理权限
   */
  CLUSTER_ADMIN("clusterAdmin","提供了最大的集群管理功能。相当于clusterManager, clusterMonitor, and hostManager和dropDatabase的权限组合。"),
  CLUSTER_MANAGER("clusterManager","提供了集群和复制集管理和监控操作。拥有该权限的用户可以操作config和local数据库（即分片和复制功能）"),
  CLUSTER_MONITOR("clusterMonitor","仅仅监控集群和复制集。"),
  HOST_MANAGER("hostManager","提供了监控和管理服务器的权限，包括shutdown节点，logrotate, repairDatabase等。"),
  /**
   * 所有数据库角色
   */
  READ_ANY_DATABASE("readAnyDatabase","具有read每一个数据库权限。但是不包括应用到集群中的数据库"),
  READ_WRITE_ANY_DATABASE("readWriteAnyDatabase","具有readWrite每一个数据库权限。但是不包括应用到集群中的数据库"),
  USER_ADMIN_ANY_DATABASE("userAdminAnyDatabase","具有userAdmin每一个数据库权限，但是不包括应用到集群中的数据库"),
  DB_ADMIN_ANY_DATABASE("dbAdminAnyDatabase","提供了dbAdmin每一个数据库权限，但是不包括应用到集群中的数据库");

  private String value;
  private String desc;

  MongoDbRoleEnum(String value,String desc){
    this.desc = desc;
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public String getDesc() {
    return desc;
  }
}
