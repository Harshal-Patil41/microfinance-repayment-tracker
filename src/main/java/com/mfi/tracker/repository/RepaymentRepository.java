package com.mfi.tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mfi.tracker.model.Repayment;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByLoanIdOrderByInstallmentNo(Long loanId);
}
