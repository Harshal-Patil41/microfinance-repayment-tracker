package com.mfi.tracker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/borrowers")
public class BorrowerController {

    private final BorrowerRepository repository;

    public BorrowerController(BorrowerRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("borrowers", repository.findAll());
        return "borrowers";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("borrower", new Borrower());
        return "borrower-form";
    }

    @PostMapping
    public String save(@ModelAttribute Borrower borrower, Model model) {
        if (borrower.getName() == null || borrower.getName().isBlank()) {
            model.addAttribute("error", "Name is required.");
            return "borrower-form";
        }
        repository.save(borrower); 
        borrower.setName(borrower.getName().trim());
        repository.save(borrower);
        return "redirect:/borrowers";
    }
}