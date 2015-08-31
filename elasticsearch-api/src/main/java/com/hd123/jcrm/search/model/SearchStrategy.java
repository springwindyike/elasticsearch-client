package com.hd123.jcrm.search.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

/**
 * 查询策略
 * 
 * @author liyue
 */
@XmlRootElement
public class SearchStrategy implements Serializable {
  private static final long serialVersionUID = 4319896580428032616L;

  /**
   * 查询字段名（大小写敏感）
   * <p>
   * {@link com.hd123.jcrm.search.model.SearchOption.SearchType} <br>
   * queryString类型查询可传空
   */
  private String fieldName = StringUtils.EMPTY;
  /** 查询字段的值，可传多个 */
  private Object[] fieldValues = new Object[] {};
  /** 搜索选项 */
  private SearchOption searchOption = new SearchOption();

  public SearchStrategy() {
  }

  public SearchStrategy(String fieldName, Object[] fieldValues) {
    this(fieldName, fieldValues, new SearchOption());
  }

  public SearchStrategy(String fieldName, Object[] fieldValues, SearchOption searchOption) {
    if (StringUtils.isBlank(fieldName)) {
      fieldName = StringUtils.EMPTY;
    }
    this.fieldName = fieldName;
    this.fieldValues = fieldValues;
    this.searchOption = searchOption;
  }

  /**
   * 查询字段名（大小写敏感）
   * <p>
   * {@link com.hd123.jcrm.search.model.SearchOption.SearchType} <br>
   * queryString类型查询可传空
   * 
   * @return 查询字段名
   */
  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * 查询字段的值，可传多个
   * 
   * @return 查询字段的值
   */
  public Object[] getFieldValues() {
    return fieldValues;
  }

  public void setFieldValues(Object[] fieldValues) {
    this.fieldValues = fieldValues;
  }

  /**
   * 搜索选项
   * 
   * @return 搜索选项
   */
  public SearchOption getSearchOption() {
    return searchOption;
  }

  public void setSearchOption(SearchOption searchOption) {
    this.searchOption = searchOption;
  }

}
