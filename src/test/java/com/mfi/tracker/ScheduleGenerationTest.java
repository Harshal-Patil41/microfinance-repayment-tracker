package com.mfi.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.mfi.tracker.dto.LoanForm;
import com.mfi.tracker.exception.BusinessException;
import com.mfi.tracker.model.Borrower;
import com.mfi.tracker.model.Frequency;
import com.mfi.tracker.model.Loan;
import com.mfi.tracker.model.Repayment;
import com.mfi.tracker.service.BorrowerService;
import com.mfi.tracker.service.LoanService;
import com.mfi.tracker.service.RepaymentService;

/** US2 / FR2 / FR3 / BR2 / BR3: schedule generation. */
@SpringBootTest
@Transactional
class ScheduleGenerationTest {

    @Autowired
    private BorrowerService borrowerService;
    @Autowired
    private LoanService loanService;
    @Autowired
    private RepaymentService repaymentService;

    private Loan create(String amount, int n, Frequency f, LocalDate start) {
        Borrower b = borrowerService.create(new Borrower("Schedule Tester", "9000000001", "Testpur"));
        LoanForm form = new LoanForm();
        form.setBorrowerId(b.getId());
        form.setAmount(new BigDecimal(amount));
        form.setInstallments(n);
        form.setFrequency(f);
        form.setStartDate(start);
        return loanService.create(form);
    }

    @Test
    void weeklyScheduleHasOneRowPerInstallmentAndLastRowAbsorbsRounding() {
        Loan loan = create("1000", 3, Frequency.WEEKLY, LocalDate.of(2026, 1, 1));
        List<Repayment> rows = repaymentService.forLoan(loan.getId());
        assertEquals(3, rows.size());
        assertEquals(new BigDecimal("333.33"), rows.get(0).getAmountDue());
        assertEquals(new BigDecimal("333.33"), rows.get(1).getAmountDue());
        assertEquals(new BigDecimal("333.34"), rows.get(2).getAmountDue());
        assertEquals(LocalDate.of(2026, 1, 8), rows.get(0).getDueDate());
        assertEquals(LocalDate.of(2026, 1, 15), rows.get(1).getDueDate());
        assertEquals(LocalDate.of(2026, 1, 22), rows.get(2).getDueDate());
        BigDecimal sum = rows.stream().map(Repayment::getAmountDue).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("1000.00"), sum);
    }

    @Test
    void monthlyScheduleAddsOneMonthPerInstallment() {
        Loan loan = create("900", 3, Frequency.MONTHLY, LocalDate.of(2026, 1, 31));
        List<Repayment> rows = repaymentService.forLoan(loan.getId());
        assertEquals(LocalDate.of(2026, 2, 28), rows.get(0).getDueDate());
        assertEquals(LocalDate.of(2026, 3, 31), rows.get(1).getDueDate());
        assertEquals(LocalDate.of(2026, 4, 30), rows.get(2).getDueDate());
    }

    @Test
    void amountTooSmallForInstallmentsIsRejected() {
        assertThrows(BusinessException.class, () -> create("0.05", 10, Frequency.WEEKLY, LocalDate.of(2026, 1, 1)));
    }
}
