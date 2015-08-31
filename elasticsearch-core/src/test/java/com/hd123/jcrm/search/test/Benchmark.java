package com.hd123.jcrm.search.test;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hd123.jcrm.search.api.index.ElasticSearchIndexService;
import com.hd123.jcrm.search.api.query.ElasticSearchQueryService;
import com.hd123.jcrm.search.config.ElasticSearchClusterConfig;

/**
 * 测试台
 * 
 * @author liyue
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
    "classpath:main.xml" })
public abstract class Benchmark {

  @Autowired
  protected ApplicationContext appCtx;

  @Autowired
  protected ElasticSearchClusterConfig clusterConfig;

  @Autowired
  protected ElasticSearchQueryService elasticSearchQueryService;
  @Autowired
  protected ElasticSearchIndexService elasticSearchIndexDataService;

  @Before
  public void setup() throws Exception {
    System.out.println(clusterConfig.getEnableCluster());
    System.out.println(clusterConfig.getClusterName());
    System.out.println(clusterConfig.getClusterNodeIp());
    System.out.println(clusterConfig.getClusterNodePort());
  }

  @After
  public void clearData() throws Exception {
  }

  public <T> T getBean(Class<T> beanClass) {
    return appCtx.getBean(beanClass);
  }

}
