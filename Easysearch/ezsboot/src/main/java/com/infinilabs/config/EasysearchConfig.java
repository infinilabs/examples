package com.infinilabs.config;

import com.infinilabs.utils.LogUtil;
import com.infinilabs.utils.VersionUtil;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import static com.infinilabs.constants.BaseEzsConstants.*;

@Configuration
//@AutoConfigureBefore(RedisConfig.class)
@ConfigurationProperties(prefix = "easysearch") //配置的前缀
@ConditionalOnProperty(prefix = "easysearch", name = "enable", havingValue = "true")
//TODO: 客户端的探活时间修改为：net.ipv4.tcp_keepalive_time = 1800
public class EasysearchConfig {

    /**
     * 支持的版本 目前支持版本为7.12.0
     */
    private final static String SUPPORTED_JAR_VERSION = "7.10.2";
    /**
     * 支持的客户端版本 目前支持7.xx 推荐7.12.0
     */
    private final static String SUPPORTED_CLIENT_VERSION = "7";

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

    private static SSLContext buildSSLContext() {
        ClassPathResource resource = new ClassPathResource("instance.crt");
        SSLContext sslContext = null;
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate trustedCa;
            try (InputStream is = resource.getInputStream()) {
                trustedCa = factory.generateCertificate(is);
            }
            KeyStore trustStore = KeyStore.getInstance("pkcs8");
            trustStore.load(null, null);
            trustStore.setCertificateEntry("ca", trustedCa);
            SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(trustStore, null);
            sslContext = sslContextBuilder.build();
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException |
                 KeyManagementException e) {
            LogUtil.formatError("Easysearch 连接认证失败", e);
        }

        return sslContext;
    }

    /**
     * 校验es client版本及jar包版本
     *
     * @param restHighLevelClient es高级客户端
     */
    private static void verify(RestHighLevelClient restHighLevelClient) {
        // 校验jar包版本是否为推荐使用版本
        String jarVersion = VersionUtil.getJarVersion(restHighLevelClient.getClass());
        LogUtil.formatInfo("Elasticsearch jar version:", jarVersion);
        if (!jarVersion.equals(SUPPORTED_JAR_VERSION) && !UNKNOWN.equals(jarVersion)) {
            LogUtil.formatError("supported elasticsearch and restHighLevelClient jar version is:%s ,Please resolve the dependency conflict!", SUPPORTED_JAR_VERSION);
        }
        String clientVersion = VersionUtil.getClientVersion(restHighLevelClient);
        LogUtil.formatInfo("Elasticsearch client version:%s", clientVersion);
        if (!clientVersion.startsWith(SUPPORTED_CLIENT_VERSION)) {
            // 这里校验客户端为非强制，客户端版本非推荐版本对应提醒即可，es会报错提醒
            LogUtil.formatWarn("supported elasticsearch client version is:%s.xx", SUPPORTED_CLIENT_VERSION);
        }
        if (!jarVersion.equals(clientVersion)) {
            // 提示jar包与客户端版本不对应，es官方推荐jar包版本对应客户端版本
            LogUtil.formatWarn("Elasticsearch clientVersion:%s not equals jarVersion:%s, It does not affect your use, but we still recommend keeping it consistent!", clientVersion, jarVersion);
        }
    }

    @PostConstruct
    void init() {
        // 解决netty启动冲突问题
        // Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

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
