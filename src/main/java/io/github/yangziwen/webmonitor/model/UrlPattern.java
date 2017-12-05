package io.github.yangziwen.webmonitor.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Table(name = "url_pattern")
public class UrlPattern {

    @Id
    @Column
    private Long id;

    @Column
    private String url;

    @Column
    private String project;

}
