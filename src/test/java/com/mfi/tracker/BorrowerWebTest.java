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

/** US1 / FR1 / FR12: add a borrower with validation. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BorrowerWebTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void formRenders() throws Exception {
        mvc.perform(get("/borrowers/new")).andExpect(status().isOk());
    }

    @Test
    void validBorrowerIsSavedAndListed() throws Exception {
        mvc.perform(post("/borrowers").param("name", "Unit Tester").param("phone", "9123456780").param("village", "Testpur"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/borrowers"));
        mvc.perform(get("/borrowers")).andExpect(content().string(containsString("Unit Tester")));
    }

    @Test
    void emptyNameShowsErrorAndSavesNothing() throws Exception {
        mvc.perform(post("/borrowers").param("name", "").param("phone", "9123456780").param("village", "Testpur"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Name is required")));
    }

    @Test
    void badPhoneShowsError() throws Exception {
        mvc.perform(post("/borrowers").param("name", "Bad Phone").param("phone", "123").param("village", "Testpur"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Phone must be exactly 10 digits")));
    }
}
