package com.mfi.tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mfi.tracker.dto.LoanForm;
import com.mfi.tracker.exception.BusinessException;
import com.mfi.tracker.model.Frequency;
import com.mfi.tracker.model.Loan;
import com.mfi.tracker.service.BorrowerService;
import com.mfi.tracker.service.LoanService;
import com.mfi.tracker.service.RepaymentService;

import jakarta.validation.Valid;

@Controller
public class LoanController {

    private final LoanService loanService;
    private final BorrowerService borrowerService;
    private final RepaymentService repaymentService;

    public LoanController(LoanService loanService, BorrowerService borrowerService, RepaymentService repaymentService) {
        this.loanService = loanService;
        this.borrowerService = borrowerService;
        this.repaymentService = repaymentService;
    }

    @GetMapping("/loans")
    public String list() {
        return "redirect:/dashboard";
    }

    @GetMapping("/loans/new")
    public String newForm(@RequestParam(required = false) Long borrowerId, Model model) {
        LoanForm form = new LoanForm();
        form.setBorrowerId(borrowerId);
        form.setStartDate(repaymentService.today());
        form.setInstallments(10);
        form.setFrequency(Frequency.WEEKLY);
        addFormData(model);
        model.addAttribute("form", form);
        return "loan-form";
    }

    @PostMapping("/loans")
    public String create(@Valid @ModelAttribute("form") LoanForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        addFormData(model);
        if (result.hasErrors()) {
            return "loan-form";
        }
        try {
            Loan saved = loanService.create(form);
            redirect.addFlashAttribute("success",
                    "Loan #" + saved.getId() + " was created with " + saved.getInstallments() + " installments.");
            return "redirect:/loans/" + saved.getId();
        } catch (BusinessException ex) {
            result.rejectValue("amount", "amount.invalid", ex.getMessage());
            return "loan-form";
        }
    }

    @GetMapping("/loans/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("loan", loanService.row(id));
        model.addAttribute("repayments", repaymentService.forLoan(id));
        return "loan-detail";
    }

    private void addFormData(Model model) {
        model.addAttribute("borrowers", borrowerService.findAll());
        model.addAttribute("frequencies", Frequency.values());
    }
}
