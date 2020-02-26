package com.leoat.urlshorteningapp.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {

    private Long code;
    private String message;

    public static ErrorResponse notFound(Object id, ErrorCode code) {
        return ErrorResponse.builder()
                .code(code.getCode())
                .message(String.format("Object not found for the id specified: %s", id))
                .build();
    }

    public static ErrorResponse unexpectedError(String errorMessage) {
        return ErrorResponse.builder()
                .code(ErrorCode.UNEXPECTED_ERROR.getCode())
                .message(errorMessage)
                .build();
    }
}
