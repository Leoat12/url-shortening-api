package com.leoat.urlshorteningapp.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
public class UrlGenerateRequest implements Serializable {

    @NotBlank
    private String longUrl;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate expiryAt;

}
