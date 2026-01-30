package com.alperen.websecurity.exception;

import java.time.Instant;
import java.util.List;

public class ApiErrorResponse {
    private Instant timestamp;
    private int status;
    private List<ApiError> errors;

    public ApiErrorResponse(Instant timestamp, int status, List<ApiError> errors) {
        this.timestamp = timestamp;
        this.status = status;
        this.errors = errors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public List<ApiError> getErrors() {
        return errors;
    }

    public static class ApiError {
        private String field;
        private String message;

        public ApiError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}
