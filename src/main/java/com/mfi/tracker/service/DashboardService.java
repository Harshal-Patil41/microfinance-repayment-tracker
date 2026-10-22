package com.mfi.tracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mfi.tracker.dto.Indicators;
import com.mfi.tracker.model.Loan;
import com.mfi.tracker.model.Repayment;
import com.mfi.tracker.model.RepaymentStatus;
import com.mfi.tracker.repository.LoanRepository;
import com.mfi.tracker.repository.RepaymentRepository;

/**
 * BR8: total disbursed = sum of loan amounts; total collected = sum of amounts paid; overdue count = OVERDUE rows.
 * BR9: collection rate % = total collected / total amount due up to today x 100 (0 if nothing is due yet).
 */
@Service
@Transactional
public class DashboardService {

    private final BorrowerService borrowerService;
    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;
    private final RepaymentService repaymentService;

    public DashboardService(BorrowerService borrowerService, LoanRepository loanRepository,
                            RepaymentRepository repaymentRepository, RepaymentService repaymentService) {
        this.borrowerService = borrowerService;
        this.loanRepository = loanRepository;
        this.repaymentRepository = repaymentRepository;
        this.repaymentService = repaymentService;
    }

    public Indicators indicators() {
        repaymentService.refreshStatuses();
        LocalDate today = repaymentService.today();
        List<Loan> loans = loanRepository.findAll();
        List<Repayment> rows = repaymentRepository.findAll();

        BigDecimal disbursed = loans.stream().map(Loan::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal collected = rows.stream().map(Repayment::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dueToDate = rows.stream().filter(r -> !r.getDueDate().isAfter(today))
                .map(Repayment::getAmountDue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal rate = dueToDate.signum() == 0 ? BigDecimal.ZERO.setScale(1)
                : collected.multiply(BigDecimal.valueOf(100)).divide(dueToDate, 1, RoundingMode.HALF_UP);

        return new Indicators(borrowerService.count(), loans.size(),
                disbursed.setScale(2, RoundingMode.HALF_UP), collected.setScale(2, RoundingMode.HALF_UP),
                count(rows, RepaymentStatus.OVERDUE), count(rows, RepaymentStatus.PAID),
                count(rows, RepaymentStatus.PENDING), rate);
    }

    private static long count(List<Repayment> rows, RepaymentStatus s) {
        return rows.stream().filter(r -> r.getStatus() == s).count();
    }
}
