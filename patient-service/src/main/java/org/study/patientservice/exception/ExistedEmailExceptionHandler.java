package org.study.patientservice.exception;

public class ExistedEmailExceptionHandler extends RuntimeException {
    public ExistedEmailExceptionHandler(String message) {
        super(message);
    }
}
