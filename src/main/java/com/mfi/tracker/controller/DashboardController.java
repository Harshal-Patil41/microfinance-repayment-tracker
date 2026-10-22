package com.mfi.tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mfi.tracker.service.DashboardService;
import com.mfi.tracker.service.LoanService;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final LoanService loanService;

    public DashboardController(DashboardService dashboardService, LoanService loanService) {
        this.dashboardService = dashboardService;
        this.loanService = loanService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("ind", dashboardService.indicators());
        model.addAttribute("loans", loanService.rows(q));
        model.addAttribute("q", q == null ? "" : q);
        return "dashboard";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("javaVersion", System.getProperty("java.version"));
        return "about";
    }
}
