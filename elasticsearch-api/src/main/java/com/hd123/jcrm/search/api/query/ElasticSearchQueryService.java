package com.hd123.jcrm.search.api.query;

import java.util.List;

import org.elasticsearch.common.Nullable;

import com.hd123.jcrm.search.model.SearchOption.QueryLogic;
import com.hd123.jcrm.search.model.SearchStrategy;
import com.hd123.jcrm.search.model.SortOption;

/**
 * 搜索服务
 * 
 * @author liyue
 */
public interface ElasticSearchQueryService {

  /** 最大查询结果数 */
  public static final int MAX_SEARCH_COUNTS = 20000;

  /**
   * 搜索
   * 
   * @param indexNames
   *          索引名称，非空
   * @param documents
   *          文档类型，确认文档类型存在
   * @param searchContents
   *          查询内容
   * @param searchLogic
   *          搜索条件之间的逻辑关系
   * @param from
   *          从第几条记录开始（应大于等于0）
   * @param size
   *          共显示多少条记录（应大于0，不要太大以免内存溢出）
   * @param sortOption
   *          排序方式
   * @param filterContents
   *          过滤内容
   * @param filterLogic
   *          过滤条件之间的逻辑关系
   * @return 查询结果，查不到返回null
   * @throws Exception
   *           异常信息
   */
  public EsSearchResponse search(String[] indexNames, @Nullable String[] documents,
      @Nullable List<SearchStrategy> searchContents, @Nullable QueryLogic searchLogic, int from,
      int size, @Nullable SortOption sortOption, @Nullable List<SearchStrategy> filterContents,
      @Nullable QueryLogic filterLogic) throws Exception;

  /**
   * 查询数量
   * 
   * @param indexNames
   *          索引名称，非空
   * @param documents
   *          文档类型，确认文档类型存在
   * @param searchContents
   *          查询内容
   * @param searchLogic
   *          搜索条件之间的逻辑关系
   * @param filterContents
   *          过滤内容
   * @param filterLogic
   *          过滤条件之间的逻辑关系
   * @return 查询结果的数量
   * @throws Exception
   *           异常信息
   */
  public long getCount(String[] indexNames, @Nullable String[] documents,
      @Nullable List<SearchStrategy> searchContents, @Nullable QueryLogic searchLogic,
      @Nullable List<SearchStrategy> filterContents, @Nullable QueryLogic filterLogic)
          throws Exception;

  /**
   * 查询数量
   * 
   * @param indexNames
   *          索引名称，非空
   * @param documents
   *          文档类型，确认文档类型存在
   * @param queryString
   *          查询字符串(JSON)，非空
   * @return 查询结果的数量
   * @throws Exception
   *           异常信息
   */
  public long getCount(String[] indexNames, @Nullable String[] documents,
      @Nullable String queryString) throws Exception;

}
