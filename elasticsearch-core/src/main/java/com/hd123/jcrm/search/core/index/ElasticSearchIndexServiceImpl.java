package com.hd123.jcrm.search.core.index;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;

import javax.persistence.Id;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.hd123.jcrm.search.api.index.ElasticSearchIndexService;
import com.hd123.jcrm.search.core.BaseSearchCluster;
import com.hd123.jcrm.search.model.Document;
import com.hd123.jcrm.search.model.IndexConfig;
import com.hd123.jcrm.search.util.IndexUtils;

/**
 * 索引服务实现
 * 
 * @author liyue
 */
@Service(value = "elasticSearchIndexService")
public class ElasticSearchIndexServiceImpl extends BaseSearchCluster
    implements ElasticSearchIndexService {

  @Override
  @Async
  public <T> void indexDatas(IndexConfig indexConfig, List<T> datas) throws Exception {
    assert indexConfig != null;
    assert StringUtils.isNotBlank(indexConfig.getIndexAliasName());
    assert StringUtils.isNotBlank(indexConfig.getDocumentType());
    assert datas != null;

    if (datas.isEmpty()) {
      return;
    }

    if (logger.isInfoEnabled()) {
      logger.info("Indexing bulk data request, for size:" + datas.size());
    }

    // 将所有可能的日期类型统一转换
    final SerializeConfig serializeConfige = new SerializeConfig();
    final SimpleDateFormatSerializer simpleDateFormatSerializer = new SimpleDateFormatSerializer(
        "yyyy-MM-dd HH:mm:ss");
    serializeConfige.put(java.util.Date.class, simpleDateFormatSerializer);
    serializeConfige.put(java.sql.Date.class, simpleDateFormatSerializer);
    serializeConfige.put(java.security.Timestamp.class, simpleDateFormatSerializer);
    serializeConfige.put(java.sql.Timestamp.class, simpleDateFormatSerializer);
    serializeConfige.put(org.elasticsearch.cluster.metadata.MappingMetaData.Timestamp.class,
        simpleDateFormatSerializer);

    final List<IndexRequestBuilder> requests = Lists.newArrayList();
    for (T data : datas) {
      try {
        final Class<?> clazz = data.getClass();
        final Method[] methods = clazz.getMethods();

        // 文档id
        String id = StringUtils.EMPTY;

        // 注解方法
        for (Method method : methods) {
          if (method.isAnnotationPresent(Id.class)) {
            method.setAccessible(true);
            id += method.invoke(data).toString();
          }
        }

        // 注解属性(不包含父类)
        if (StringUtils.isBlank(id)) {
          final Field[] fields = clazz.getDeclaredFields();
          for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
              field.setAccessible(true);
              id = field.get(data).toString();
              break;
            }
          }
        }

        // 取hashcode
        if (StringUtils.isBlank(id)) {
          id = String.valueOf(Objects.hashCode(data));
        }

        final String json = JSON.toJSONString(data, serializeConfige);
        final IndexRequestBuilder indexRequestBuilder = getClient()
            .prepareIndex(indexConfig.getIndexAliasName(), indexConfig.getDocumentType(), id)
            .setSource(json);
        requests.add(indexRequestBuilder);
      } catch (Exception ex) {
        if (logger.isErrorEnabled()) {
          logger.error(
              "Error occurred while creating index document for data, moving to next data!", ex);
        }

        continue;
      }
    }

    processBulkRequests(requests);
    refreshServer();
  }

  @Override
  @Async
  public <T> void indexWithCustomDocuments(IndexConfig indexConfig, List<Document<T>> documents)
      throws Exception {
    assert indexConfig != null;
    assert StringUtils.isNotBlank(indexConfig.getIndexAliasName());
    assert StringUtils.isNotBlank(indexConfig.getDocumentType());
    assert documents != null;

    if (documents.isEmpty()) {
      return;
    }

    if (logger.isInfoEnabled()) {
      logger.info("Indexing bulk data request, for size:" + documents.size());
    }

    // 将所有可能的日期类型统一转换
    final SerializeConfig serializeConfige = new SerializeConfig();
    final SimpleDateFormatSerializer simpleDateFormatSerializer = new SimpleDateFormatSerializer(
        "yyyy-MM-dd HH:mm:ss");
    serializeConfige.put(java.util.Date.class, simpleDateFormatSerializer);
    serializeConfige.put(java.sql.Date.class, simpleDateFormatSerializer);
    serializeConfige.put(java.security.Timestamp.class, simpleDateFormatSerializer);
    serializeConfige.put(java.sql.Timestamp.class, simpleDateFormatSerializer);
    serializeConfige.put(org.elasticsearch.cluster.metadata.MappingMetaData.Timestamp.class,
        simpleDateFormatSerializer);

    final List<IndexRequestBuilder> requests = Lists.newArrayList();
    for (Document<T> document : documents) {
      try {
        final String json = JSON.toJSONString(document.getDocument(), serializeConfige);
        final IndexRequestBuilder indexRequestBuilder = getClient()
            .prepareIndex(indexConfig.getIndexAliasName(), indexConfig.getDocumentType(),
                document.getId())
            .setSource(json);
        requests.add(indexRequestBuilder);
      } catch (Exception ex) {
        if (logger.isErrorEnabled()) {
          logger.error(
              "Error occurred while creating index document for data, moving to next data!", ex);
        }

        continue;
      }
    }

    processBulkRequests(requests);
    refreshServer();
  }

  @Override
  @Async
  public void indexOptimize() throws Exception {
    final BulkRequestBuilder bulkRequestBuilder = getClient().prepareBulk();
    /*
     * 索引段即lucene中的segments概念，我们知道ES索引过程中会refresh和tranlog也就是说我们在索引过程中segments
     * number不至一个。而segments number与检索是有直接联系的，segments number越多检索越慢，而将segments
     * numbers 有可能的情况下保证为1这将可以提到将近一半的检索速度。
     */
    getClient().admin().indices().prepareOptimize().setMaxNumSegments(1)
        // 立即返回
        .setWaitForMerge(false).setFlush(true).execute().actionGet();
    bulkRequestBuilder.execute().actionGet();
    refreshServer();
  }

  @Override
  @Async
  @SuppressWarnings("unused")
  public void dropIndex(String indexName, String documentType, String documentId) throws Exception {
    IndexUtils.checkIndex(indexName);

    // 检查是否存在索引
    final IndicesExistsRequest ier = new IndicesExistsRequest();
    ier.indices(new String[] {
        indexName });
    final boolean exists = getClient().admin().indices().exists(ier).actionGet().isExists();

    if (exists) {
      if (StringUtils.isNotBlank(documentType)) {
        // 删除Id对应的文档
        if (StringUtils.isNotBlank(documentId)) {
          final DeleteResponse response = getClient()
              .prepareDelete(indexName, documentType, documentId).setOperationThreaded(false)
              .execute().actionGet();
        } else {
          // 查询所有的documents
          final MatchAllQueryBuilder allQueryBuilder = QueryBuilders.matchAllQuery();
          // 删除所有documentType的文档
          final DeleteByQueryResponse response = getClient().prepareDeleteByQuery(indexName)
              .setQuery(allQueryBuilder).setTypes(documentType).execute().actionGet();
        }
      } else {
        if (StringUtils.isBlank(documentId)) {
          // 删除整个索引
          final DeleteIndexResponse response = getClient().admin().indices()
              .prepareDelete(indexName).execute().actionGet();
        } else {
          throw new Exception("参数不合法，文档类型为空时文档Id也需为空");
        }
      }

      refreshServer();
    } else {
      if (logger.isInfoEnabled()) {
        logger.info(MessageFormat.format("index {0} not exists", indexName));
      }

      return;
    }
  }

  /**
   * 处理批量索引请求
   * 
   * @param requests
   * @return
   */
  private BulkResponse processBulkRequests(List<IndexRequestBuilder> requests) {
    if (requests.size() > 0) {
      final BulkRequestBuilder bulkRequest = getClient().prepareBulk();

      // 批量处理请求
      for (IndexRequestBuilder indexRequestBuilder : requests) {
        bulkRequest.add(indexRequestBuilder);
      }

      if (logger.isInfoEnabled()) {
        logger.info("Executing bulk index request for size:" + requests.size());
      }

      // 执行索引
      final BulkResponse bulkResponse = bulkRequest.execute().actionGet();

      if (logger.isInfoEnabled()) {
        logger.info(
            "Bulk operation data index response total items is:" + bulkResponse.getItems().length);
      }

      if (bulkResponse.hasFailures()) {
        if (logger.isErrorEnabled()) {
          // process failures by iterating through each bulk response item
          logger
              .error("bulk operation indexing has failures:" + bulkResponse.buildFailureMessage());
        }
      }

      bulkRequest.request().requests().clear();

      return bulkResponse;
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Executing bulk index request for size: 0");
      }

      return null;
    }
  }

}
