// src/main/java/com/swp2/demo/security/CustomAccessDeniedHandler.java
package com.swp2.demo.security; // Or wherever your security classes are

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

import static jakarta.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static jakarta.servlet.RequestDispatcher.ERROR_MESSAGE;
import static jakarta.servlet.RequestDispatcher.ERROR_REQUEST_URI;
import static jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private String errorPage; // To store the path to your error controller, e.g., "/error"

    public CustomAccessDeniedHandler(String errorPage) {
        this.errorPage = errorPage;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        // Log the exception for debugging
        System.err.println("Access Denied: " + accessDeniedException.getMessage() + " for URI: " + request.getRequestURI());
        accessDeniedException.printStackTrace();

        // Set attributes that CustomErrorController can read
        request.setAttribute(ERROR_STATUS_CODE, 403);
        request.setAttribute(ERROR_MESSAGE, "You do not have permission to access this resource.");
        request.setAttribute(ERROR_EXCEPTION, accessDeniedException); // Pass the exception itself
        request.setAttribute(ERROR_REQUEST_URI, request.getRequestURI());

        // Forward to the /error controller
        request.getRequestDispatcher(errorPage).forward(request, response);
    }
}