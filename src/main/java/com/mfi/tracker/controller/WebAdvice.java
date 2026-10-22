package com.mfi.tracker.controller;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import com.mfi.tracker.exception.BusinessException;
import com.mfi.tracker.exception.NotFoundException;

/** Shared page data and friendly error pages for all controllers. */
@ControllerAdvice
public class WebAdvice {

    private final Clock clock;

    public WebAdvice(Clock clock) {
        this.clock = clock;
    }

    @ModelAttribute("today")
    public LocalDate today() {
        return LocalDate.now(clock);
    }

    @ExceptionHandler(NotFoundException.class)
    public ModelAndView notFound(NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Not found", ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView business(BusinessException ex) {
        return error(HttpStatus.BAD_REQUEST, "Request rejected", ex.getMessage());
    }

    private ModelAndView error(HttpStatus status, String title, String message) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("errorTitle", title);
        mav.addObject("errorMessage", message);
        return mav;
    }
}
