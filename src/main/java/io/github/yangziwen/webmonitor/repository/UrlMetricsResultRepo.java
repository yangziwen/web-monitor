package io.github.yangziwen.webmonitor.repository;

import javax.sql.DataSource;

import org.sql2o.Sql2o;

import io.github.yangziwen.quickdao.sql2o.BaseSql2oRepository;
import io.github.yangziwen.webmonitor.model.UrlMetricsResult;

public class UrlMetricsResultRepo extends BaseSql2oRepository<UrlMetricsResult> {

    public UrlMetricsResultRepo(DataSource dataSource) {
        super(new Sql2o(dataSource));
    }

}
