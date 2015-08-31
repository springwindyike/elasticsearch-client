package com.hd123.jcrm.search.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 搜索选项定义
 * 
 * @author liyue
 */
@XmlRootElement
public final class SearchOption implements Serializable {
  private static final long serialVersionUID = 72320713782868638L;

  private QueryLogic searchLogic = QueryLogic.should;
  private SearchType searchType = SearchType.queryString;
  private DataFilter dataFilter = DataFilter.all;
  private RangeQueryType rangeQueryType = RangeQueryType.gte;
  /** querystring精度，取值[1-100]的整数 */
  private String queryStringPrecision = "100";
  /** 排名权重 */
  private float boost = 1.0f;
  /** 是否高亮 */
  private boolean highlight = false;

  public SearchOption() {
  }

  public SearchOption(SearchType searchType, QueryLogic queryLogic, DataFilter dataFilter,
      float boost, boolean highlight) throws Exception {
    if (Objects.equals(SearchType.queryString, searchType)) {
      setQueryStringPrecision("100");
    }

    if (Objects.equals(SearchType.range, searchType)) {
      setRangeQueryType(RangeQueryType.gte);
    }

    this.setSearchLogic(queryLogic);
    this.setSearchType(searchType);
    this.setDataFilter(dataFilter);
    this.setBoost(boost);
    this.setHighlight(highlight);
  }

  /**
   * 搜索类型
   * 
   * @author liyue
   */
  public enum SearchType {

    /** 按照词组搜索，搜索一个词时候使用 */
    term,

    /** 按照区间搜索 */
    range,

    /** 按照quert_string搜索，搜索非词组时候使用 */
    queryString
  }

  /**
   * 搜索条件之间的逻辑关系
   * <p>
   * must表示条件必须都满足，should表示只要有一个条件满足就可以
   * 
   * @author liyue
   */
  public enum QueryLogic {

    /** 逻辑must关系 */
    must,

    /** 逻辑should关系 */
    should
  }

  /**
   * 区间搜索类型
   * 
   * @author liyue
   */
  public enum RangeQueryType {

    /** 大于 */
    gt,

    /** 大于等于 */
    gte,

    /** 小于 */
    lt,

    /** 小于等于 */
    lte,

    /** 范围 */
    range
  }

  /**
   * 数据过滤
   * 
   * @author liyue
   */
  public enum DataFilter {

    /** 只显示有值的 */
    exists,

    /** 显示没有值的 */
    notExists,

    /** 显示全部 */
    all
  }

  public DataFilter getDataFilter() {
    return this.dataFilter;
  }

  public void setDataFilter(DataFilter dataFilter) {
    this.dataFilter = dataFilter;
  }

  public boolean isHighlight() {
    return this.highlight;
  }

  public void setHighlight(boolean highlight) {
    this.highlight = highlight;
  }

  public float getBoost() {
    return this.boost;
  }

  public void setBoost(float boost) {
    this.boost = boost;
  }

  public QueryLogic getSearchLogic() {
    return this.searchLogic;
  }

  public void setSearchLogic(QueryLogic searchLogic) {
    this.searchLogic = searchLogic;
  }

  public SearchType getSearchType() {
    return this.searchType;
  }

  public void setSearchType(SearchType searchType) {
    this.searchType = searchType;
  }

  public RangeQueryType getRangeQueryType() {
    return rangeQueryType;
  }

  public void setRangeQueryType(RangeQueryType rangeQueryType) {
    this.rangeQueryType = rangeQueryType;
  }

  public String getQueryStringPrecision() {
    return this.queryStringPrecision;
  }

  public void setQueryStringPrecision(String queryStringPrecision) throws Exception {
    final int precision = Integer.valueOf(queryStringPrecision);
    if (precision < 1 || precision > 100) {
      throw new Exception("querystring精度不在范围内，应为[1-100]区间内的整数。");
    }
    this.queryStringPrecision = queryStringPrecision;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  /**
   * 格式化查询日期
   * 
   * @param object
   *          日期对象
   * @return 日期字符串
   */
  public static String formatDate(Object object) {
    if (object instanceof java.util.Date) {
      return SearchOption.formatDateFromDate((java.util.Date) object);
    }
    return SearchOption.formatDateFromString(object.toString());
  }

  /**
   * 判断是否为日期格式
   * 
   * @param object
   *          对象
   * @return 是否为日期格式
   */
  public static boolean isDate(Object object) {
    return object instanceof java.util.Date
        || Pattern.matches("[1-3][0-9][0-9][0-9]-[0-1][0-2]+", object.toString());
  }

  private static String formatDateFromDate(Date date) {
    SimpleDateFormat dateFormat_hms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      final String result = dateFormat_hms.format(date);
      return result;
    } catch (Exception e) {
      // continue
    }
    try {
      final String result = dateFormat.format(date) + "00:00:00";
      return result;
    } catch (Exception e) {
      // continue
    }
    return dateFormat_hms.format(new Date());
  }

  private static String formatDateFromString(String date) {
    SimpleDateFormat dateFormat_hms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      final Date value = dateFormat_hms.parse(date);
      return SearchOption.formatDateFromDate(value);
    } catch (Exception e) {
      // continue
    }
    try {
      final Date value = dateFormat.parse(date);
      return SearchOption.formatDateFromDate(value);
    } catch (Exception e) {
      // continue
    }
    return dateFormat_hms.format(new Date());
  }

}