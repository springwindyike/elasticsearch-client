package com.hd123.jcrm.search.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

/**
 * 文档
 * 
 * @author liyue
 * @param <T>
 *          文档对象类型
 */
@XmlRootElement
public class Document<T> implements Serializable {
  private static final long serialVersionUID = 4807426914025242891L;

  private String id;
  private T document;

  @SuppressWarnings("unused")
  private Document() {
  }

  public Document(String documentId, T document) {
    assert StringUtils.isNotBlank(documentId);
    assert document != null;

    setId(documentId);
    setDocument(document);
  }

  /**
   * 文档ID（唯一标识）
   * 
   * @return 文档ID（唯一标识）
   */
  public String getId() {
    return id;
  }

  private void setId(String documentId) {
    this.id = documentId;
  }

  /**
   * 文档内容
   * 
   * @return 文档内容
   */
  public T getDocument() {
    return document;
  }

  private void setDocument(T document) {
    this.document = document;
  }

}
