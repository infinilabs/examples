package com.infinilabs.constants;

public interface BaseEzsConstants {
    /**
     * 默认的当前页码
     */
    Integer PAGE_NUM = 1;
    /**
     * 默认的每页显示条目数
     */
    Integer PAGE_SIZE = 10;
    /**
     * 逗号
     */
    String COMMA = ",";
    /**
     * 冒号
     */
    String COLON = ":";
    /**
     * 百分号
     */
    String PERCENT_SIGN = "%";
    /**
     * 分片数量字段
     */
    String SHARDS_FIELD = "index.number_of_shards";
    /**
     * 副本数量字段
     */
    String REPLICAS_FIELD = "index.number_of_replicas";
    /**
     * 最大返回个数字段
     */
    String MAX_RESULT_WINDOW_FIELD = "index.max_result_window";
    /**
     * 通配符
     */
    String WILDCARD_SIGN = "*";
    /**
     * ezs默认schema
     */
    String DEFAULT_SCHEMA = "https";
    /**
     * ezs默认schema
     */
    String HTTP_SCHEMA = "http";
    /**
     * 默认返回数
     */
    Integer DEFAULT_SIZE = 10000;
    /**
     * 未知的版本号
     */
    String UNKNOWN = "unknown";
}
