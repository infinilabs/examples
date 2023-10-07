# 项目说明

> 由于新版本的 Spring-Data-Elasticsearch 对集群的返回头是否存在`X-Elastic-Product`,并且api有了很大的变化，可能会存在兼容性的问题。

本项目推荐继续7.10.2版本的 High Level REST Client 来进行操作。

具体修改步骤如下：

1. 修改pom.xml 依赖的版本

```xml
<!-- 版本信息定义 -->
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>17</java.version>
        <easysearch.api.version>7.10.2</easysearch.api.version>
    </properties>

<!-- 依赖包的版本覆盖 -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.data</groupId>
                <artifactId>spring-data-bom</artifactId>
                <version>2021.1.3</version>
                <scope>import</scope>
                <type>pom</type>
            </dependency>
        </dependencies>
    </dependencyManagement>

<!-- 依赖包覆盖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>${easysearch.api.version}</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch</groupId>
            <artifactId>elasticsearch</artifactId>
            <version>${easysearch.api.version}</version>
        </dependency>
```
2. 使用 EasysearchConfig 进行客户端定义

```java
@Lazy
    @Bean(destroyMethod = "close", name = "restHighLevelClient")
    @ConditionalOnMissingBean
    public RestHighLevelClient easysearchClient() {

        // 初始化 RestClient, hostName 和 port 填写集群的内网 VIP 地址与端口
        RestClientBuilder builder = RestClient.builder(toHttpHost()).setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(connTimeout).setSocketTimeout(socketTimeout).setConnectionRequestTimeout(connectionRequestTimeout));

        // 保活策略
        builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom().setSoKeepAlive(true).build()));

        // 设置认证信息
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.disableAuthCaching();
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            });
        }

        RestHighLevelClient easysearchClient = new RestHighLevelClient(builder);
        return easysearchClient;
    }


    @Lazy
    @Bean(name = {"elasticsearchOperations", "elasticsearchTemplate"})
    public ElasticsearchRestTemplate elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(easysearchClient());
    }
```

> 如果您依然想使用最新的 Spring-Data-Elasticsearch 版本来访问 Easysearch ,请查看[补丁文件](patch/EasysearchConfig.java)