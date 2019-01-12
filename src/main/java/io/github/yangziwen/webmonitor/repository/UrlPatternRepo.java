package io.github.yangziwen.webmonitor.repository;

import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.sql2o.Sql2o;

import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.sql2o.BaseSql2oRepository;
import io.github.yangziwen.webmonitor.model.UrlPattern;

public class UrlPatternRepo extends BaseSql2oRepository<UrlPattern> {

    public UrlPatternRepo(DataSource dataSource) {
        super(new Sql2o(dataSource));
    }

    public List<String> getAllProjects() {
        Query query = new Query().select("distinct project as project");
        return list(query).stream()
                .map(UrlPattern::getProject)
                .sorted()
                .collect(Collectors.toList());
    }

}
