package uk.cf.ac.LegalandGeneralTeam11.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle any exception that is thrown in the application
     * @param e `Exception` object
     * @return `String` object
     */

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e) {
        System.out.println("Exception: " + e.getMessage());
        return "error/500";
    }
}
