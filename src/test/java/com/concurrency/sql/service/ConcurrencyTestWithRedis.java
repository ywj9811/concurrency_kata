package com.concurrency.sql.service;

import com.concurrency.sql.domain.book.service.BookServiceWithRedis;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ConcurrencyTestWithRedis {
    @Autowired
    private BookServiceWithRedis bookService;

    @Test
    @DisplayName("With_Redis_SpinLock")
    void concurrencyCheckWithSpinLock() throws InterruptedException {
        int before = bookService.countStock(1L);
        int threadCount = 100;
        //멀티스레드 이용 ExecutorService : 비동기를 단순하게 처리할 수 있도록 해주는 java api
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        //다른 스레드에서 수행이 완료될 때 까지 대기할 수 있도록 도와주는 API - 요청이 끝날때 까지 기다림
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                        try {
                            bookService.orderBookByRedisWithSpinLock(1L);
                        } catch (InterruptedException e) {
                            System.out.println("예외 발생");
                            throw new RuntimeException(e);
                        }
                        latch.countDown();
                    }
            );
        }

        latch.await();

        int after = bookService.countStock(1L);

        //100 - (1*100) = 0
        System.out.println("Except : "+ 100 + "\nActual : " + (before - after));
        assertThat(after).isEqualTo(before-100);
    }

    @Test
    @DisplayName("With_Redis_Redisson")
    void concurrencyCheckWithRedisson() throws InterruptedException {
        int before = bookService.countStock(1L);
        int threadCount = 100;
        //멀티스레드 이용 ExecutorService : 비동기를 단순하게 처리할 수 있도록 해주는 java api
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        //다른 스레드에서 수행이 완료될 때 까지 대기할 수 있도록 도와주는 API - 요청이 끝날때 까지 기다림
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                        bookService.orderBookByRedisWithRedisson(1L);
                        latch.countDown();
                    }
            );
        }

        latch.await();

        int after = bookService.countStock(1L);

        //100 - (1*100) = 0
        System.out.println("Except : "+ 100 + "\nActual : " + (before - after));
        assertThat(after).isEqualTo(before-100);
    }
}
