package com.leoat.urlshorteningapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@Table("url_info")
@NoArgsConstructor
@AllArgsConstructor
public class UrlInfo implements Serializable {

    @Id
    private Integer id;
    private String longUrl;
    private String shortUrl;
    private LocalDate expiryAt;

}
