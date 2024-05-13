package org.example;

import com.infinilabs.clients.easysearch._types.InlineScript;
import com.infinilabs.clients.easysearch._types.aggregations.HistogramBucket;
import com.infinilabs.clients.easysearch._types.query_dsl.MatchQuery;
import com.infinilabs.clients.easysearch._types.query_dsl.Query;
import com.infinilabs.clients.easysearch._types.query_dsl.RangeQuery;
import com.infinilabs.clients.easysearch.core.BulkRequest;
import com.infinilabs.clients.easysearch.core.BulkResponse;
import com.infinilabs.clients.easysearch.core.GetResponse;
import com.infinilabs.clients.easysearch.core.IndexRequest;
import com.infinilabs.clients.easysearch.core.IndexResponse;
import com.infinilabs.clients.easysearch.core.SearchResponse;
import com.infinilabs.clients.easysearch.core.UpdateByQueryResponse;
import com.infinilabs.clients.easysearch.core.UpdateResponse;
import com.infinilabs.clients.easysearch.core.search.Hit;
import com.infinilabs.clients.easysearch.core.search.SourceConfig;
import com.infinilabs.clients.easysearch.core.search.SourceConfigParam;
import com.infinilabs.clients.easysearch.core.search.TotalHits;
import com.infinilabs.clients.easysearch.core.search.TotalHitsRelation;
import com.infinilabs.clients.json.JsonData;
import com.infinilabs.clients.util.ObjectBuilder;
import org.example.domain.Product;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TestEasysearchDocs extends TestBase {

    @Test
    public void testInsertDocWithId() throws IOException {
        if (!client.indices().exists(ex -> ex.index("products")).value()) {
            client.indices().create(c -> c.index("products"));

            Product product = new Product("bk-1", "City bike", 123.0);

            IndexResponse response = client.index(i -> i.index("products").id(product.getSku()).document(product));

            System.out.println("Indexed with version " + response.version());
        } else {
            System.out.println("the index exists,please delete it and test again.");
        }
    }

    @Test
    public void testIndexWithJson() throws IOException {
        Reader input = new StringReader("{'id': 'sn10003', 'name': 'television', 'price': 5500.5}".replace('\'', '"'));

        IndexRequest<JsonData> request = IndexRequest.of(i -> i.index("products").withJson(input));

        IndexResponse response = client.index(request);

        System.out.println("Indexed with version " + response.version());
    }


    @Test
    public void testBulkDocs() throws IOException {
        List<Product> products = new ArrayList<>();
        products.add(new Product("sn10004", "T-shirt", 100.5));
        products.add(new Product("sn10005", "phone", 8999.9));
        products.add(new Product("sn10006", "ipad", 6555.5));

        BulkRequest.Builder br = new BulkRequest.Builder();
        for (Product product : products) {
            br.operations(op -> op.index(idx -> idx.index("products").id(product.getSku()).document(product)));
        }

        BulkResponse response = client.bulk(br.build());

        System.out.println("Indexed with docs " + response.items().size());
    }

    @Test
    public void testGetDocs() throws IOException {
        GetResponse<Product> response = client.get(g -> g.index("products").id("bk-1"), Product.class);

        if (response.found()) {
            Product product = response.source();
            System.out.printf("Product name [%s]", product.getName());
        } else {
            System.out.println("Product not found");
        }
    }

    @Test
    public void testSearchDocs() throws IOException {
        String searchText = "bike";
        SourceConfig sourceConfig = SourceConfig.of(sc -> sc.filter(sf -> sf.includes("sku", "name")));

        SearchResponse<Product> response = client.search(s -> s.index("products").source(sourceConfig).query(q -> q.match(t -> t.field("name").query(searchText))), Product.class);
        TotalHits totalHits = response.hits().total();
        boolean isExactResult = totalHits.relation() == TotalHitsRelation.Eq;
        if (isExactResult) {
            System.out.printf("Exact Result: %s\n", totalHits.value());
        } else {
            System.out.printf("More than result: %s\n", totalHits.value());
        }

        List<Hit<Product>> hits = response.hits().hits();
        for (Hit<Product> hit : hits) {
            Product product = hit.source();
            System.out.printf("Product, %s\n", product);
        }
    }

    @Test
    public void testUpdateDocWithId() throws IOException {
        if (client.indices().exists(ex -> ex.index("products")).value()) {
            Product product = new Product("bk-1", "City bike v2", 123.0);

            UpdateResponse response = client.update(u -> u.index("products").id("bk-1").doc(product), Product.class);

            System.out.println("Indexed with version " + response.version());
        } else {
            System.out.println("the index exists,please delete it and test again.");
        }
    }

    @Test
    public void testUpsertDocWithId() throws IOException {
        if (client.indices().exists(ex -> ex.index("products")).value()) {
            Product product = new Product("bk-1", "City bike v2", 123.0);

            UpdateResponse response = client.update(u -> u.index("products").id("bk-1").doc(product).upsert(product).docAsUpsert(true), Product.class);

            System.out.println("Indexed with version " + response.version());
        } else {
            System.out.println("the index exists,please delete it and test again.");
        }
    }

    @Test
    public void testFindWthNestedQueries() {
        String searchText= "bike";
        Double price = 123.0;
        Query byName = MatchQuery.of(q -> q.field("name").query(searchText))._toQuery();
        Query byMaxPrice = RangeQuery.of(q -> q.field("price").gte(JsonData.of(price)))._toQuery();
        SearchResponse response;
        try {
            response = client.search(s -> s.index("products").query(q -> q.bool(b -> b.must(byName).must(byMaxPrice))), Product.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<Hit<Product>> hits = response.hits().hits();
        for (Hit<Product> hit : hits) {
            Product product = hit.source();
            System.out.println("Found product " + product.getName() + ", score " + hit.score());
        }
    }

    @Test
    public void testUpdateDocByquery() throws IOException {
        if (client.indices().exists(ex -> ex.index("products")).value()) {
            UpdateByQueryResponse response = client.updateByQuery(u -> u.index("products").script(s -> s.inline(InlineScript.of(i -> i.lang("painless").source("ctx._source.price += 1000")))).query(q -> q.match(m -> m.field("name").query("T-shirt"))));
            System.out.println("Indexed doc updated " + response.updated());
        } else {
            System.out.println("the index exists,please delete it and test again.");
        }
    }


    @Test
    public void testSimpleAggregation() {
        String searchText = "bike";

        Query query = MatchQuery.of(m -> m.field("name").query(searchText))._toQuery();
        SearchResponse<Void> response;
        try {
            response = client.search(b -> b.index("products").size(0).query(query).aggregations("price-histogram", a -> a.histogram(h -> h.field("price").interval(50.0))), Void.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<HistogramBucket> buckets = response.aggregations().get("price-histogram").histogram().buckets().array();

        for (HistogramBucket bucket : buckets) {
            System.out.println("There are " + bucket.docCount() + " bikes under " + bucket.key());
        }
    }

    @Test
    public void testDeleteIndex() {
        try {
            client.indices().delete(c -> c.index("products"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void templatedSearch() {
        try {
            client.putScript(r -> r.id("query-script").script(s -> s.lang("mustache").source("{\"query\":{\"match\":{\"{{field}}\":\"{{value}}\"}}}")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void buildTermQuery(String value) {
        Query query = new Query.Builder().term(e -> e.field("category").value(value)).build();
        try {
            SearchResponse<Product> search = client.search(s -> s.index("products").query(query), Product.class);
            for (Hit<Product> hit : search.hits().hits()) {
                System.out.println(("product : " + hit.source()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
