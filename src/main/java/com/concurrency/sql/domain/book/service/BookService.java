package com.concurrency.sql.domain.book.service;

import com.concurrency.sql.domain.book.entity.Book;
import com.concurrency.sql.domain.book.repository.BookRepository;
import com.concurrency.sql.global.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final RedisRepository redisRepository;

    @Transactional
    public void orderBook(Long id) {
        Book book = bookRepository.findById(id).orElseThrow();
        log.info("ThreadID : {} ", Thread.currentThread().getId());
        book.minusStock();
    }

    @Transactional
    public int orderBookByLock(Long id) {
        Book book = bookRepository.findByIdWithPessimisticWrite(id).orElseThrow();
        log.info("ThreadID : {} ", Thread.currentThread().getId());
        book.minusStock();
        return book.getStock();
    }

    @Transactional
    public int countStock(Long id) {
        Book book = bookRepository.findById(id).orElseThrow();
        return book.getStock();
    }

    @Transactional
    public void orderBookByRedisWithSpinLock(Long id) throws InterruptedException {
        int wait = 0;
        while (!redisRepository.lock(id.toString())) {
            wait++;
//            Thread.sleep(100);
            //계속해서 lock을 확인하면 너무 자주 확인하니 100ms대기
            log.info("ThreadID : {} 대기 - {}번째", Thread.currentThread().getId(), wait);
        } // 락을 획득하기 위해 대기

        try {
            Book book = bookRepository.findById(id).orElseThrow();
            book.minusStock();
        } finally {
            redisRepository.deleteLock(id.toString());
            // 락 해제
        }
    }
}
