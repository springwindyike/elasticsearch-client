package com.hd123.jcrm.search.api.index;

import java.util.List;

import org.elasticsearch.common.Nullable;

import com.hd123.jcrm.search.model.Document;
import com.hd123.jcrm.search.model.IndexConfig;

/**
 * 索引服务
 * 
 * @author liyue
 */
public interface ElasticSearchIndexService {

  /**
   * 批量索引并更新数据(大数据量推荐多线程异步索引)
   * 
   * <p>
   * 建议索引完毕后调用 {@link #indexOptimize()}
   * 
   * @param <T>
   *          索引数据类型
   * @param indexConfig
   *          索引设置
   * @param datas
   *          索引数据(理论上应为@Entity注解标注的实体的集合，根据@Id自动生成文档Id)
   * @throws Exception
   *           异常信息
   */
  public <T> void indexDatas(IndexConfig indexConfig, List<T> datas) throws Exception;

  /**
   * 批量索引并更新数据(大数据量推荐多线程异步索引)
   * 
   * <p>
   * 建议索引完毕后调用 {@link #indexOptimize()}
   * <p>
   * 与 {@link #indexDatas(IndexConfig, List)}的区别是可以自定义文档Id
   * 
   * @param <T>
   *          索引数据类型
   * @param indexConfig
   *          索引设置
   * @param documents
   *          文档集合
   * @throws Exception
   *           异常信息
   */
  public <T> void indexWithCustomDocuments(IndexConfig indexConfig, List<Document<T>> documents)
      throws Exception;

  /**
   * 优化索引，索引数据完成后调用
   * 
   * @throws Exception
   *           异常信息
   */
  public void indexOptimize() throws Exception;

  /**
   * 删除索引
   * 
   * @param indexName
   *          索引名称，非空
   * @param documentType
   *          文档类型
   * @param documentId
   *          文档id,传空将删除全部文档
   * @throws Exception
   *           异常信息
   */
  public void dropIndex(String indexName, @Nullable String documentType,
      @Nullable String documentId) throws Exception;

}
