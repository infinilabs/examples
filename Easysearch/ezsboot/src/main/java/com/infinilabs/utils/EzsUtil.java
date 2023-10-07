package com.infinilabs.utils;

import com.infinilabs.model.P;
import com.infinilabs.model.PageForm;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class EzsUtil {
    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 对聚合结果进行封装
     */
    private static List<Object> handleResult(SearchResponse agg) {
        Map<String, Aggregation> aggregations = agg.getAggregations().asMap();
        List<Object> objects = new ArrayList<>();
        // 第一层分组统计
        aggregations.forEach((k, v) -> {
            Map<String, Object> group = new HashMap<>();
            parseAggs(k, v, group, objects);
        });
        return objects;
    }

    /**
     * 解析聚合结果
     */
    private static void parseAggs(String key, Aggregation value, Map<String, Object> group, List<Object> objects) {
        if (value instanceof Terms) {
            for (Terms.Bucket bucket : ((Terms) value).getBuckets()) {
                Set<Map.Entry<String, Aggregation>> entries = bucket.getAggregations().asMap().entrySet();
                group.put(key, bucket.getKeyAsString());
                for (Map.Entry<String, Aggregation> entry : entries) {
                    if (entry.getValue() instanceof Terms) {
                        parseAggs(entry.getKey(), entry.getValue(), group, objects);
                    } else {
                        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                        bucket.getAggregations().asMap().forEach((k2, v2) -> map.put(k2, getValue(v2)));
                        map.putAll(group);
                        objects.add(map);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 取值
     */
    private static String getValue(Aggregation agg) {
        switch (agg.getType()) {
            case "avg":
                return String.valueOf(((Avg) agg).getValue());
            case "sum":
                return String.valueOf(((Sum) agg).getValue());
            case "value_count":
                return String.valueOf(((ValueCount) agg).getValue());
            case "min":
                return String.valueOf(((Min) agg).getValue());
            case "max":
                return String.valueOf(((Max) agg).getValue());
            case "cardinality":
                return String.valueOf(((Cardinality) agg).getValue());
            default:
                return String.valueOf(agg);
        }
    }

    /**
     * 创建索引
     */
    @SneakyThrows
    public Boolean createIndex(String indexName) {
        //1.创建索引请求
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        // request.settings()  可以设置分片规则
        // request.mapping() 可以设置映射字段
        //2.执行客户端请求
        CreateIndexResponse indexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        return indexResponse.isAcknowledged();
    }

    /**
     * 查询索引  * 查询全部
     */
    @SneakyThrows
    public List<String> searchIndex(String indexName) {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if (!exists) {
            return new ArrayList<>();
        }
        GetIndexResponse getIndexResponse = restHighLevelClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);
        return Arrays.asList(getIndexResponse.getIndices());
    }

    /**
     * 删除索引
     */
    @SneakyThrows
    public Boolean deleteIndex(String indexName) {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if (!exists) {
            return false;
        }
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        AcknowledgedResponse response = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }

    /**
     * 文档数据插入(插入前会自动生成index)
     * source()  支持json map 键值对等形式
     */
    @SneakyThrows
    public String insert(String indexName, String id, String jsonStr) {
        IndexRequest indexRequest = new IndexRequest(indexName);
        // 不设置id 会自动使用es默认的
        if (StringUtil.isNotBlank(id)) {
            indexRequest.id(id);
        }
        indexRequest.source(jsonStr, XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        return indexResponse.getId();
    }

    /**
     * 批量插入数据
     * map {id, json}
     */
    @SneakyThrows
    public BulkResponse insertBatch(String indexName, Map<String, String> valueMap) {
        BulkRequest bulkRequest = new BulkRequest();
        Set<String> keySet = valueMap.keySet();
        for (String id : keySet) {
            IndexRequest request = new IndexRequest(indexName).id(id).source(valueMap.get(id), XContentType.JSON);
            bulkRequest.add(request);
        }
        return restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    /**
     * 更新- es有则更新，无则写入
     * 可以接受 String、Map、XContentBuilder 或 Object 键对
     */
    @SneakyThrows
    public String update(String indexName, String id, String jsonStr) {
        String searchById = searchById(indexName, id);
        if (StringUtil.isBlank(searchById)) {
            return null;
        }
        UpdateRequest updateRequest = new UpdateRequest(indexName, id).doc(jsonStr, XContentType.JSON);
        UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        return update.getId();
    }

    /**
     * 根据id进行删除
     */
    @SneakyThrows
    public String delete(String indexName, String id) {
        DeleteRequest deleteRequest = new DeleteRequest(indexName, id);
        DeleteResponse delete = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        return delete.getId();
    }

    /**
     * 根据id进行删除
     */
    @SneakyThrows
    public List<String> deleteBatch(String indexName, List<String> ids) {
        List<String> deleteList = new ArrayList<>();
        if (CollectionUtils.isEmpty(ids)) {
            return deleteList;
        }
        for (String id : ids) {
            DeleteRequest deleteRequest = new DeleteRequest(indexName, id);
            deleteList.add(restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT).getId());
        }
        return deleteList;
    }

    /**
     * 根据id进行查询
     */
    @SneakyThrows
    public String searchById(String indexName, String id) {
        GetRequest getRequest = new GetRequest(indexName, id);
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        return getResponse.getSourceAsString();
    }

    /**
     * 根据QueryBuilder来查询全部的条数
     */
    @SneakyThrows
    public Long findTotal(String indexName, QueryBuilder builder) {
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source().query(builder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return response.getHits().getTotalHits().value;
    }

    /**
     * 1, id查询: QueryBuilders.idsQuery().ids(id)
     * 精确查询 QueryBuilders.termQuery("userName.keyword", "王五")  .keyword 值是中文时需要，非中文时不需要
     * 范围查询：QueryBuilders.rangeQuery().form().to().includeLower(false).includeUpper(false) 默认是true包含头尾，设置false去掉头尾
     * 匹配所有：QueryBuilders.matchAllQuery()
     * 模糊查询：QueryBuilders.fuzzyQuery()
     * 全文检索，会进行分词,多个字段检索：QueryBuilders.multiMatchQuery("kimchy", "name", "description") 查询name或description包含kimchy
     * 全文检索，会进行分词,单字段检索：QueryBuilders.matchQuery(name", "kimchy") 查询name包含kimchy
     * 通配符查询, 支持*，匹配任何字符序列, 包括空，避免* 开始 QueryBuilders.wildcardQuery("user", "ki*hy")
     * 跨度查询：QueryBuilders.span………
     * 2，组合查询:BoolQueryBuilder  must：and  mustNot:not  should:or  in：termsQuery传list
     * QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("name", Lists.newArrayList())).mustNot(QueryBuilders.……);
     * 过滤器查询：在原本查询结果的基础上对数据进行筛选，不会计算分值,所以效率比must更高
     * QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("userName", "王五"))
     * 3, 查询部分字段: SearchSourceBuilder().fetchSource(new String[]{"userName", "age"}, new String[]{}) 查询userName和age字段
     * 4, 权重计算，权重越高月靠前:  给name精确查询提高权重为2 QueryBuilders.termQuery("name", "kimchy").boost(2.0f)
     * 高于设定分数, 不计算相关性查询: QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("name", "kimchy")).boost(2.0f);
     * 5，Nested&Join父子类型：得检索效率慢，不建议在ES做Join操作
     * 父子查询：QueryBuilders.hasChildQuery("tweet", QueryBuilders.termQuery("user", "kimchy")).scoreMode("max")
     * 嵌套查询, 内嵌文档查询 QueryBuilders.nestedQuery("location", QueryBuilders.boolQuery()
     * .must(QueryBuilders.matchQuery("location.lat", 0.962590433140581))
     * .must(QueryBuilders.rangeQuery("location.lon").lt(36.0000).gt(0.000))).scoreMode("total")
     * 6, 排序：在查询的结果上进行二次排序，date、float 等类型添加排序，text类型的字段不允许排序 SearchSourceBuilder.sort()
     */
    @SneakyThrows
    public List<Map<String, Object>> findAll(String indexName, QueryBuilder builder) {
        // 设置源字段过虑,第一个参数结果集包括哪些字段，第二个参数表示结果集不包括哪些字段
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(builder)
                .fetchSource(new String[]{"userName", "age"}, new String[]{});
        SearchRequest request = new SearchRequest(indexName).source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        return handleResponse(response, 0, 0).getRecords();
    }

    /**
     * 1，from-size浅分页适合数据量不大的情况（官网推荐是数据少于10000条），可以跳码进行查询
     * 2，scroll 是一种滚屏形式的分页检索，scroll查询是很耗性能的方式，不建议在实时查询中运用
     */
    @SneakyThrows
    public P<Map<String, Object>> fromSizePage(String indexName, QueryBuilder queryBuilder, PageForm PageForm) {
        int from = PageForm.getPageSize() * (PageForm.getPageNum() - 1);
        // 构建分页搜寻器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder)
                .from(from)
                .size(PageForm.getPageSize());
        if (StringUtil.isNotBlank(PageForm.getOrderBy())) {
            sourceBuilder.sort(PageForm.getOrderBy(), PageForm.getOrderType() ? SortOrder.ASC : SortOrder.DESC);
        }
        SearchRequest request = new SearchRequest(indexName).source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        return handleResponse(response, PageForm.getPageNum(), PageForm.getPageSize());
    }

    /**
     * 分页返回值处理
     */
    @SneakyThrows
    private P<Map<String, Object>> handleResponse(SearchResponse response, int pageNum, int pageSize) {
        SearchHit[] hits = response.getHits().getHits();
        List<Map<String, Object>> result = Arrays.stream(hits).map(h -> {
            Map<String, Object> sourceAsMap = h.getSourceAsMap();
            sourceAsMap.put("id", h.getId());
            return sourceAsMap;
        }).collect(Collectors.toList());
        return new P<>(result, response.getHits().getTotalHits().value, pageSize, pageNum);
    }

    /**
     * search_after 适用于深度分页+ 排序，分页是根据上一页最后一条数据来定位下一页的位置，所以无法跳页请求，性能最好
     */
    @SneakyThrows
    public P<Map<String, Object>> searchAfterPage(String indexName, QueryBuilder queryBuilder, PageForm PageForm) {
        // 构建分页搜寻器
        // searchAfter需要将from设置为0或-1，当然也可以不写

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder)
                .from(0)
                .size(PageForm.getPageSize());
        if (StringUtil.isNotBlank(PageForm.getOrderBy())) {
            sourceBuilder.sort(PageForm.getOrderBy(), PageForm.getOrderType() ? SortOrder.ASC : SortOrder.DESC);
        }
        if (null != PageForm.getSortCursor() && PageForm.getSortCursor().length > 0) {
            sourceBuilder.searchAfter(PageForm.getSortCursor());
        }
        SearchRequest request = new SearchRequest(indexName).source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = response.getHits().getHits();
        Object[] sortCursor = hits[hits.length - 1].getSortValues();
        P<Map<String, Object>> page = handleResponse(response, PageForm.getPageNum(), PageForm.getPageSize());
        page.setSortCursor(sortCursor);
        return page;
    }

    /**
     * moreLikeThisQuery: 实现基于内容推荐, 支持实现一句话相似文章查询
     * percent_terms_to_match：匹配项（term）的百分比，默认是0.3
     * min_term_freq：一篇文档中一个词语至少出现次数，小于这个值的词将被忽略，默认是2
     * max_query_terms：一条查询语句中允许最多查询词语的个数，默认是25
     * stop_words：设置停止词，匹配时会忽略停止词
     * min_doc_freq：一个词语最少在多少篇文档中出现，小于这个值的词会将被忽略，默认是无限制
     * max_doc_freq：一个词语最多在多少篇文档中出现，大于这个值的词会将被忽略，默认是无限制
     * min_word_len：最小的词语长度，默认是0
     * max_word_len：最多的词语长度，默认无限制
     * boost_terms：设置词语权重，默认是1
     * boost：设置查询权重，默认是1
     * analyzer：设置使用的分词器，默认是使用该字段指定的分词器
     */
    public List<Map<String, Object>> moreLikeThisQuery(String indexName, String words) {
        QueryBuilder queryBuilder = QueryBuilders.moreLikeThisQuery(new String[]{words})
                .minTermFreq(1).maxQueryTerms(3);
        return findAll(indexName, queryBuilder);
    }

    /**
     * 聚合查询: TODO 字段类型是text就不支持聚合排序
     * 桶（bucket）: 满足特定条件的文档的集合  GROUP BY userName
     * 指标（metric）: 对桶内的文档进行聚合分析的操作 COUNT(userName)
     * select age, createTime, SUM(age), AVG(age),MIN(age),COUNT(age) from user_index GROUP BY age, createTime
     */
    @SneakyThrows
    public List<Object> aggregateQuery(String indexName, List<String> fieldList, TermsAggregationBuilder aggregation) {
        if (CollectionUtils.isEmpty(fieldList)) {
            return new ArrayList<>();
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().aggregation(aggregation);
        SearchRequest request = new SearchRequest(indexName).source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        return handleResult(response);
    }
}
