package io.github.yangziwen.webmonitor.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "url_pattern")
public class UrlPattern {

    @Id
    @Column
    private Long id;

    @Column
    private String url;

    @Column
    private String project;

    public UrlPattern() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

}
