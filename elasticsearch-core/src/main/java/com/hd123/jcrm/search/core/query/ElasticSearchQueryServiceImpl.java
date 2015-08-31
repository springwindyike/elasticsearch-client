package com.hd123.jcrm.search.core.query;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.ExistsFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.NotFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryFilterBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.hd123.jcrm.search.api.query.ElasticSearchQueryService;
import com.hd123.jcrm.search.api.query.EsSearchResponse;
import com.hd123.jcrm.search.core.BaseSearchCluster;
import com.hd123.jcrm.search.model.SearchOption;
import com.hd123.jcrm.search.model.SearchOption.DataFilter;
import com.hd123.jcrm.search.model.SearchOption.QueryLogic;
import com.hd123.jcrm.search.model.SearchOption.RangeQueryType;
import com.hd123.jcrm.search.model.SearchOption.SearchType;
import com.hd123.jcrm.search.model.SearchStrategy;
import com.hd123.jcrm.search.model.SortOption;
import com.hd123.jcrm.search.model.SortOption.SortType;

/**
 * 搜索服务实现
 * 
 * @author liyue
 */
@Service(value = "elasticSearchQueryService")
public class ElasticSearchQueryServiceImpl extends BaseSearchCluster
    implements ElasticSearchQueryService {

  @Override
  public EsSearchResponse search(String[] indices, String[] documents,
      List<SearchStrategy> searchContents, QueryLogic searchLogic, int from, int size,
      SortOption sortOption, List<SearchStrategy> filterContents, QueryLogic filterLogic)
          throws Exception {
    assert indices != null && indices.length > 0;

    if (from < 0) {
      from = 0;
    }

    if (size <= 0 || size >= MAX_SEARCH_COUNTS) {
      size = MAX_SEARCH_COUNTS;
    }

    try {
      // 查询条件
      QueryBuilder queryBuilder = createQueryBuilder(searchContents, searchLogic);
      // 过滤条件
      queryBuilder = createFilterBuilder(filterLogic, queryBuilder, searchContents, filterContents);
      /*
       * {@link #org.elasticsearch.action.search.SearchType}
       * 
       * QUERY_THEN_FETCH
       * 查询是针对所有的块执行的，但返回的是足够的信息，而不是文档内容（Document）。结果会被排序和分级，基于此，只有相关的块的文档对象会被返回
       * 。由于被取到的仅仅是这些，故而返回的hit的大小正好等于指定的size。这对于有许多块的index来说是很便利的（返回结果不会有重复的，
       * 因为块被分组了）。
       * 
       * QUERY_AND_FETCH
       * 最原始（也可能是最快的）实现就是简单的在所有相关的shard上执行检索并返回结果。每个shard返回一定尺寸的结果。
       * 由于每个shard已经返回了一定尺寸的hit，这种类型实际上是返回多个shard的一定尺寸的结果给调用者。
       * 
       * DFS_QUERY_THEN_FETCH
       * 与QUERY_THEN_FETCH相同，预期一个初始的散射相伴用来为更准确的score计算分配了的term频率。
       * 
       * DFS_QUERY_AND_FETCH
       * 与QUERY_AND_FETCH相同，预期一个初始的散射相伴用来为更准确的score计算分配了的term频率。
       * 
       * SCAN 在执行了没有进行任何排序的检索时执行浏览。此时将会自动的开始滚动结果集。
       * 
       * COUNT 只计算结果的数量，也会执行facet。
       */
      SearchRequestBuilder searchRequestBuilder = getClient().prepareSearch(indices)
          .setSearchType(org.elasticsearch.action.search.SearchType.DFS_QUERY_THEN_FETCH)
          .setFrom(from).setSize(size).setExplain(true);
      if (queryBuilder != null) {
        searchRequestBuilder.setQuery(queryBuilder);
      }
      if (documents != null && documents.length > 0) {
        searchRequestBuilder.setTypes(documents);
      }

      // 如果需要排序
      if (sortOption != null) {
        final String sortField = sortOption.getField();
        final SortType sortType = sortOption.getType();
        if (StringUtils.isNotBlank(sortField)) {
          final SortOrder sortOrder = Objects.equal(sortType, SortType.desc) ? SortOrder.DESC
              : SortOrder.ASC;
          searchRequestBuilder = searchRequestBuilder.addSort(sortField, sortOrder);
        }
      }

      // 高亮，暂不实现
      searchRequestBuilder = createHighlight(searchRequestBuilder, searchContents);

      if (logger.isDebugEnabled()) {
        logger.debug(searchRequestBuilder.toString());
      }

      // 搜索结果
      final SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

      return buildSearchResult(searchResponse);
    } catch (Exception ex) {
      final boolean errable = logger.isErrorEnabled();
      if (ex instanceof IndexMissingException) {
        final String message = MessageFormat.format("索引{0}中有索引不存在", Arrays.toString(indices));
        if (errable) {
          logger.error(message, ex);
        }

        throw new Exception(message, ex);
      } else if (ex instanceof SearchPhaseExecutionException) {
        final String message = "查询语法解析错误，请检查搜索条件是否正确";
        if (errable) {
          logger.error(message, ex);
        }

        throw new Exception(message, ex);
      } else {
        if (errable) {
          logger.error("搜索失败", ex);
        }

        throw ex;
      }
    }
  }

  @Override
  public long getCount(String[] indexNames, String[] documents, List<SearchStrategy> searchContents,
      QueryLogic searchLogic, List<SearchStrategy> filterContents, QueryLogic filterLogic)
          throws Exception {
    try {
      QueryBuilder queryBuilder = createQueryBuilder(searchContents, searchLogic);
      queryBuilder = createFilterBuilder(filterLogic, queryBuilder, searchContents, filterContents);
      final SearchResponse searchResponse = searchCountRequest(indexNames, documents, queryBuilder);
      if (searchResponse == null || searchResponse.getHits() == null) {
        return 0;
      }

      return searchResponse.getHits().totalHits();
    } catch (Exception ex) {
      if (logger.isErrorEnabled()) {
        logger.error("failed to get search count", ex);
      }

      throw ex;
    }
  }

  @Override
  public long getCount(String[] indexNames, String[] documents, String queryString)
      throws Exception {
    final SearchResponse searchResponse = searchCountRequest(indexNames, documents, queryString);
    if (searchResponse == null || searchResponse.getHits() == null) {
      return 0;
    }

    return searchResponse.getHits().totalHits();
  }

  /**
   * 执行count查询
   * 
   * @param indexNames
   * @param queryBuilder
   * @return
   * @throws Exception
   */
  private SearchResponse searchCountRequest(String[] indexNames, String[] documents,
      Object queryBuilder) throws Exception {
    try {
      SearchRequestBuilder searchRequestBuilder = getClient().prepareSearch(indexNames)
          .setSearchType(org.elasticsearch.action.search.SearchType.COUNT);
      if (documents != null && documents.length > 0) {
        searchRequestBuilder.setTypes(documents);
      }

      final boolean debugable = logger.isDebugEnabled();
      if (queryBuilder != null) {
        if (queryBuilder instanceof QueryBuilder) {
          final QueryBuilder builder = (QueryBuilder) queryBuilder;
          searchRequestBuilder = searchRequestBuilder.setQuery(builder);
          if (debugable) {
            logger.debug(searchRequestBuilder.toString());
          }
        } else if (queryBuilder instanceof String || queryBuilder instanceof CharSequence) {
          final String query = (String) queryBuilder;
          searchRequestBuilder = searchRequestBuilder.setQuery(QueryBuilders.wrapperQuery(query));
          if (debugable) {
            logger.debug(query);
          }
        }
      }

      return searchRequestBuilder.execute().actionGet();
    } catch (Exception ex) {
      if (logger.isErrorEnabled()) {
        logger.error("查询数量失败，请检查参数是否正确", ex);
      }

      throw ex;
    }
  }

  // public long getCount(String[] indexNames, byte[] queryString) {
  // try {
  // SearchResponse searchResponse = searchCountRequest(indexNames,
  // queryString);
  // return searchResponse.hits().totalHits();
  // }
  // catch (Exception e) {
  // logger.error(e.getMessage());
  // }
  // return 0;
  // }

  /**
   * 创建搜索条件
   * 
   * @param searchContents
   * @param searchLogic
   * @throws Exception
   */
  private QueryBuilder createQueryBuilder(List<SearchStrategy> searchContents,
      QueryLogic searchLogic) throws Exception {
    try {
      if (searchContents == null || searchContents.size() == 0) {
        return null;
      }

      BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
      // 循环每一个需要搜索的字段和值
      for (SearchStrategy searchContent : searchContents) {
        final String field = searchContent.getFieldName();
        final Object[] values = searchContent.getFieldValues();
        // 排除非法的搜索值
        if (!checkValue(values)) {
          if (logger.isWarnEnabled()) {
            logger.warn("搜素字段值不符合规范，搜索结果可能不准确。");
          }

          continue;
        }

        final QueryBuilder queryBuilder = createSingleFieldQueryBuilder(field, values,
            searchContent.getSearchOption());
        if (queryBuilder != null) {
          if (Objects.equal(QueryLogic.should, searchLogic)) {
            // should关系，也就是说，在A索引里有或者在B索引里有都可以
            boolQueryBuilder = boolQueryBuilder.should(queryBuilder);
          } else {
            // must关系，也就是说，在A索引里有，在B索引里也必须有
            boolQueryBuilder = boolQueryBuilder.must(queryBuilder);
          }
        }
      }

      return boolQueryBuilder;
    } catch (Exception ex) {
      if (logger.isErrorEnabled()) {
        logger.error("build QueryBuilder failed", ex);
      }

      throw ex;
    }
  }

  /**
   * 创建过滤条件
   * 
   * @param filterLogic
   * @param queryBuilder
   * @param searchContents
   * @param filterContents
   * @return
   * @throws Exception
   */
  private QueryBuilder createFilterBuilder(QueryLogic filterLogic, QueryBuilder queryBuilder,
      List<SearchStrategy> searchContents, List<SearchStrategy> filterContents) throws Exception {
    try {
      AndFilterBuilder andFilterBuilder = null;

      if (searchContents != null) {
        for (SearchStrategy searchContent : searchContents) {
          final Object[] values = searchContent.getFieldValues();
          // 排除非法的搜索值
          if (!checkValue(values)) {
            if (logger.isWarnEnabled()) {
              logger.warn("搜素字段值不符合规范，搜索结果可能不准确。");
            }

            continue;
          }

          final SearchOption searchOption = searchContent.getSearchOption();
          if (Objects.equal(DataFilter.exists, searchOption.getDataFilter())) {
            // 被搜索的条件必须有值
            final ExistsFilterBuilder existsFilterBuilder = FilterBuilders
                .existsFilter(searchContent.getFieldName());
            if (andFilterBuilder == null) {
              andFilterBuilder = FilterBuilders.andFilter(existsFilterBuilder);
            } else {
              andFilterBuilder = andFilterBuilder.add(existsFilterBuilder);
            }
          }
        }
      }

      if (filterContents == null || filterContents.isEmpty()) {
        // 如果没有其它过滤条件，返回
        return QueryBuilders.filteredQuery(queryBuilder, andFilterBuilder);
      }

      // 构造过滤条件
      final QueryFilterBuilder queryFilterBuilder = FilterBuilders
          .queryFilter(createQueryBuilder(filterContents, filterLogic));
      // 构造not过滤条件，表示搜索结果不包含这些内容，而不是不过滤
      final NotFilterBuilder notFilterBuilder = FilterBuilders.notFilter(queryFilterBuilder);

      if (andFilterBuilder == null) {
        andFilterBuilder = FilterBuilders.andFilter(notFilterBuilder);
      } else {
        andFilterBuilder = FilterBuilders.andFilter(andFilterBuilder, notFilterBuilder);
      }

      final FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(queryBuilder,
          andFilterBuilder);
      return filteredQueryBuilder;
    } catch (Exception ex) {
      if (logger.isErrorEnabled()) {
        logger.error("create FilterBuilder failed", ex);
      }

      throw ex;
    }
  }

  /**
   * 构造单一字段查询器
   * 
   * @param field
   * @param values
   * @param searchOption
   * @return
   * @throws Exception
   */
  private QueryBuilder createSingleFieldQueryBuilder(String field, Object[] values,
      SearchOption searchOption) throws Exception {
    try {
      // 区间搜索
      if (Objects.equal(SearchType.range, searchOption.getSearchType())) {
        return createRangeQueryBuilder(field, values, searchOption);
      }

      BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
      QueryBuilder queryBuilder = null;

      // term 搜索
      if (Objects.equal(SearchType.term, searchOption.getSearchType())) {
        queryBuilder = createTermQueryBuilder(field, values, searchOption.getBoost());
      }

      // queryString搜索
      if (Objects.equal(SearchType.queryString, searchOption.getSearchType())) {
        queryBuilder = createQueryStringBuilder(field, values,
            searchOption.getQueryStringPrecision(), searchOption.getBoost());
      }

      if (queryBuilder != null) {
        if (Objects.equal(QueryLogic.should, searchOption.getSearchLogic())) {
          boolQueryBuilder = boolQueryBuilder.should(queryBuilder);
        } else {
          boolQueryBuilder = boolQueryBuilder.must(queryBuilder);
        }
      }

      return boolQueryBuilder;
    } catch (Exception ex) {
      if (logger.isErrorEnabled()) {
        logger.error("create SingleFieldQueryBuilder failed", ex);
      }

      throw ex;
    }
  }

  /**
   * 构造queryString搜索
   * 
   * @param field
   * @param values
   * @param queryStringPrecision
   * @param boost
   * @return
   * @throws Exception
   */
  private QueryBuilder createQueryStringBuilder(String field, Object[] values,
      String queryStringPrecision, float boost) throws Exception {
    QueryBuilder queryBuilder = null;
    if (values != null && values.length > 0) {
      for (Object valueItem : values) {
        // 格式化搜索数据
        String formatValue = valueItem.toString().trim().replace("*", " ");
        if (formatValue.length() == 1) {
          // 如果搜索长度为1的非数字的字符串，格式化为通配符搜索
          if (!Pattern.matches("[0-9]", formatValue)) {
            formatValue = "*" + formatValue + "*";
          }
        }

        final QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders
            .queryString(formatValue).minimumShouldMatch(queryStringPrecision);

        if (StringUtils.isNotBlank(field)) {
          queryStringQueryBuilder.field(field);
        }

        queryBuilder = queryStringQueryBuilder.boost(boost);
      }
    }

    return queryBuilder;
  }

  /**
   * 构造term搜索
   * 
   * @param field
   * @param values
   * @param boost
   * @return
   * @throws Exception
   */
  private QueryBuilder createTermQueryBuilder(String field, Object[] values, float boost)
      throws Exception {
    if (StringUtils.isBlank(field)) {
      throw new Exception("term查询字段名不能为空");
    }

    QueryBuilder queryBuilder = null;
    if (values != null && values.length > 0) {
      for (Object valueItem : values) {
        // 格式化搜索数据
        String formatValue = valueItem.toString().trim().replace("*", " ");
        queryBuilder = QueryBuilders.termQuery(field, formatValue).boost(boost);
      }
    }

    return queryBuilder;
  }

  /**
   * 构造range搜索
   * 
   * @param field
   * @param values
   * @param searchOption
   * @return
   * @throws Exception
   */
  private RangeQueryBuilder createRangeQueryBuilder(String field, Object[] values,
      SearchOption searchOption) throws Exception {
    if (StringUtils.isBlank(field)) {
      throw new Exception("range查询字段名不能为空");
    }

    if (values == null) {
      return null;
    }

    if (values.length > 2) {
      throw new Exception("range查询字段值不能超过两个");
    }

    if (values.length == 1) {
      if (StringUtils.isBlank(values[0].toString().trim())) {
        throw new Exception("range查询字段值不能为空");
      }

      boolean timeType = false;
      if (SearchOption.isDate(values[0])) {
        timeType = true;
      }

      final String value;
      if (timeType) {
        value = SearchOption.formatDate(values[0]);
      } else {
        value = values[0].toString();
      }

      if (Objects.equal(searchOption.getRangeQueryType(), RangeQueryType.gt)) {
        return QueryBuilders.rangeQuery(field).gt(value);
      } else if (Objects.equal(searchOption.getRangeQueryType(), RangeQueryType.gte)) {
        return QueryBuilders.rangeQuery(field).gte(value);
      } else if (Objects.equal(searchOption.getRangeQueryType(), RangeQueryType.lt)) {
        return QueryBuilders.rangeQuery(field).lt(value);
      } else {
        return QueryBuilders.rangeQuery(field).lte(value);
      }
    } else {
      if (StringUtils.isBlank(values[0].toString().trim())
          || StringUtils.isBlank(values[1].toString().trim())) {
        throw new Exception("range查询字段值不能有空值");
      }

      boolean timeType = false;
      if (SearchOption.isDate(values[0])) {
        if (SearchOption.isDate(values[1])) {
          timeType = true;
        }
      }

      final String begin, end;
      if (timeType) {
        /*
         * 如果时间类型的区间搜索出现问题，有可能是数据类型导致的：
         * （1）在监控页面（elasticsearch-head）中进行range搜索，看看什么结果，如果也搜索不出来，则：（2）
         * （2）请确定mapping中是date类型，格式化格式是yyyy-MM-dd HH:mm:ss
         * （3）请确定索引里的值是类似2012-01-01 00:00:00的格式
         * （4）如果是从数据库导出的数据，请确定数据库字段是char或者varchar类型，而不是date类型（此类型可能会有问题）
         */
        begin = SearchOption.formatDate(values[0]);
        end = SearchOption.formatDate(values[1]);
      } else {
        begin = values[0].toString();
        end = values[1].toString();
      }

      return QueryBuilders.rangeQuery(field).from(begin).to(end);
    }
  }

  /**
   * 构造搜索结果
   *
   * @param searchResponse
   */
  private EsSearchResponse buildSearchResult(SearchResponse searchResponse) {
    EsSearchResponse response = null;
    if (searchResponse != null) {
      final List<Map<String, Object>> resultList = Lists.newArrayList();
      for (SearchHit searchHit : searchResponse.getHits()) {
        final Iterator<Entry<String, Object>> iterator = searchHit.getSource().entrySet()
            .iterator();
        final Map<String, Object> resultMap = Maps.newHashMap();
        while (iterator.hasNext()) {
          final Entry<String, Object> entry = iterator.next();
          resultMap.put(entry.getKey(), entry.getValue());
        }
        // final Map<String, HighlightField> highlightMap =
        // searchHit.highlightFields();
        // final Iterator<Entry<String, HighlightField>> highlightIterator =
        // highlightMap.entrySet()
        // .iterator();
        // while (highlightIterator.hasNext()) {
        // Entry<String, HighlightField> entry = highlightIterator.next();
        // final Object[] contents = entry.getValue().fragments();
        // if (contents.length == 1) {
        // resultMap.put(entry.getKey(), contents[0].toString());
        // }
        // }

        resultList.add(resultMap);
      }

      response = new EsSearchResponse();
      response.setResultJson(JSON.toJSONString(resultList));
      response.setCounts(resultList.size());
      response.setTotalCounts(searchResponse.getHits().getTotalHits());
      response.setTimedOut(searchResponse.isTimedOut());
      response.setTookInMillis(searchResponse.getTookInMillis());
      response.setSuccessfulShards(searchResponse.getSuccessfulShards());
      response.setFailedShards(searchResponse.getFailedShards());
      response.setTotalShards(searchResponse.getTotalShards());
      if (searchResponse.isTerminatedEarly() != null) {
        response.setTerminatedEarly(searchResponse.isTerminatedEarly());
      }
    }

    return response;
  }

  /**
   * 高亮显示
   * 
   * @param searchRequestBuilder
   * @param searchContents
   * @return
   */
  private SearchRequestBuilder createHighlight(SearchRequestBuilder searchRequestBuilder,
      List<SearchStrategy> searchContents) {
    // 循环每一个需要搜索的字段和值
    if (searchContents != null) {
      for (SearchStrategy searchContent : searchContents) {
        // String field = entry.getKey();
        final Object[] values = searchContent.getFieldValues();
        // 排除非法的搜索值
        if (!checkValue(values)) {
          if (logger.isWarnEnabled()) {
            logger.warn("搜素字段值不符合规范，搜索结果可能不准确。");
          }

          continue;
        }

        // 获得搜索类型
        final SearchOption searchOption = searchContent.getSearchOption();
        if (searchOption.isHighlight()) { // TODO
          /**
           * http://www.elasticsearch.org/guide/reference/api/search/
           * highlighting. html
           *
           * fragment_size设置成1000，默认值会造成返回的数据被截断
           */
          // searchRequestBuilder =
          // searchRequestBuilder.addHighlightedField(field, 1000)
          // .setHighlighterPreTags("<" + highlightCSS.split(",")[0] + ">")
          // .setHighlighterPostTags("</" + highlightCSS.split(",")[1] +
          // ">");
        }
      }
    }

    return searchRequestBuilder;
  }

  /**
   * 简单的值校验
   * 
   * @param values
   */
  private boolean checkValue(Object[] values) {
    if (values == null) {
      return false;
    } else if (values.length == 0) {
      return false;
    } else {
      for (Object value : values) {
        if (value == null || StringUtils.isBlank(value.toString().trim())) {
          return false;
        }
      }
    }

    return true;
  }

}
