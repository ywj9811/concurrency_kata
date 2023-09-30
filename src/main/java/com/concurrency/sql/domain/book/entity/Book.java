package com.concurrency.sql.domain.book.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;
    private String bookName;
    private int stock;

    public int minusStock() {
        if (stock - 1 < 0) {
            throw new RuntimeException("재고 부족");
        }
        this.stock--;
        return stock;
    }

    public int minus(int stock) {
        this.stock = stock;
        return stock;
    }

    public void setting() {
        this.stock = 100;
    }
}

