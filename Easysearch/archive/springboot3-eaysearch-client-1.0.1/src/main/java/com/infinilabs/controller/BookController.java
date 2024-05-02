package com.infinilabs.controller;

import com.infinilabs.dao.domain.Book;
import com.infinilabs.service.BookService;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@RestController
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/book/add")
    public Map<String, String> addBook(@RequestBody Book book) {
        bookService.addBook(book);
        Map<String, String> map = new HashMap<>();
        map.put("msg", "ok");
        return map;
    }

    @GetMapping("/book/search")
    public List<SearchHit<Book>> searchBook(String key) {
        return bookService.searchBook(key);
    }
}
