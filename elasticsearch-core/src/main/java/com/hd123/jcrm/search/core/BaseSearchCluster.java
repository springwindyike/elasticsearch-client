package com.hd123.jcrm.search.core;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hd123.jcrm.search.config.ElasticSearchClusterConfig;
import com.hd123.jcrm.search.model.ElasticSearchReservedWords;

/**
 * elasticsearch基础服务
 * 
 * @author liyue
 */
@Component
public class BaseSearchCluster implements InitializingBean, DisposableBean {
  // 日志打印
  protected final Logger logger = LoggerFactory.getLogger(getClass());
  /**
   * instance a json mapper
   *
   * @see {@link com.alibaba.fastjson.JSON}
   */
  @Deprecated
  protected static final ObjectMapper mapper = new ObjectMapper();

  @Autowired
  protected ElasticSearchClusterConfig clusterConfig;

  /**
   * 节点客户端({@link org.elasticsearch.client.node.NodeClient})：
   * <p>
   * 节点客户端以无数据节点(none data node)身份加入集群， 换言之， 它自己不存储任何数据， 但是它知道数据在集群中的具体位置，
   * 并且能够直接转发请求到对应的节点上。
   * <p>
   * 传输客户端({@link org.elasticsearch.client.transport.TransportClient})：
   * <p>
   * 这个更轻量的传输客户端能够发送请求到远程集群。 它自己不加入集群， 只是简单转发请求给集群中的节点。
   */
  private static volatile TransportClient client;

  /**
   * 取得客户端
   * 
   * @return
   */
  protected synchronized Client getClient() {
    if (client == null) {
      if (logger.isWarnEnabled()) {
        logger.warn(MessageFormat.format("当前集群名称为:“{0}”,节点为:“{1}:{2}”,请检查配置是否正确。",
            clusterConfig.getClusterName(), clusterConfig.getClusterNodeIp(),
            clusterConfig.getClusterNodePort().toString()));
      }

      openClient();
    }

    return client;
  }

  /**
   * 刷新
   */
  protected void refreshServer() {
    client.admin().indices().refresh(Requests.refreshRequest()).actionGet();
  }

  /**
   * 新增节点
   * 
   * @param name
   * @param port
   */
  protected void addNewNode(String name, int port) {
    client.addTransportAddress(new InetSocketTransportAddress(name, port));
  }

  /**
   * 删除节点
   * 
   * @param name
   * @param port
   */
  protected void removeNode(String name, int port) {
    client.removeTransportAddress(new InetSocketTransportAddress(name, port));
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (clusterConfig.getEnableCluster().booleanValue()) {
      openClient();
    }
  }

  @Override
  public void destroy() throws Exception {
    closeClient();
  }

  /**
   * 打开客户端
   */
  private void openClient() {
    if (client == null) {
      if (logger.isInfoEnabled()) {
        logger.info("Creating client for Search!");
      }

      try {
        /*
         * other settings
         *
         * client.transport.ignore_cluster_name Set to true to ignore cluster
         * name validation of connected nodes. (since 0.19.4)
         */
        final Settings settings = ImmutableSettings.settingsBuilder()
            // 集群名称
            .put(ElasticSearchReservedWords.CLUSTER_NAME.getValue(), clusterConfig.getClusterName())
            // 自动嗅探其他节点
            .put(ElasticSearchReservedWords.CLIENT_TRANSPORT_SNIFF.getValue(), Boolean.TRUE)
            // 超时时间
            .put(ElasticSearchReservedWords.CLIENT_TRANSPORT_PING_TIMEOUT.getValue(), "10s")
            // 探测节点连接及列表的时间间隔
            .put(ElasticSearchReservedWords.CLIENT_TRANSPORT_NODES_SAMPLER_INTERVAL.getValue(),
                "3600s")
            // 构造settings
            .build();

        // 采用反射的方法生成一个client,效率明显高于由于new一个client
        final Class<?> clazz = Class.forName(TransportClient.class.getName());
        final Constructor<?> constructor = clazz.getDeclaredConstructor(Settings.class);
        constructor.setAccessible(true);
        client = (TransportClient) constructor.newInstance(settings);
        // 添加一个节点
        client.addTransportAddress(new InetSocketTransportAddress(clusterConfig.getClusterNodeIp(),
            clusterConfig.getClusterNodePort().intValue()));

        if (client.connectedNodes().size() == 0) {
          if (logger.isWarnEnabled()) {
            logger.warn(MessageFormat.format("当前集群名称为:“{0}”,节点为:“{1}:{2}”,请检查配置是否正确。",
                clusterConfig.getClusterName(), clusterConfig.getClusterNodeIp(),
                clusterConfig.getClusterNodePort().toString()));
          }

          if (logger.isErrorEnabled()) {
            logger.error(
                "There are no active nodes available for the transport, it will be automatically added once nodes are live!");
          }
        }
      } catch (Exception ex) {
        // ignore any exception
        if (logger.isErrorEnabled()) {
          logger.error("Error occured while creating search client!", ex);
        }
      }
    }
  }

  /**
   * 关闭客户端
   */
  private void closeClient() {
    if (client == null) {
      return;
    }

    client.close();
    client = null;
  }

}
