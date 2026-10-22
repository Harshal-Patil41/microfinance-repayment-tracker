package com.mfi.tracker;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Every page renders, the health endpoint is UP, and the pay route rejects bad amounts. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SmokeTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void rootRedirectsToDashboard() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void allPagesRender() throws Exception {
        for (String path : new String[] {"/dashboard", "/dashboard?q=ravi", "/borrowers", "/borrowers/new", "/loans/new",
                "/loans/1", "/repayments", "/repayments?status=OVERDUE", "/repayments?status=PAID", "/alerts", "/about"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
    }

    @Test
    void dashboardShowsIndicators() throws Exception {
        mvc.perform(get("/dashboard"))
                .andExpect(content().string(containsString("total-disbursed")))
                .andExpect(content().string(containsString("overdue-link")));
    }

    @Test
    void healthEndpointIsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("UP")));
    }

    @Test
    void payRouteRejectsExcessAmountWithMessage() throws Exception {
        mvc.perform(post("/repayments/1/pay").param("amountPaid", "5000"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void unknownLoanGives404() throws Exception {
        mvc.perform(get("/loans/9999")).andExpect(status().isNotFound());
    }

    @Test
    void invalidLoanFormShowsErrors() throws Exception {
        mvc.perform(post("/loans").param("borrowerId", "1").param("amount", "0")
                        .param("startDate", "2026-01-01").param("installments", "0").param("frequency", "WEEKLY"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Loan amount must be greater than 0")))
                .andExpect(content().string(containsString("At least 1 installment is required")));
    }
}
