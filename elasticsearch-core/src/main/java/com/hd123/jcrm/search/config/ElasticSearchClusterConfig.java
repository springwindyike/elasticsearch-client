package com.hd123.jcrm.search.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 集群设置
 * 
 * @author liyue
 */
@Component
public class ElasticSearchClusterConfig implements InitializingBean {

  @Value("${elasticSearch.clusterConfig.enableCluster}")
  private Boolean enableCluster;
  @Value("${elasticSearch.clusterConfig.clusterName}")
  private String clusterName;
  @Value("${elasticSearch.clusterConfig.clusterNodeIp}")
  private String clusterNodeIp;
  @Value("${elasticSearch.clusterConfig.clusterNodePort}")
  private Integer clusterNodePort;

  /**
   * 是否启用集群
   * 
   * @return
   */
  public Boolean getEnableCluster() {
    return enableCluster;
  }

  /**
   * 集群名称
   * 
   * @return
   */
  public String getClusterName() {
    return clusterName;
  }

  /**
   * 集群节点IP地址
   * 
   * @return
   */
  public String getClusterNodeIp() {
    return clusterNodeIp;
  }

  /**
   * 集群节点端口
   * 
   * @return
   */
  public Integer getClusterNodePort() {
    return clusterNodePort;
  }

  public void setEnableCluster(Boolean enableCluster) {
    this.enableCluster = enableCluster;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public void setClusterNodeIp(String clusterNodeIp) {
    this.clusterNodeIp = clusterNodeIp;
  }

  public void setClusterNodePort(Integer clusterNodePort) {
    this.clusterNodePort = clusterNodePort;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // 校验配置是否正确
    if (StringUtils.isBlank(getClusterName())) {
      throw new IllegalArgumentException("集群名称配置错误");
    }

    if (StringUtils.isBlank(getClusterName())) {
      throw new IllegalArgumentException("集群节点IP地址配置错误");
    }

    if (clusterNodePort == null) {
      throw new IllegalArgumentException("集群节点端口配置错误");
    }
  }

}
