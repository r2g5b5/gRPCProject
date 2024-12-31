package com.example.grpcproject.client_service.controller;

import com.example.grpcproject.client_service.service.BookAuthorClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BookAuthorController {
    private final BookAuthorClientService bookAuthorService;

    @GetMapping("/author/{id}")
    public Map<String, Object> getAuthor(@PathVariable String id) {
        return bookAuthorService.getAuthor(Integer.parseInt(id));
    }

    @GetMapping("/book/{id}")
    public List<Map<String, Object>> getBookAuthors(@PathVariable String id) throws InterruptedException {
        return bookAuthorService.getBooks(Integer.parseInt(id));
    }


    @GetMapping("/book")
    public Map<String, List<Map<String, Object>>> getExpensiveBook() throws InterruptedException {
        return bookAuthorService.getExpensiveBook();
    }

    @GetMapping("/book/author/{gender}")
    public List<Map<String, Object>>  getBookByGender(@PathVariable String gender) throws InterruptedException {
        return bookAuthorService.getBooksByGender(gender);
    }
}
