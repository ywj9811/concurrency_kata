package com.concurrency.sql.domain.book.service;

import com.concurrency.sql.domain.book.entity.Book;
import com.concurrency.sql.domain.book.repository.BookRepository;
import com.concurrency.sql.global.RedisRepository;
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
        log.info("ThreadID : {} ", Thread.currentThread().getId());
        book.minusStock();
        bookRepository.saveAndFlush(book);
    }

    @Transactional
    public int orderBookByLock(Long id) {
        Book book = bookRepository.findByIdWithPessimisticWrite(id).orElseThrow();
        log.info("ThreadID : {} ", Thread.currentThread().getId());
        book.minusStock();
        bookRepository.saveAndFlush(book);
        return book.getStock();
    }

    @Transactional
    public int countStock(Long id) {
        Book book = bookRepository.findById(id).orElseThrow();
        return book.getStock();
    }
}
