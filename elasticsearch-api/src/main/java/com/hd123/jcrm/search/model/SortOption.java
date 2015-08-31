package com.hd123.jcrm.search.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 排序方式
 * 
 * @author liyue
 */
@XmlRootElement
public final class SortOption implements Serializable {
  private static final long serialVersionUID = 464402909262240609L;

  private String field;
  private SortType type = SortType.desc;

  public SortOption() {
  }

  public SortOption(String field, SortType type) {
    this.field = field;
    this.type = type;
  }

  public enum SortType {
    asc, desc
  }

  /**
   * 排序字段
   * 
   * @return 排序字段
   */
  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  /**
   * 排序类型
   * 
   * @return 排序类型
   */
  public SortType getType() {
    return type;
  }

  public void setType(SortType type) {
    this.type = type;
  }

}
