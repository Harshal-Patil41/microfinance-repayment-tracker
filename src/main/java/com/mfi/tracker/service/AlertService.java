package com.mfi.tracker.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mfi.tracker.dto.AlertReport;
import com.mfi.tracker.dto.AlertRow;
import com.mfi.tracker.dto.MissingSchedule;
import com.mfi.tracker.model.Loan;
import com.mfi.tracker.model.Repayment;
import com.mfi.tracker.model.RepaymentStatus;
import com.mfi.tracker.repository.LoanRepository;
import com.mfi.tracker.repository.RepaymentRepository;

/**
 * BR7 and FR10/FR11:
 *  - late     : OVERDUE and today is MORE than 7 days after the due date
 *  - invalid  : amountPaid is negative or larger than amountDue (possible in data loaded outside the forms)
 *  - missing  : a loan has fewer repayment rows than its installment count
 */
@Service
@Transactional
public class AlertService {

    public static final int LATE_AFTER_DAYS = 7;

    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;
    private final RepaymentService repaymentService;

    public AlertService(LoanRepository loanRepository, RepaymentRepository repaymentRepository,
                        RepaymentService repaymentService) {
        this.loanRepository = loanRepository;
        this.repaymentRepository = repaymentRepository;
        this.repaymentService = repaymentService;
    }

    public AlertReport report() {
        repaymentService.refreshStatuses();
        LocalDate today = repaymentService.today();
        List<Repayment> all = repaymentRepository.findAll();

        List<AlertRow> late = new ArrayList<>();
        List<AlertRow> invalid = new ArrayList<>();
        Map<Long, Integer> rowCount = new HashMap<>();

        for (Repayment r : all) {
            rowCount.merge(r.getLoan().getId(), 1, Integer::sum);
            long days = ChronoUnit.DAYS.between(r.getDueDate(), today);
            if (r.getStatus() == RepaymentStatus.OVERDUE && days > LATE_AFTER_DAYS) {
                late.add(row(r, days, days + " days past due"));
            }
            if (r.getAmountPaid().signum() < 0) {
                invalid.add(row(r, Math.max(days, 0), "Negative amount paid"));
            } else if (r.getAmountPaid().compareTo(r.getAmountDue()) > 0) {
                invalid.add(row(r, Math.max(days, 0), "Amount paid is above the amount due"));
            }
        }
        late.sort(Comparator.comparing((AlertRow a) -> a.daysOverdue).reversed().thenComparing(a -> a.repaymentId));
        invalid.sort(Comparator.comparing((AlertRow a) -> a.repaymentId));

        List<MissingSchedule> missing = new ArrayList<>();
        for (Loan l : loanRepository.findAll(Sort.by("id"))) {
            int found = rowCount.getOrDefault(l.getId(), 0);
            if (found < l.getInstallments()) {
                missing.add(new MissingSchedule(l.getId(), l.getBorrower().getName(), l.getBorrower().getVillage(),
                        l.getInstallments(), found));
            }
        }
        return new AlertReport(late, invalid, missing);
    }

    private static AlertRow row(Repayment r, long days, String reason) {
        return new AlertRow(r.getId(), r.getLoan().getId(), r.getInstallmentNo(), r.getLoan().getBorrower().getName(),
                r.getLoan().getBorrower().getVillage(), r.getDueDate(), r.getAmountDue(), r.getAmountPaid(), days, reason);
    }
}
