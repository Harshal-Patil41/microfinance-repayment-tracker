package com.mfi.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mfi.tracker.dto.LoanRow;
import com.mfi.tracker.exception.BusinessException;
import com.mfi.tracker.model.Repayment;
import com.mfi.tracker.model.RepaymentStatus;
import com.mfi.tracker.service.LoanService;
import com.mfi.tracker.service.RepaymentService;

/** US3 / FR4 / FR5 / BR4-BR6: payments and statuses. Uses the seeded data. */
@SpringBootTest
@Transactional
class PaymentRulesTest {

    @Autowired
    private LoanService loanService;
    @Autowired
    private RepaymentService repaymentService;

    private LoanRow loan(String borrower, String amount) {
        return loanService.rows(null).stream()
                .filter(r -> r.borrowerName.equals(borrower) && r.amount.compareTo(new BigDecimal(amount)) == 0)
                .findFirst().orElseThrow();
    }

    /** First installment of Imran Sheikh's 12,000 loan: 2,000 due in about a month, nothing paid. */
    private Repayment imranFirst() {
        return repaymentService.forLoan(loan("Imran Sheikh", "12000").loanId).get(0);
    }

    @Test
    void partialPaymentKeepsInstallmentPending() {
        Repayment r = imranFirst();
        repaymentService.pay(r.getId(), new BigDecimal("500"));
        assertEquals(new BigDecimal("500.00"), imranFirst().getAmountPaid());
        assertEquals(RepaymentStatus.PENDING, imranFirst().getStatus());
    }

    @Test
    void fullPaymentMakesInstallmentPaid() {
        Repayment r = imranFirst();
        repaymentService.pay(r.getId(), new BigDecimal("2000"));
        assertEquals(RepaymentStatus.PAID, imranFirst().getStatus());
    }

    @Test
    void zeroNegativeAndExcessPaymentsAreRejected() {
        Long id = imranFirst().getId();
        assertThrows(BusinessException.class, () -> repaymentService.pay(id, BigDecimal.ZERO));
        assertThrows(BusinessException.class, () -> repaymentService.pay(id, new BigDecimal("-5")));
        assertThrows(BusinessException.class, () -> repaymentService.pay(id, new BigDecimal("2000.01")));
        assertThrows(BusinessException.class, () -> repaymentService.pay(id, null));
    }

    @Test
    void paidInstallmentRejectsFurtherPayment() {
        Repayment first = repaymentService.forLoan(loan("Asha Patil", "6000").loanId).get(0);
        assertEquals(RepaymentStatus.PAID, first.getStatus());
        assertThrows(BusinessException.class, () -> repaymentService.pay(first.getId(), new BigDecimal("1")));
    }

    @Test
    void partPaidInstallmentAfterDueDateIsOverdue() {
        // Meena Joshi, installment 2: due 7 days ago, 400 of 1000 paid
        Repayment second = repaymentService.forLoan(loan("Meena Joshi", "4000").loanId).get(1);
        assertEquals(RepaymentStatus.OVERDUE, second.getStatus());
        repaymentService.pay(second.getId(), new BigDecimal("600"));
        assertEquals(RepaymentStatus.PAID, repaymentService.forLoan(loan("Meena Joshi", "4000").loanId).get(1).getStatus());
    }
}
