package com.mfi.tracker;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BorrowerRepositoryTests {

    @Autowired
    private BorrowerRepository repository;

    @Test
    void savesAndFindsBorrower() {
        Borrower borrower = new Borrower();
        borrower.setName("Test Borrower");
        borrower.setPhone("9876543210");
        borrower.setVillage("Sample Village");

        Borrower saved = repository.save(borrower);

        assertTrue(repository.findById(saved.getId()).isPresent());
    }
}