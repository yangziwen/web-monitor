package io.github.yangziwen.webmonitor.service;

import static io.github.yangziwen.webmonitor.util.DataSourceFactory.getDataSource;

import java.util.Set;
import java.util.stream.Collectors;

import io.github.yangziwen.webmonitor.metrics.UrlPatternManager;
import io.github.yangziwen.webmonitor.model.UrlPattern;
import io.github.yangziwen.webmonitor.repository.UrlPatternRepo;

public class MonitorService {

    private static final UrlPatternRepo urlPatternRepo = new UrlPatternRepo(getDataSource());

    private MonitorService() {}

    public static void reloadUrlPatterns() {
        Set<String> urlPatterns = urlPatternRepo
                .list()
                .stream()
                .map(UrlPattern::getUrl)
                .collect(Collectors.toSet());
        UrlPatternManager.reloadUrlPatterns(urlPatterns);
    }

}
