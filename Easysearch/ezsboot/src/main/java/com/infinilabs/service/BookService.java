package com.infinilabs.service;

import com.infinilabs.dao.domain.Book;
import com.infinilabs.dao.es.BookEzsRepository;
import com.infinilabs.utils.LogUtil;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service
public class BookService {
    private final BookEzsRepository bookEzsRepository;

    public BookService(BookEzsRepository bookEzsRepository) {
        this.bookEzsRepository = bookEzsRepository;
    }

    public void addBook(Book book) {
        try {
           // bookEzsRepository.save(book);
        } catch (Exception e) {
            LogUtil.error(String.format("保存ES错误！%s", e.getMessage()));
        }
    }

    public List<SearchHit<Book>> searchBook(String keyword) {
        return bookEzsRepository.findByTitleOrAuthor(keyword, keyword);
    }
}
