package com.steven.solomon.tree;

import com.steven.solomon.verification.ValidateUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BaseTreeNode implements Serializable {

  /**
   * 子Id
   */
  private String id;
  /**
   * 父ID
   */
  private String pId;

  private List<BaseTreeNode> child;

  public BaseTreeNode() {
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getpId() {
    return this.pId;
  }

  public void setpId(String pId) {
    this.pId = pId;
  }

  public List<BaseTreeNode> getChild() {
    return this.child;
  }

  public void setChild(List<BaseTreeNode> child) {
    this.child = child;
  }

  public void addChild(BaseTreeNode baseTreeNode) {
    if (ValidateUtils.isEmpty(this.child)) {
      this.setChild(new ArrayList());
    }

    this.getChild().add(baseTreeNode);
  }
}
