package com.infinilabs.dao.es;

import com.infinilabs.dao.domain.Book;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.annotations.HighlightParameters;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface BookEzsRepository extends ElasticsearchRepository<Book, String> {

    @Highlight(fields = {@HighlightField(name = "title"), @HighlightField(name = "author")}, parameters = @HighlightParameters(preTags = "<strong><font style='color:red'>", postTags = "</font></strong>", fragmentSize = 500, numberOfFragments = 3))
    List<SearchHit<Book>> findByTitleOrAuthor(String title, String author);

    long deleteByTitle(String title);

    List<Book> findByTitle(String title);
}
