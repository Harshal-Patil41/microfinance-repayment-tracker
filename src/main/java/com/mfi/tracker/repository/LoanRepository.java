package com.mfi.tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mfi.tracker.model.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
