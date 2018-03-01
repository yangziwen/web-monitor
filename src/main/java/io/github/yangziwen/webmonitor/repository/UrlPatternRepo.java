package io.github.yangziwen.webmonitor.repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import io.github.yangziwen.webmonitor.model.UrlPattern;
import io.github.yangziwen.webmonitor.repository.base.BaseRepository;

public class UrlPatternRepo extends BaseRepository<UrlPattern> {

    public UrlPatternRepo(DataSource dataSource) {
        super(dataSource);
    }

    public List<String> getAllProjects() {
        return doList("select distinct project as project from url_pattern", Collections.<String, Object>emptyMap())
                .stream()
                .map(UrlPattern::getProject)
                .collect(Collectors.toList());
    }

}
