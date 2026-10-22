package com.mfi.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mfi.tracker.model.Borrower;

public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
}
