package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.infinilabs.clients.easysearch.EasysearchClient;
import com.infinilabs.clients.json.jackson.JacksonJsonpMapper;
import com.infinilabs.clients.transport.rest_client.RestClientTransport;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import javax.net.ssl.SSLContext;
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
import org.easysearch.client.RestClient;
import org.junit.Before;

public class TestBase {

  protected EasysearchClient client;

  public static EasysearchClient initEasysearchClient(String serverUrl, String user,
      String password) {
    try {
      String[] urls = serverUrl.split(",");
      HttpHost[] httpHostArray = new HttpHost[urls.length];
      for (int i = 0; i < urls.length; i++) {
        String urlStr = urls[i].trim();
        URL url = new URL(urlStr);

        String host = url.getHost(); // 获取 IP 地址
        int port = url.getPort(); // 获取端口号
        String protocol = url.getProtocol(); // 获取协议
        httpHostArray[i] = new HttpHost(host, port, protocol);
      }

      final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
      credentialsProvider.setCredentials(AuthScope.ANY,
          new UsernamePasswordCredentials(user, password));
      SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null,
          (TrustStrategy) (chain, authType) -> true).build();
      SSLIOSessionStrategy sessionStrategy = new SSLIOSessionStrategy(sslContext,
          NoopHostnameVerifier.INSTANCE);

      RestClient restClient = RestClient.builder(httpHostArray)
          .setHttpClientConfigCallback((HttpAsyncClientBuilder httpAsyncClientBuilder) -> {
            httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            httpAsyncClientBuilder.disableAuthCaching();
            httpAsyncClientBuilder.setSSLStrategy(sessionStrategy);
            return httpAsyncClientBuilder;
          }).build();

      ObjectMapper objectMapper = JsonMapper.builder()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .build();
      JacksonJsonpMapper jacksonJsonpMapper = new JacksonJsonpMapper(objectMapper);

      return new EasysearchClient(new RestClientTransport(restClient, jacksonJsonpMapper));
    } catch (Exception e) {
      throw new RuntimeException("Failed to create Easysearch client", e);
    }
  }

  @Before
  public void init() throws IOException {
    String dir = System.getProperty("user.dir");
    Properties prop = new Properties();
    Path path = Paths.get(dir, "src", "main", "resources", "application.conf");
    prop.load(new FileInputStream(path.toString()));

    String serverUrl = prop.getProperty("server-url");
    String username = prop.getProperty("username");
    String password = prop.getProperty("password");

    System.out.printf("curl -ku %s:%s %s\n", username, password, serverUrl);

    client = initEasysearchClient(serverUrl, username, password);
  }
}
