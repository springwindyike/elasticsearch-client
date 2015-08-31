package com.hd123.jcrm.search.api.query;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 搜索响应
 * 
 * @author liyue
 */
@XmlRootElement
public class EsSearchResponse implements Serializable {
  private static final long serialVersionUID = -637690309547551591L;

  private String resultJson;
  private long counts;
  private long totalCounts;
  private int totalShards;
  private int successfulShards;
  private int failedShards;
  private boolean timedOut;
  private long tookInMillis;
  private boolean isTerminatedEarly = false;

  /**
   * 是否提前退出
   * 
   * @return 是否提前退出
   */
  public boolean isTerminatedEarly() {
    return isTerminatedEarly;
  }

  public void setTerminatedEarly(boolean isTerminatedEarly) {
    this.isTerminatedEarly = isTerminatedEarly;
  }

  /**
   * 总分片数
   * 
   * @return 总分片数
   */
  public int getTotalShards() {
    return totalShards;
  }

  public void setTotalShards(int totalShards) {
    this.totalShards = totalShards;
  }

  /**
   * 成功的分片数
   * 
   * @return 成功的分片数
   */
  public int getSuccessfulShards() {
    return successfulShards;
  }

  public void setSuccessfulShards(int successfulShards) {
    this.successfulShards = successfulShards;
  }

  /**
   * 失败的分片数
   * 
   * @return 失败的分片数
   */
  public int getFailedShards() {
    return failedShards;
  }

  public void setFailedShards(int failedShards) {
    this.failedShards = failedShards;
  }

  /**
   * 是否超时
   * 
   * @return 是否超时
   */
  public boolean isTimedOut() {
    return timedOut;
  }

  public void setTimedOut(boolean timedOut) {
    this.timedOut = timedOut;
  }

  /**
   * 花费毫秒数
   * 
   * @return 花费毫秒数
   */
  public long getTookInMillis() {
    return tookInMillis;
  }

  public void setTookInMillis(long tookInMillis) {
    this.tookInMillis = tookInMillis;
  }

  /**
   * 查询结果Json字符串
   * 
   * @return 查询结果Json字符串
   */
  public String getResultJson() {
    return resultJson;
  }

  public void setResultJson(String resultJson) {
    this.resultJson = resultJson;
  }

  /**
   * 返回的结果总数
   * 
   * @return 返回的结果总数
   */
  public long getCounts() {
    return counts;
  }

  /**
   * 结果总数
   * 
   * @return 结果总数
   */
  public long getTotalCounts() {
    return totalCounts;
  }

  public void setTotalCounts(long totalCounts) {
    this.totalCounts = totalCounts;
  }

  public void setCounts(long counts) {
    this.counts = counts;
  }

}
