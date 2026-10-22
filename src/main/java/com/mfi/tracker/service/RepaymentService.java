package com.mfi.tracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mfi.tracker.exception.BusinessException;
import com.mfi.tracker.exception.NotFoundException;
import com.mfi.tracker.model.Repayment;
import com.mfi.tracker.model.RepaymentStatus;
import com.mfi.tracker.repository.RepaymentRepository;

/**
 * Status rules (BR4-BR6):
 *   PAID    : amountPaid >= amountDue
 *   OVERDUE : not paid and dueDate is before today
 *   PENDING : not paid and dueDate is today or later
 */
@Service
@Transactional
public class RepaymentService {

    private final RepaymentRepository repository;
    private final Clock clock;

    public RepaymentService(RepaymentRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public static RepaymentStatus statusOf(Repayment r, LocalDate today) {
        if (r.getAmountPaid().compareTo(r.getAmountDue()) >= 0) {
            return RepaymentStatus.PAID;
        }
        if (r.getDueDate().isBefore(today)) {
            return RepaymentStatus.OVERDUE;
        }
        return RepaymentStatus.PENDING;
    }

    /** Re-derives every status from the stored amounts and today's date. Called before any read. */
    public void refreshStatuses() {
        LocalDate today = today();
        for (Repayment r : repository.findAll()) {
            RepaymentStatus s = statusOf(r, today);
            if (r.getStatus() != s) {
                r.setStatus(s);
            }
        }
    }

    /** All repayments (status == null) or only those with the given status, earliest due date first. */
    public List<Repayment> byStatus(RepaymentStatus status) {
        refreshStatuses();
        return repository.findAll().stream()
                .filter(r -> status == null || r.getStatus() == status)
                .sorted(Comparator.comparing(Repayment::getDueDate).thenComparing(Repayment::getId))
                .collect(Collectors.toList());
    }

    public List<Repayment> forLoan(Long loanId) {
        refreshStatuses();
        return repository.findByLoanIdOrderByInstallmentNo(loanId);
    }

    public Repayment get(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Installment " + id + " was not found"));
    }

    /** Records a payment: amount must be > 0 and not exceed what is still due on the installment. */
    public Repayment pay(Long id, BigDecimal amountPaid) {
        Repayment r = get(id);
        if (amountPaid == null || amountPaid.signum() <= 0) {
            throw new BusinessException("Payment amount must be greater than 0");
        }
        BigDecimal amount = amountPaid.setScale(2, RoundingMode.HALF_UP);
        BigDecimal remaining = r.getAmountDue().subtract(r.getAmountPaid());
        if (remaining.signum() <= 0) {
            throw new BusinessException("This installment is already fully paid");
        }
        if (amount.compareTo(remaining) > 0) {
            throw new BusinessException(String.format("Payment of ₹%,.2f exceeds the amount still due (₹%,.2f)", amount, remaining));
        }
        r.setAmountPaid(r.getAmountPaid().add(amount));
        r.setStatus(statusOf(r, today()));
        return r;
    }
}
