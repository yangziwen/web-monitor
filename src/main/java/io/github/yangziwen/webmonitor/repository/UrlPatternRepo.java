package io.github.yangziwen.webmonitor.repository;

import javax.sql.DataSource;

import io.github.yangziwen.webmonitor.model.UrlPattern;
import io.github.yangziwen.webmonitor.repository.base.BaseRepository;

public class UrlPatternRepo extends BaseRepository<UrlPattern> {

    public UrlPatternRepo(DataSource dataSource) {
        super(dataSource);
    }

}
