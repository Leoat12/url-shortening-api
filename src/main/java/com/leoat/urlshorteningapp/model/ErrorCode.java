package com.leoat.urlshorteningapp.model;

public enum ErrorCode {

    URL_INFO_NOT_FOUND(1L),
    UNEXPECTED_ERROR(99L);

    private Long code;

    ErrorCode(Long code) {
        this.code = code;
    }

    public Long getCode() {
        return code;
    }
}
