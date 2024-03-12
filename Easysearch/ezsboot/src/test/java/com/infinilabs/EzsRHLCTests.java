package com.infinilabs;

import com.alibaba.fastjson2.JSON;
import com.infinilabs.utils.LogUtil;
import org.easysearch.action.admin.indices.delete.DeleteIndexRequest;
import org.easysearch.action.support.master.AcknowledgedResponse;
import org.easysearch.client.RequestOptions;
import org.easysearch.client.RestHighLevelClient;
import org.easysearch.client.indices.CreateIndexRequest;
import org.easysearch.client.indices.CreateIndexResponse;
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
}