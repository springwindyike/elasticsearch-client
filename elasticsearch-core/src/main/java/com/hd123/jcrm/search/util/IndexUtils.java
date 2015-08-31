package com.hd123.jcrm.search.util;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 索引工具类
 * 
 * @author liyue
 */
public class IndexUtils {

  /**
   * 验证索引名称合法性
   * 
   * @param indexName
   * @throws Exception
   */
  public static void checkIndex(String indexName) throws Exception {
    assert indexName != null;

    // elasticsearch要求索引名称全为小写字母
    final String REGEX_INDEX_NAME = "^[a-z]+$";
    final Pattern pattern = Pattern.compile(REGEX_INDEX_NAME);
    final Matcher matcher = pattern.matcher(indexName);
    if (!matcher.matches()) {
      throw new Exception(MessageFormat.format("索引名称{0}应全为小写字母。", indexName));
    }
  }

}
