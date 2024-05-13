package org.example;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.infinilabs.clients.easysearch.EasysearchClient;
import com.infinilabs.clients.easysearch._helpers.bulk.BulkIngester;
import com.infinilabs.clients.easysearch.core.bulk.BulkOperation;
import com.infinilabs.clients.json.jackson.JacksonJsonpMapper;
import com.infinilabs.clients.transport.rest_client.RestClientTransport;
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

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

/**
 * Easysearch Client demo
 */
public class Demo {
    public static void main(String[] args) throws IOException {
        String dir = System.getProperty("user.dir");
        Properties prop = new Properties();
        Path path = Paths.get(dir, "src", "main", "resources", "application.conf");
        prop.load(new FileInputStream(path.toString()));

        String serverUrl = prop.getProperty("server-url");
        String username = prop.getProperty("username");
        String password = prop.getProperty("password");

        String csvFile = prop.getProperty("csv-file");
        String csvPath = Paths.get(dir, "src", "main", "resources", csvFile).toString();

        EasysearchClient client = initEasysearchClient(serverUrl, username, password);

        if (!client.indices().exists(ex -> ex.index("books")).value()) {
            client.indices()
                    .create(c -> c
                            .index("books")
                            .mappings(mp -> mp
                                    .properties("id", p -> p.text(t -> t))
                                    .properties("title", p -> p.text(t -> t))
                                    .properties("description", p -> p.text(t -> t))
                                    .properties("author", p -> p.text(t -> t))
                                    .properties("year", p -> p.text(s -> s))
                                    .properties("publisher", p -> p.text(t -> t))
                                    .properties("ratings", p -> p.halfFloat(hf -> hf))
                            ));
        }

        Instant start = Instant.now();
        System.out.println("Starting BulkIndexer... \n");

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.builder()
                .addColumn("id") // same order as in the csv
                .addColumn("title")
                .addColumn("description")
                .addColumn("author")
                .addColumn("year")
                .addColumn("publisher")
                .addColumn("ratings")
                .setColumnSeparator(';')
                .setSkipFirstDataRow(true)
                .build();

        MappingIterator<Book> it = csvMapper
                .readerFor(Book.class)
                .with(schema)
                .readValues(new FileReader(csvPath));

        BulkIngester ingester = BulkIngester.of(bi -> bi
                .client(client)
                .maxConcurrentRequests(20)
                .maxOperations(5000));

        boolean hasNext = true;

        while (hasNext) {
            try {
                Book book = it.nextValue();
                System.out.println(book);
                ingester.add(BulkOperation.of(b -> b
                        .index(i -> i
                                .index("books")
                                // 使用自定义的id
                                .id(book.getId())
                                .document(book))));
                hasNext = it.hasNextValue();
            } catch (JsonParseException | InvalidFormatException e) {
                System.out.println(e);
            }
        }

        ingester.close();

        client.indices().refresh();

        Instant end = Instant.now();

        System.out.println("Finished in: " + Duration.between(start, end).toMillis() + "\n");

        System.exit(0);
    }

    public static EasysearchClient initEasysearchClient(String serverUrl, String user, String password) {
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
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true).build();
            SSLIOSessionStrategy sessionStrategy = new SSLIOSessionStrategy(sslContext, NoopHostnameVerifier.INSTANCE);

            RestClient restClient = RestClient.builder(httpHostArray).setHttpClientConfigCallback((HttpAsyncClientBuilder httpAsyncClientBuilder) -> {
                httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                httpAsyncClientBuilder.disableAuthCaching();
                httpAsyncClientBuilder.setSSLStrategy(sessionStrategy);
                return httpAsyncClientBuilder;
            }).build();

            JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(JsonMapper.builder().build());

            return new EasysearchClient(new RestClientTransport(restClient, jsonpMapper));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Easysearch client", e);
        }
    }
}
