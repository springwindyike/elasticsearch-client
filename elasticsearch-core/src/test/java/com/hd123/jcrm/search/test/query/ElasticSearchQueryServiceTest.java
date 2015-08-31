package com.hd123.jcrm.search.test.query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.hd123.jcrm.search.api.query.EsSearchResponse;
import com.hd123.jcrm.search.model.Document;
import com.hd123.jcrm.search.model.IndexConfig;
import com.hd123.jcrm.search.model.SearchOption;
import com.hd123.jcrm.search.model.SearchOption.DataFilter;
import com.hd123.jcrm.search.model.SearchOption.QueryLogic;
import com.hd123.jcrm.search.model.SearchOption.RangeQueryType;
import com.hd123.jcrm.search.model.SearchOption.SearchType;
import com.hd123.jcrm.search.model.SearchStrategy;
import com.hd123.jcrm.search.model.SortOption;
import com.hd123.jcrm.search.model.SortOption.SortType;
import com.hd123.jcrm.search.test.Benchmark;
import com.hd123.jcrm.search.test.model.TestData;
import com.hd123.jcrm.search.test.model.TestData.Data;

/**
 * @author liyue
 */
@SuppressWarnings("unused")
public class ElasticSearchQueryServiceTest extends Benchmark {

  @Test
  public void Test() throws Exception {
    TestData t = new TestData();
    Document<TestData> d = new Document<TestData>("fdsf", t);
  }

  @Test
  public void search() throws Exception {
    final String indexNames[] = new String[] {
        "crm" };
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    final List<SearchStrategy> searchContens = Lists.newArrayList();
    final SearchStrategy searchContent1 = new SearchStrategy("cardNum", new Object[] {
        "0000029008" });
    final SearchOption opt = new SearchOption();
    opt.setSearchType(SearchType.term);
    final SearchStrategy searchContent2 = new SearchStrategy("cardType", new Object[] {
        300, 301 }, opt);
    final SearchOption opt2 = new SearchOption();
    final SearchOption opt3 = new SearchOption(SearchType.range, QueryLogic.should, DataFilter.all,
        1.0f, false);
    opt2.setSearchType(SearchType.range);
    final SearchStrategy searchContent3 = new SearchStrategy("cardNum", new Object[] {
        "0000029008", "0000029018" }, opt3);
    final SearchStrategy searchContent4 = new SearchStrategy("bytime", new Object[] {
        format.parse("2015-01-01 16:48:02"), new Date() }, opt2);
    opt2.setSearchType(SearchType.queryString);
    final SearchStrategy searchContent5 = new SearchStrategy("", new Object[] {
        "使用中" }, opt2);
    opt3.setRangeQueryType(RangeQueryType.lt);
    final SearchStrategy searchContent6 = new SearchStrategy("cardNum", new Object[] {
        "0000029008" }, opt3);
    searchContens.add(searchContent1);
    // searchContens.add(searchContent2);
    searchContens.add(searchContent3);
    // searchContens.add(searchContent4);
    // searchContens.add(searchContent5);
    // searchContens.add(searchContent6);

    final QueryLogic searchLogic = QueryLogic.should;
    final int from = 0, size = 100;
    final List<SearchStrategy> filterContents = Lists.newArrayList();
    // final SearchStrategy filterContent1 = new SearchStrategy("id", new
    // Object[] {
    // 6 });
    // filterContents.add(filterContent1);

    final QueryLogic filterLogic = QueryLogic.must;
    final SortOption sortOption = new SortOption("cardNum", SortType.desc);
    final EsSearchResponse result = elasticSearchQueryService.search(indexNames, null,
        searchContens, searchLogic, from, size, sortOption, filterContents, filterLogic);

    // final List<TestData> datas = new ArrayList<TestData>();
    // for (Map<String, Object> map : result) {
    // final TestData data = JSON.parseObject(JSON.toJSONString(map),
    // TestData.class);
    // datas.add(data);
    // }

    System.out.println("\n===========================result=========================\n");
    // System.out.println(JSON.toJSONString(datas));
    System.out.println(JSON.toJSONString(result) + "\n");
    System.out
        .println(JSON.toJSONString(JSON.parseObject(result.getResultJson(), List.class)) + "\n");
    final long count = elasticSearchQueryService.getCount(indexNames, null, searchContens,
        searchLogic, filterContents, filterLogic);
    System.out.println("===========================Count:" + count + "=========================\n");
  }

  @Test
  public void testCountSearch() throws Exception {
    final String indexNames[] = new String[] {
        "crm" };
    // final EsSearchResponse result =
    // elasticSearchQueryService.search(indexNames, null, null, null,
    // -2, 20000, null, null, null);
    // System.out.println(JSON.toJSONString(result) + "\n");
    // System.out
    // .println(JSON.toJSONString(JSON.parseObject(result.getResultJson(),
    // List.class)) + "\n");
    final String query = "{\"query_string\":{\"query\":\"0000029008\",\"fields\":[\"cardNum\"],\"boost\":1.0,\"minimum_should_match\":\"100\"}}";
    final long c = elasticSearchQueryService.getCount(indexNames, null, query);
    System.out.println(c);
  }

  @Test
  public void testIndex() throws Exception {
    IndexConfig config = new IndexConfig("test", "testData");
    List<TestData> datas = Lists.newArrayList();

    for (int i = 5; i < 9; i++) {
      TestData data = new TestData();
      data.setId(i);
      data.setName("第" + i + "个");
      data.setDescription("第" + i + "个测试数据。");
      ArrayList<String> childs = Lists.newArrayList();
      ArrayList<Data> dtas = Lists.newArrayList();
      for (int j = 0; j < 4; j++) {
        childs.add("第" + j + "个child");
        Data d = new Data();
        d.setDataId(j);
        d.setDataName("第" + j + "个data");
        if (j == 1) {
          d.setIsData(Boolean.TRUE);
        }
        dtas.add(d);
      }
      data.setChilds(childs);
      data.setDatas(dtas);
      datas.add(data);
    }

    elasticSearchIndexDataService.indexDatas(config, datas);
  }

}
