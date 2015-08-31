package com.hd123.jcrm.search.model;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 索引设置
 * 
 * @author liyue
 */
@XmlRootElement
public class IndexConfig implements Serializable {
  private static final long serialVersionUID = -3612210114455143513L;

  /** 相当于database name */
  private String indexName;
  /** 相当于table name */
  private String documentType;

  @SuppressWarnings("unused")
  private IndexConfig() {
  }

  public IndexConfig(String indexName, String documentType) throws Exception {
    checkIndex(indexName);

    setIndexName(indexName);
    setDocumentType(documentType);
  }

  /**
   * 索引名称
   * 
   * @return 索引名称
   */
  public String getIndexAliasName() {
    return indexName;
  }

  private void setIndexName(String indexName) throws Exception {
    this.indexName = indexName;
  }

  /**
   * 文档类型
   * 
   * @return 文档类型
   */
  public String getDocumentType() {
    return documentType;
  }

  private void setDocumentType(String documentType) {
    this.documentType = documentType;
  }

  /**
   * 验证索引名称合法性
   * <p>
   * elasticsearch要求索引名称全为小写字母
   * 
   * @param indexName
   * @throws Exception
   */
  private void checkIndex(String indexName) throws Exception {
    assert indexName != null;

    final String REGEX_INDEX_NAME = "^[a-z]+$";
    final Pattern pattern = Pattern.compile(REGEX_INDEX_NAME);
    final Matcher matcher = pattern.matcher(indexName);
    if (!matcher.matches()) {
      throw new Exception(MessageFormat.format("索引名称{0}应全为小写字母。", indexName));
    }
  }

}
