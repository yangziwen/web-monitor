package io.github.yangziwen.webmonitor.service;

import static io.github.yangziwen.webmonitor.util.DataSourceFactory.getDataSource;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.core.Order.Direction;
import io.github.yangziwen.webmonitor.metrics.UrlMetricsManager;
import io.github.yangziwen.webmonitor.metrics.UrlPatternManager;
import io.github.yangziwen.webmonitor.metrics.bean.PeriodUrlMetrics;
import io.github.yangziwen.webmonitor.metrics.bean.UrlMetrics;
import io.github.yangziwen.webmonitor.model.UrlMetricsResult;
import io.github.yangziwen.webmonitor.model.UrlPattern;
import io.github.yangziwen.webmonitor.repository.UrlMetricsResultRepo;
import io.github.yangziwen.webmonitor.repository.UrlPatternRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitorService {

    private static final UrlPatternRepo urlPatternRepo = new UrlPatternRepo(getDataSource());

    private static final UrlMetricsResultRepo urlMetricsResultRepo = new UrlMetricsResultRepo(getDataSource());

    private static Map<String, String> urlPatternMapping = Collections.emptyMap();

    static {
        log.info("{} url patterns are ready", UrlPatternManager.getLoadedUrlPatternCount());
    }

    private MonitorService() {}

    public static void reloadUrlPatterns() {
        Map<String, String> mapping = getUrlPatternProjectMapping();
        UrlPatternManager.reloadUrlPatterns(mapping.keySet());
        urlPatternMapping = Collections.unmodifiableMap(mapping);
    }

    public static Map<String, String> getUrlPatternProjectMapping() {
        return urlPatternRepo
                .list()
                .stream()
                .collect(Collectors.toMap(UrlPattern::getUrl, UrlPattern::getProject));
    }

    public static List<String> getAllProjects() {
        return urlPatternRepo.getAllProjects();
    }

    public static void batchSaveUrlPatterns(List<UrlPattern> patterns) {
        if (CollectionUtils.isEmpty(patterns)) {
            return;
        }
        urlPatternRepo.batchInsert(patterns);
    }

    public static void deleteUrlPatternsByParams(Criteria criteria) {
        urlPatternRepo.delete(criteria);
    }

    public static List<UrlMetrics> getUrlMetricsResultsBetween(Date beginTime, Date endTime) {
        Criteria params = new Criteria()
                .and("endTime").ge(beginTime)
                .and("endTime").le(endTime);
        return urlMetricsResultRepo.list(params).stream()
                .collect(Collectors.groupingBy(UrlMetricsResult::getUrl))
                .entrySet().stream()
                .map(entry -> {
                    return entry.getValue().stream().reduce(
                            new UrlMetrics(entry.getKey()),
                            (metrics, result) -> metrics.merge(result),
                            (m1, m2) -> m1.merge(m2));
                })
                .peek(m -> m.setProject(urlPatternMapping.get(m.getUrlPattern())))
                .collect(Collectors.toList());
    }

    public static List<UrlMetrics> getRecentUrlMetricsResults(Date beginTime) {
        List<UrlMetrics> list1 = getUrlMetricsResultsBetween(beginTime, new Date(System.currentTimeMillis()));
        List<UrlMetrics> list2 = UrlMetricsManager.getMetricsListAfterRenewTime();
        return ListUtils.union(list1, list2).stream()
                .collect(Collectors.groupingBy(UrlMetrics::getUrlPattern))
                .entrySet().stream()
                .map(entry -> {
                    return entry.getValue().stream().reduce(
                            new UrlMetrics(entry.getKey()),
                            (m1, m2) -> m1.merge(m2));
                })
                .peek(m -> m.setProject(urlPatternMapping.get(m.getUrlPattern())))
                .collect(Collectors.toList());
    }

    public static List<UrlMetrics> getUrlMetricsResultsOfUrl(String url, Date beginTime, Date endTime) {
        Criteria criteria = new Criteria()
                .and("endTime").ge(beginTime)
                .and("endTime").le(endTime)
                .and("url").eq(url);
        Query query = new Query()
                .where(criteria)
                .orderBy("endTime", Direction.DESC);
        return urlMetricsResultRepo.list(query).stream()
                .map(result -> new PeriodUrlMetrics(result))
                .peek(m -> m.setProject(urlPatternMapping.get(m.getUrlPattern())))
                .collect(Collectors.toList());
    }

    public static void deleteUrlMetricsResultsByParams(Criteria criteria) {
        urlMetricsResultRepo.delete(criteria);
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
