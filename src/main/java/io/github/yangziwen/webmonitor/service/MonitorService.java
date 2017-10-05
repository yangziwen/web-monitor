package io.github.yangziwen.webmonitor.service;

import static io.github.yangziwen.webmonitor.util.DataSourceFactory.getDataSource;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import io.github.yangziwen.webmonitor.metrics.UrlPatternManager;
import io.github.yangziwen.webmonitor.metrics.bean.UrlMetrics;
import io.github.yangziwen.webmonitor.model.UrlMetricsResult;
import io.github.yangziwen.webmonitor.model.UrlPattern;
import io.github.yangziwen.webmonitor.repository.UrlMetricsResultRepo;
import io.github.yangziwen.webmonitor.repository.UrlPatternRepo;
import io.github.yangziwen.webmonitor.repository.base.QueryMap;

public class MonitorService {

    private static final UrlPatternRepo urlPatternRepo = new UrlPatternRepo(getDataSource());

    private static final UrlMetricsResultRepo urlMetricsResultRepo = new UrlMetricsResultRepo(getDataSource());

    private MonitorService() {}

    public static void reloadUrlPatterns() {
        Set<String> urlPatterns = urlPatternRepo
                .list()
                .stream()
                .map(UrlPattern::getUrl)
                .collect(Collectors.toSet());
        UrlPatternManager.reloadUrlPatterns(urlPatterns);
    }

    public static List<UrlMetrics> getUrlMetricsResultsBetween(Date beginTime, Date endTime) {
        QueryMap params = new QueryMap()
                .param("beginTime__ge", beginTime)
                .param("endTime__le", endTime);
        return urlMetricsResultRepo.list(params).stream()
                .collect(Collectors.groupingBy(UrlMetricsResult::getUrl))
                .entrySet().stream()
                .map(entry -> {
                    return entry.getValue().stream().reduce(
                            new UrlMetrics(entry.getKey()),
                            (metrics, result) -> metrics.merge(result),
                            (m1, m2) -> m1.merge(m2));
                })
                .collect(Collectors.toList());
    }

    public static void batchSaveUrlMetricsResults(List<UrlMetricsResult> results) {
        if (CollectionUtils.isEmpty(results)) {
            return;
        }
        int batchSize = 50;
        for (int i = 0; i < results.size(); i += batchSize) {
            List<UrlMetricsResult> sublist = results.subList(i, Math.min(i + batchSize, results.size()));
            urlMetricsResultRepo.batchInsert(sublist);
        }
    }

}
