package com.infinilabs;

import com.infinilabs.dao.domain.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import java.util.Date;

@SpringBootTest
public class EzsJpaTests {
    @Autowired
    @Qualifier("elasticsearchTemplate")
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Test
    public void add() {
        Book book = new Book();
        book.setAuthor("金庸");
        book.setTitle("倚天屠龙记");
        book.setCounty("中国");
        book.setPrice(888.8);
        book.setCreateTime(new Date());
        elasticsearchTemplate.save(book);
    }
}
