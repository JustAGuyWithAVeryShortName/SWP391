package com.swp2.demo.Controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.PrintWriter;
import java.io.StringWriter; // Import these two for stack trace conversion

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorMessageObject = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object errorExceptionObject = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION); // Get the Throwable object

        String generalMessage = "Something went wrong!";
        String specificErrorType = "N/A";

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            model.addAttribute("status", statusCode);

            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                generalMessage = "The page you are looking for could not be found.";
                specificErrorType = "Not Found";
                return "404";
            }
            else if(statusCode == HttpStatus.FORBIDDEN.value()) {
                generalMessage = "You do not have permission to access this page.";
                specificErrorType = "Access Denied";
                return "403";
            }
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                generalMessage = "An internal server error occurred.";
                specificErrorType = "Internal Server Error";
                return "500";
            }
        }

        if (errorMessageObject != null) {
            generalMessage = errorMessageObject.toString();
        } else if (errorExceptionObject instanceof Throwable) {
            // If a specific error message isn't set, but an exception is, try to get its message
            generalMessage = ((Throwable) errorExceptionObject).getMessage();
            if (generalMessage == null || generalMessage.trim().isEmpty()) {
                generalMessage = "An unexpected error occurred.";
            }
        }

        model.addAttribute("message", generalMessage);
        model.addAttribute("error", specificErrorType);

        // Optional: Expose trace for development (be careful in production)
        // Get the exception object and convert its stack trace to a string
        if (errorExceptionObject instanceof Throwable) {
            Throwable throwable = (Throwable) errorExceptionObject;
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            model.addAttribute("trace", sw.toString());
        }

        return "error";
    }
}