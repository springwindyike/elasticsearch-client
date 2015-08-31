package com.hd123.jcrm.search.model;

/**
 * ElasticSearch关键字常量
 * 
 * @author liyue
 */
public enum ElasticSearchReservedWords {

  ANALYSIS("analysis"),

  FILTER("filter"),

  TYPE("type"),

  STOP("stop"),

  STOPWORDS_PATH("stopwords_path"),

  SNOWBALL("snowball"),

  LANGUAGE("language"),

  WORD_DELIMITER("word_delimiter"),

  PROTECTED_WORDS_PATH("protected_words_path"),

  TYPE_TABLE_PATH("type_table_path"),

  SYNONYM("synonym"),

  SYNONYMS_PATH("synonyms_path"),

  ANALYZER("analyzer"),

  CUSTOM("custom"),

  TOKENIZER("tokenizer"),

  WHITESPACE("whitespace"),

  LOWERCASE("lowercase"),

  CHAR_FILTER("char_filter"),

  HTML_STRIP("html_strip"),

  KEYWORD("keyword"),

  STANDARD("standard"),

  PROPERTIES("properties"),

  DATE("date"),

  DATE_FORMATS("date_formats"),

  FORMAT("format"),

  STORE("store"),

  YES("yes"),

  INDEX("index"),

  NOT_ANALYZED("not_analyzed"),

  FLOAT("float"),

  BOOLEAN("boolean"),

  STRING("string"),

  DOUBLE("double"),

  FIELDS("fields"),

  MULTI_FIELD("multi_field"),

  INDEX_MAPPER_DYNAMIC("index.mapper.dynamic"),

  DEFAULT("_default_"),

  DYNAMIC("dynamic"),

  SOURCE("_source"),

  ENABLED("enabled"),

  INTEGER("integer"),

  CLUSTER_NAME("cluster.name"),

  PATH_DATA("path.data"),

  PATH_WORK("path.work"),

  PATH_LOG("path.log"),

  PATH_CONF("path.conf"),

  NUMBER_OF_SHARDS("number_of_shards"),

  NUMBER_OF_REPLICAS("number_of_replicas"),

  ANALYZER_SIMPLE("simple"),

  SYNONYMS_IGNORE_CASE("ignore_case"),

  SYNONYMS_EXPAND("expand"),

  PING_TIMEOUT("ping_timeout"),

  NODES_SAMPLER_INTERVAL("nodes_sampler_interval"),

  IGNORE_CLUSTER_NAME("ignore_cluster_name"),

  CLIENT_TRANSPORT_SNIFF("client.transport.sniff"),

  CLIENT_TRANSPORT_PING_TIMEOUT("client.transport.ping_timeout"),

  CLIENT_TRANSPORT_NODES_SAMPLER_INTERVAL("client.transport.nodes_sampler_interval"),

  NESTED("nested");

  private String value;

  public String getValue() {
    return value;
  }

  private ElasticSearchReservedWords(String value) {
    this.value = value;
  }

}
