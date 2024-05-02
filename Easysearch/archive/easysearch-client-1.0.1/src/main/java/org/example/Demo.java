package org.example;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.easysearch.action.admin.indices.delete.DeleteIndexRequest;
import org.easysearch.action.bulk.BulkRequest;
import org.easysearch.action.bulk.BulkResponse;
import org.easysearch.action.delete.DeleteRequest;
import org.easysearch.action.delete.DeleteResponse;
import org.easysearch.action.get.GetRequest;
import org.easysearch.action.get.GetResponse;
import org.easysearch.action.index.IndexRequest;
import org.easysearch.action.support.master.AcknowledgedResponse;
import org.easysearch.client.RequestOptions;
import org.easysearch.client.RestClient;
import org.easysearch.client.RestHighLevelClient;
import org.easysearch.client.indices.CreateIndexRequest;
import org.easysearch.common.Strings;
import org.easysearch.common.settings.Settings;
import org.easysearch.common.xcontent.XContentType;

import javax.net.ssl.SSLContext;
import java.io.IOException;

/**
 * Easysearch Client demo
 *
 */
public class Demo 
{
    public static void main( String[] args ) throws IOException {
        RestHighLevelClient client = initEasysearchClient("admin", "admin");

        CreateIndexRequest createIndexRequest = new CreateIndexRequest("test-index");
        createIndexRequest.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 1)
        );

        //Create index
        client.indices().create(createIndexRequest, RequestOptions.DEFAULT);

        // Bulk
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < 10; i++) {
            IndexRequest indexRequest = new IndexRequest("test-index") // 替换为您的索引名称
                    .id(Integer.toString(i)) // 文档ID
                    .source("{\"field1\":\"value" + i + "\"}", XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        if (bulkResponse.hasFailures()) {
            System.out.println("Bulk operation had failures: " + bulkResponse.buildFailureMessage());
        } else {
            System.out.println("Bulk operation completed successfully");
        }

        // GET
        GetRequest getRequest = new GetRequest("test-index", "1");
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(response.getSourceAsString());

        // Delete doc
        DeleteRequest deleteDocumentRequest = new DeleteRequest("test-index", "1");
        DeleteResponse deleteResponse = client.delete(deleteDocumentRequest, RequestOptions.DEFAULT);
        System.out.println(Strings.toString(deleteResponse));

        //Delete the index
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("test-index"); //Index name.
        AcknowledgedResponse deleteIndexResponse = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println(Strings.toString(deleteIndexResponse));

        client.close();
    }

    public static RestHighLevelClient initEasysearchClient(String user, String password) {
        try {
            HttpHost[] httpHostArray = new HttpHost[1];
            //TODO: 如果服务端未开启 https 时，需要改一下协议
            httpHostArray[0] = new HttpHost("127.0.0.1", 9200, "https");
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true).build();
            SSLIOSessionStrategy sessionStrategy = new SSLIOSessionStrategy(sslContext, NoopHostnameVerifier.INSTANCE);
            return new RestHighLevelClient(RestClient.builder(httpHostArray).setHttpClientConfigCallback((HttpAsyncClientBuilder httpAsyncClientBuilder) -> {
                httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                httpAsyncClientBuilder.disableAuthCaching();
                httpAsyncClientBuilder.setSSLStrategy(sessionStrategy);
                return httpAsyncClientBuilder;
            }));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Easysearch client", e);
        }
    }
}
