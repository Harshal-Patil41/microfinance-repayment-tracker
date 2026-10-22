package com.mfi.tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mfi.tracker.model.Borrower;
import com.mfi.tracker.service.BorrowerService;

import jakarta.validation.Valid;

@Controller
public class BorrowerController {

    private final BorrowerService borrowerService;

    public BorrowerController(BorrowerService borrowerService) {
        this.borrowerService = borrowerService;
    }

    @GetMapping("/borrowers")
    public String list(Model model) {
        model.addAttribute("borrowers", borrowerService.findAll());
        return "borrowers";
    }

    @GetMapping("/borrowers/new")
    public String newForm(Model model) {
        model.addAttribute("borrower", new Borrower());
        return "borrower-form";
    }

    @PostMapping("/borrowers")
    public String create(@Valid @ModelAttribute("borrower") Borrower borrower, BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "borrower-form";
        }
        Borrower saved = borrowerService.create(borrower);
        redirect.addFlashAttribute("success", "Borrower " + saved.getName() + " was added.");
        return "redirect:/borrowers";
    }
}
