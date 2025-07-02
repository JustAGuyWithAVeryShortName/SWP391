package com.swp2.demo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    // This field is less relevant now, as the exception will be handled by @ControllerAdvice
    // but kept for compatibility if you have other uses.
    private String errorPage;

    public CustomAccessDeniedHandler(String errorPage) {
        this.errorPage = errorPage;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        System.err.println("CustomAccessDeniedHandler: Re-throwing AccessDeniedException to be caught by @ControllerAdvice.");
        // Re-throw the exception. Spring will then look for an @ExceptionHandler.
        // This prevents the default Whitelabel Error Page from rendering prematurely.
        throw accessDeniedException;
    }
}