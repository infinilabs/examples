package com.infinilabs.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.infinilabs.constants.EasysearchConstants.*;

@Configuration
//@AutoConfigureBefore(RedisConfig.class)
@ConfigurationProperties(prefix = "easysearch") //配置的前缀
@ConditionalOnProperty(prefix = "easysearch", name = "enable", havingValue = "true")
//TODO: 客户端的探活时间修改为：net.ipv4.tcp_keepalive_time = 1800
public class EasysearchConfig {

    @Setter
    private String host;

    @Setter
    private Integer connTimeout;

    @Setter
    private Integer socketTimeout;

    @Setter
    private Integer connectionRequestTimeout;

    @Setter
    private String username;

    @Setter
    private String password;

    @PostConstruct
    void init() {
        // 解决netty启动冲突问题
        // Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    @Bean
    public ElasticsearchClient easysearchClient() {

        RestClient restClient = getRestClient();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    @Bean
    public ElasticsearchAsyncClient elasticsearchAsyncClient() {

        RestClient restClient = getRestClient();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchAsyncClient(transport);
    }

    private RestClient getRestClient() {
        // 初始化 RestClient, hostName 和 port 填写集群的内网 VIP 地址与端口
        RestClientBuilder builder = RestClient.builder(toHttpHost()).setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(connTimeout).setSocketTimeout(socketTimeout).setConnectionRequestTimeout(connectionRequestTimeout));

        // 设置认证信息
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.disableAuthCaching();
                // Keepalive
                httpClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom().setSoKeepAlive(true).build());
                // Fix for Easysearch
                httpClientBuilder.setDefaultHeaders(List.of(new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()))).addInterceptorLast((HttpResponseInterceptor) (response, context) -> response.addHeader("X-Elastic-Product", "Elasticsearch"));
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            });
        }

        RestClient restClient = builder.build();
        return restClient;
    }


    /**
     * 解析配置的字符串，转为HttpHost对象数组
     */
    private HttpHost[] toHttpHost() {
        if (!StringUtils.hasLength(host)) {
            throw new RuntimeException("invalid easysearch configuration");
        }

        String[] hostArray = host.split(COMMA);
        HttpHost[] httpHosts = new HttpHost[hostArray.length];
        HttpHost httpHost;
        for (int i = 0; i < hostArray.length; i++) {
            String[] strings = hostArray[i].split(COLON);
            httpHost = new HttpHost(strings[0], Integer.parseInt(strings[1]), HTTP_SCHEMA);
            httpHosts[i] = httpHost;
        }
        return httpHosts;
    }
}
