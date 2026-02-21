package com.silentsupply.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response returned by the API for all error conditions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** HTTP status code. */
    private int status;

    /** Short error description. */
    private String message;

    /** Timestamp when the error occurred. */
    private LocalDateTime timestamp;

    /** Detailed validation errors, if applicable. */
    private List<String> errors;
}
