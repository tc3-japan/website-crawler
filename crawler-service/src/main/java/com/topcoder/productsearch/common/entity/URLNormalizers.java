package com.topcoder.productsearch.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "url_normalizers")
public class URLNormalizers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "website_id")
    private Integer websiteId;

    @Column(name = "regex_pattern")
    private String regexPattern;

    @Column(name = "substitution")
    private String substitution;

}