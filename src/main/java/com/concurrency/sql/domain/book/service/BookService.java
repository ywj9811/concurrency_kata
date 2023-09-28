package com.concurrency.sql.domain.book.service;

import com.concurrency.sql.domain.book.entity.Book;
import com.concurrency.sql.domain.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    @Transactional
    public void orderBook(Long id) {
        Book book = bookRepository.findById(id).orElseThrow();
        book.minusStock();
    }

    @Transactional
    public int orderBookByLock(Long id) {
        Book book = bookRepository.findByIdWithPessimisticWrite(id).orElseThrow();
        book.minusStock();
        return book.getStock();
    }

    @Transactional
    public int countStock(Long id) {
        Book book = bookRepository.findById(id).orElseThrow();
        return book.getStock();
    }
}
