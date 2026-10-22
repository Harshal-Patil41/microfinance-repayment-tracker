package com.mfi.tracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mfi.tracker.dto.LoanForm;
import com.mfi.tracker.dto.LoanRow;
import com.mfi.tracker.exception.BusinessException;
import com.mfi.tracker.exception.NotFoundException;
import com.mfi.tracker.model.Borrower;
import com.mfi.tracker.model.Frequency;
import com.mfi.tracker.model.Loan;
import com.mfi.tracker.model.Repayment;
import com.mfi.tracker.model.RepaymentStatus;
import com.mfi.tracker.repository.BorrowerRepository;
import com.mfi.tracker.repository.LoanRepository;
import com.mfi.tracker.repository.RepaymentRepository;

@Service
@Transactional
public class LoanService {

    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;
    private final RepaymentRepository repaymentRepository;
    private final RepaymentService repaymentService;

    public LoanService(LoanRepository loanRepository, BorrowerRepository borrowerRepository,
                       RepaymentRepository repaymentRepository, RepaymentService repaymentService) {
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
        this.repaymentRepository = repaymentRepository;
        this.repaymentService = repaymentService;
    }

    /**
     * Creates the loan and generates its repayment schedule (BR2, BR3):
     *  - installment = amount / n rounded to 2 decimals; the LAST installment absorbs any rounding difference
     *  - weekly: due date = start + 7 days x i ; monthly: start + i months
     */
    public Loan create(LoanForm f) {
        Borrower borrower = borrowerRepository.findById(f.getBorrowerId())
                .orElseThrow(() -> new NotFoundException("Borrower " + f.getBorrowerId() + " was not found"));
        int n = f.getInstallments();
        BigDecimal amount = f.getAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal each = amount.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        BigDecimal last = amount.subtract(each.multiply(BigDecimal.valueOf(n - 1L)));
        if (each.signum() <= 0 || last.signum() <= 0) {
            throw new BusinessException("The loan amount is too small for " + n + " installments");
        }

        Loan loan = loanRepository.save(new Loan(borrower, amount, f.getStartDate(), n, f.getFrequency()));
        for (int i = 1; i <= n; i++) {
            LocalDate due = f.getFrequency() == Frequency.WEEKLY
                    ? f.getStartDate().plusDays(7L * i)
                    : f.getStartDate().plusMonths(i);
            repaymentRepository.save(new Repayment(loan, i, due, i == n ? last : each));
        }
        return loan;
    }

    public Loan get(Long id) {
        return loanRepository.findById(id).orElseThrow(() -> new NotFoundException("Loan " + id + " was not found"));
    }

    /** Dashboard rows; q (optional) matches borrower name or village, partial and case-insensitive. */
    public List<LoanRow> rows(String q) {
        repaymentService.refreshStatuses();
        String needle = q == null ? "" : q.trim().toLowerCase();
        Map<Long, List<Repayment>> byLoan = repaymentRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getLoan().getId()));
        List<LoanRow> out = new ArrayList<>();
        for (Loan l : loanRepository.findAll(Sort.by("id"))) {
            Borrower b = l.getBorrower();
            if (!needle.isEmpty() && !b.getName().toLowerCase().contains(needle)
                    && !b.getVillage().toLowerCase().contains(needle)) {
                continue;
            }
            List<Repayment> rows = byLoan.getOrDefault(l.getId(), List.of());
            out.add(toRow(l, rows));
        }
        return out;
    }

    public LoanRow row(Long loanId) {
        repaymentService.refreshStatuses();
        Loan l = get(loanId);
        return toRow(l, repaymentRepository.findByLoanIdOrderByInstallmentNo(loanId));
    }

    private LoanRow toRow(Loan l, List<Repayment> rows) {
        BigDecimal paid = rows.stream().map(Repayment::getAmountPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        RepaymentStatus status;
        if (rows.stream().anyMatch(r -> r.getStatus() == RepaymentStatus.OVERDUE)) {
            status = RepaymentStatus.OVERDUE;
        } else if (!rows.isEmpty() && rows.stream().allMatch(r -> r.getStatus() == RepaymentStatus.PAID)) {
            status = RepaymentStatus.PAID;
        } else {
            status = RepaymentStatus.PENDING;
        }
        Borrower b = l.getBorrower();
        return new LoanRow(l.getId(), b.getId(), b.getName(), b.getVillage(), l.getAmount(),
                paid.setScale(2, RoundingMode.HALF_UP), l.getInstallments(), rows.size(),
                l.getFrequency(), l.getStartDate(), status);
    }
}
