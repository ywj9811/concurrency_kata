package com.concurrency.sql.service;

import com.concurrency.sql.domain.book.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ConcurrencyTest {
    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("Basic")
    void concurrencyCheck() throws InterruptedException {
        int before = bookService.countStock(1L);
        int threadCount = 100;
        //멀티스레드 이용 ExecutorService : 비동기를 단순하게 처리할 수 있도록 해주는 java api
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        //다른 스레드에서 수행이 완료될 때 까지 대기할 수 있도록 도와주는 API - 요청이 끝날때 까지 기다림
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                        bookService.orderBook(1L);
                        latch.countDown();
                    }
            );
        }

        latch.await();

        int after = bookService.countStock(1L);

        //100 - (1*100) = 0
        System.out.println("Except : "+ 100 + "\nActual : " + (before - after));
        assertThat(after).isNotEqualTo(before-100);
    }

    @Test
    @DisplayName("Lock")
    void concurrencyCheckWithLock() throws InterruptedException {
        int before = bookService.countStock(1L);
        int threadCount = 100;
        //멀티스레드 이용 ExecutorService : 비동기를 단순하게 처리할 수 있도록 해주는 java api
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        //다른 스레드에서 수행이 완료될 때 까지 대기할 수 있도록 도와주는 API - 요청이 끝날때 까지 기다림
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                        bookService.orderBookByLock(1L);
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
