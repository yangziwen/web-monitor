package io.github.yangziwen.webmonitor.repository;

import javax.sql.DataSource;

import io.github.yangziwen.webmonitor.model.UrlMetricsResult;
import io.github.yangziwen.webmonitor.repository.base.BaseRepository;

public class UrlMetricsResultRepo extends BaseRepository<UrlMetricsResult> {

    public UrlMetricsResultRepo(DataSource dataSource) {
        super(dataSource);
    }

}
