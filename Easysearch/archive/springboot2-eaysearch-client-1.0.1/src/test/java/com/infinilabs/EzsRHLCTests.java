package com.infinilabs;

import com.alibaba.fastjson2.JSON;
import com.infinilabs.utils.EzsUtil;
import com.infinilabs.utils.LogUtil;
import com.infinilabs.utils.StringUtil;
import org.easysearch.action.admin.indices.delete.DeleteIndexRequest;
import org.easysearch.action.index.IndexRequest;
import org.easysearch.action.index.IndexResponse;
import org.easysearch.action.search.SearchRequest;
import org.easysearch.action.search.SearchResponse;
import org.easysearch.action.support.master.AcknowledgedResponse;
import org.easysearch.client.RequestOptions;
import org.easysearch.client.RestHighLevelClient;
import org.easysearch.client.indices.CreateIndexRequest;
import org.easysearch.client.indices.CreateIndexResponse;
import org.easysearch.common.xcontent.XContentType;
import org.easysearch.index.query.QueryBuilder;
import org.easysearch.index.query.QueryBuilders;
import org.easysearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class EzsRHLCTests {

    @Autowired
    @Qualifier(value = "restHighLevelClient")
    private RestHighLevelClient client;

    @Test
    void testCreateIndex() throws IOException {
        // 1.创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("infinilabs_idx");

        // 2.客户端执行请求
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);

        LogUtil.formatInfo("Easysearch 索引创建结果：%s", JSON.toJSONString(response));
    }

    @Test
    void testDeleteIndex() throws IOException {
        // 1.创建索引请求
        DeleteIndexRequest request = new DeleteIndexRequest("infinilabs_idx");

        // 2.客户端执行请求
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);

        LogUtil.formatInfo("Easysearch 索引删除结果：%s", JSON.toJSONString(response));
    }

    @Test
    public void testIndexing() throws IOException {
        String jsonStr = "{\"name\": \"张三\", \"address\": \"北京市\"}";
        String indexName = "infinilabs_idx";
        IndexRequest indexRequest = new IndexRequest(indexName);
        // 不设置id 会自动使用es默认的
        indexRequest.source(jsonStr, XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void testSearch() throws IOException {
        String indexName = "infinilabs_idx";
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchAllQuery()).size(10);

        sourceBuilder.fetchSource(new String[]{"name", "address"}, new String[]{});
        SearchRequest request = new SearchRequest(indexName).source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
    }
}
