package com.hd123.jcrm.search.test.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Id;

/**
 * @author liyue
 */
public class TestData {

  private int id;
  private String name;
  private String description;
  private List<String> childs;
  private List<Data> datas;

  public List<Data> getDatas() {
    return datas;
  }

  public void setDatas(List<Data> datas) {
    this.datas = datas;
  }

  public List<String> getChilds() {
    return childs;
  }

  public void setChilds(List<String> childs) {
    this.childs = childs;
  }

  @Id
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public static class Data {
    private int dataId;
    private String dataName;
    private Boolean isData = Boolean.FALSE;
    private Date dataTime = new Date();

    public Date getDataTime() {
      return dataTime;
    }

    public void setDataTime(Date dataTime) {
      this.dataTime = dataTime;
    }

    public Data() {
    }

    public int getDataId() {
      return dataId;
    }

    public void setDataId(int dataId) {
      this.dataId = dataId;
    }

    public String getDataName() {
      return dataName;
    }

    public void setDataName(String dataName) {
      this.dataName = dataName;
    }

    public Boolean getIsData() {
      return isData;
    }

    public void setIsData(Boolean isData) {
      this.isData = isData;
    }
  }

}
