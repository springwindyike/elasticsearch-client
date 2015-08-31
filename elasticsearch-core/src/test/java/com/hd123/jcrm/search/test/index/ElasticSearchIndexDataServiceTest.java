package com.hd123.jcrm.search.test.index;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.hd123.jcrm.search.model.IndexConfig;
import com.hd123.jcrm.search.test.Benchmark;
import com.hd123.jcrm.search.test.model.TestData;
import com.hd123.jcrm.search.test.model.TestData.Data;

/**
 * @author liyue
 */
public class ElasticSearchIndexDataServiceTest extends Benchmark {

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
