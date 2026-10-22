package com.mfi.tracker.config;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mfi.tracker.dto.LoanForm;
import com.mfi.tracker.model.Borrower;
import com.mfi.tracker.model.Frequency;
import com.mfi.tracker.model.Loan;
import com.mfi.tracker.model.Repayment;
import com.mfi.tracker.repository.RepaymentRepository;
import com.mfi.tracker.service.BorrowerService;
import com.mfi.tracker.service.LoanService;
import com.mfi.tracker.service.RepaymentService;

/**
 * Dummy demonstration data (FR13), dated relative to today (T) so overdue/late cases always exist.
 * Runs only when the database is empty. Payments are written directly (not through the form rules)
 * so that the data can include the invalid rows the alert view must detect.
 *
 *   Loan  Borrower          Amount  Schedule        Notes
 *   1     Asha Patil        6000    4 weekly, T-35  all paid
 *   2     Ravi Kumar        5000    5 weekly, T-42  2 paid, 3 overdue (two are late > 7 days)
 *   3     Meena Joshi       4000    4 weekly, T-21  1 paid, 1 part-paid overdue (exactly 7 days: no alert)
 *   4     Imran Sheikh      12000   6 monthly, T-5  nothing paid yet (all pending)
 *   5     Lakshmi Nair      3000    3 weekly, T-30  all paid
 *   6     Suresh Rao        8000    4 weekly, T-60  1 paid, 3 overdue and late
 *   7     Pooja Deshmukh    2000    4 weekly, T-14  1 paid, rest pending
 *   8     Farah Khan        9000    3 monthly, T-3  nothing paid yet
 *   9     Asha Patil        1500    3 weekly, T-7   nothing paid yet (first due today)
 *   10    Farah Khan        2000    4 weekly, T-28  INVALID rows: paid 600 > due 500, paid -100
 *   11    Lakshmi Nair      3000    4 weekly, T-14  MISSING schedule rows (only 2 of 4)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final BorrowerService borrowerService;
    private final LoanService loanService;
    private final RepaymentService repaymentService;
    private final RepaymentRepository repaymentRepository;
    private final Clock clock;
    private final boolean enabled;

    public DataSeeder(BorrowerService borrowerService, LoanService loanService, RepaymentService repaymentService,
                      RepaymentRepository repaymentRepository, Clock clock,
                      @Value("${app.seed-enabled:true}") boolean enabled) {
        this.borrowerService = borrowerService;
        this.loanService = loanService;
        this.repaymentService = repaymentService;
        this.repaymentRepository = repaymentRepository;
        this.clock = clock;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled || borrowerService.count() > 0) {
            return;
        }
        Borrower asha = b("Asha Patil", "9876500001", "Rampur");
        Borrower ravi = b("Ravi Kumar", "9876500002", "Shivpur");
        Borrower meena = b("Meena Joshi", "9876500003", "Kharghar");
        Borrower imran = b("Imran Sheikh", "9876500004", "Nandgaon");
        Borrower lakshmi = b("Lakshmi Nair", "9876500005", "Pimpri");
        Borrower suresh = b("Suresh Rao", "9876500006", "Wadi");
        Borrower pooja = b("Pooja Deshmukh", "9876500007", "Sonapur");
        Borrower farah = b("Farah Khan", "9876500008", "Lonavla");

        Loan l1 = loan(asha, "6000", 4, Frequency.WEEKLY, -35);
        paidInFull(l1, 1, 2, 3, 4);

        Loan l2 = loan(ravi, "5000", 5, Frequency.WEEKLY, -42);
        paidInFull(l2, 1, 2);

        Loan l3 = loan(meena, "4000", 4, Frequency.WEEKLY, -21);
        paidInFull(l3, 1);
        setPaid(l3, 2, "400");

        loan(imran, "12000", 6, Frequency.MONTHLY, -5);

        Loan l5 = loan(lakshmi, "3000", 3, Frequency.WEEKLY, -30);
        paidInFull(l5, 1, 2, 3);

        Loan l6 = loan(suresh, "8000", 4, Frequency.WEEKLY, -60);
        paidInFull(l6, 1);

        Loan l7 = loan(pooja, "2000", 4, Frequency.WEEKLY, -14);
        paidInFull(l7, 1);

        loan(farah, "9000", 3, Frequency.MONTHLY, -3);
        loan(asha, "1500", 3, Frequency.WEEKLY, -7);

        Loan l10 = loan(farah, "2000", 4, Frequency.WEEKLY, -28);
        setPaid(l10, 1, "600");      // invalid: above the amount due (500)
        setPaid(l10, 2, "-100");     // invalid: negative
        setPaid(l10, 3, "500");

        Loan l11 = loan(lakshmi, "3000", 4, Frequency.WEEKLY, -14);
        setPaid(l11, 1, "750");
        List<Repayment> rows = repaymentRepository.findByLoanIdOrderByInstallmentNo(l11.getId());
        repaymentRepository.deleteAll(rows.stream().filter(r -> r.getInstallmentNo() > 2).toList());

        repaymentService.refreshStatuses();
    }

    private Borrower b(String name, String phone, String village) {
        return borrowerService.create(new Borrower(name, phone, village));
    }

    private Loan loan(Borrower borrower, String amount, int installments, Frequency frequency, int startOffsetDays) {
        LoanForm f = new LoanForm();
        f.setBorrowerId(borrower.getId());
        f.setAmount(new BigDecimal(amount));
        f.setInstallments(installments);
        f.setFrequency(frequency);
        f.setStartDate(LocalDate.now(clock).plusDays(startOffsetDays));
        return loanService.create(f);
    }

    private void paidInFull(Loan loan, int... installmentNos) {
        for (int no : installmentNos) {
            for (Repayment r : repaymentRepository.findByLoanIdOrderByInstallmentNo(loan.getId())) {
                if (r.getInstallmentNo() == no) {
                    r.setAmountPaid(r.getAmountDue());
                }
            }
        }
    }

    private void setPaid(Loan loan, int installmentNo, String amount) {
        for (Repayment r : repaymentRepository.findByLoanIdOrderByInstallmentNo(loan.getId())) {
            if (r.getInstallmentNo() == installmentNo) {
                r.setAmountPaid(new BigDecimal(amount));
            }
        }
    }
}
