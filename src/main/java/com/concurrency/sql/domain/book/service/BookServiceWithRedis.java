package com.concurrency.sql.domain.book.service;

import com.concurrency.sql.domain.book.entity.Book;
import com.concurrency.sql.domain.book.repository.BookRepository;
import com.concurrency.sql.global.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class BookServiceWithRedis {
    private final BookRepository bookRepository;
    private final RedisRepository redisRepository;
    private final RedissonClient redissonClient;

    @Transactional
    public int countStock(Long id) {
        Book book = bookRepository.findById(id).orElseThrow();
        return book.getStock();
    }

    public void orderBookByRedisWithSpinLock(Long id) throws InterruptedException {
        int wait = 0;
        while (!redisRepository.lock(id.toString())) {
            wait++;
            Thread.sleep(50);
            //계속해서 lock을 확인하면 너무 자주 확인하니 (n)ms대기
            log.info("ThreadID : {} 대기 - {}번째", Thread.currentThread().getId(), wait);
        } // 락을 획득하기 위해 대기

        try {
            Book book = bookRepository.findById(id).orElseThrow();
            int count = book.minusStock();
            log.info("count : {}", count);
            bookRepository.saveAndFlush(book);
        } finally {
            redisRepository.deleteLock(id.toString());
            // 락 해제
        }
    }

    public void orderBookByRedisWithRedisson(Long id) {
        RLock lock = redissonClient.getLock(id.toString()+"_Redisson");
        try {
            boolean available = lock.tryLock(3, 1, TimeUnit.SECONDS);
            if (!available) {
                throw new RuntimeException("Lock을 획득하지 못했습니다.");
            }
            Book book = bookRepository.findById(id).orElseThrow();
            book.minusStock();
            bookRepository.saveAndFlush(book);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
