package com.mfi.tracker.controller;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mfi.tracker.exception.BusinessException;
import com.mfi.tracker.model.Repayment;
import com.mfi.tracker.model.RepaymentStatus;
import com.mfi.tracker.service.RepaymentService;

@Controller
public class RepaymentController {

    private final RepaymentService repaymentService;

    public RepaymentController(RepaymentService repaymentService) {
        this.repaymentService = repaymentService;
    }

    /** US6: list repayments, optionally filtered by status (PAID, PENDING or OVERDUE). */
    @GetMapping("/repayments")
    public String list(@RequestParam(required = false) RepaymentStatus status, Model model) {
        model.addAttribute("repayments", repaymentService.byStatus(status));
        model.addAttribute("status", status);
        model.addAttribute("statuses", RepaymentStatus.values());
        return "repayments";
    }

    /** US3: record a payment against one installment. */
    @PostMapping("/repayments/{id}/pay")
    public String pay(@PathVariable Long id, @RequestParam(required = false) BigDecimal amountPaid,
                      RedirectAttributes redirect) {
        Repayment installment = repaymentService.get(id);
        Long loanId = installment.getLoan().getId();
        try {
            Repayment saved = repaymentService.pay(id, amountPaid);
            redirect.addFlashAttribute("success", String.format(
                    "Payment recorded for installment %d. Status is now %s.", saved.getInstallmentNo(), saved.getStatus()));
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", "Payment rejected: " + ex.getMessage());
        }
        return "redirect:/loans/" + loanId;
    }
}
