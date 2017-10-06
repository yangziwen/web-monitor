package io.github.yangziwen.webmonitor.metrics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import io.github.yangziwen.webmonitor.metrics.bean.ElementRing;
import io.github.yangziwen.webmonitor.metrics.bean.NginxAccess;
import io.github.yangziwen.webmonitor.metrics.bean.UrlMetrics;
import io.github.yangziwen.webmonitor.model.UrlMetricsResult;
import io.github.yangziwen.webmonitor.service.MonitorService;

public class UrlMetricsManager {

    private static final int DEFAULT_RING_CAPACITY = 512;

    private static ConcurrentHashMap<String, ElementRing<NginxAccess>> urlRingMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, UrlMetrics> urlMetricsMap = new ConcurrentHashMap<>();

    private static Date metricsRenewTime = new Date();

    private UrlMetricsManager() {}

    public static void doStats(NginxAccess access) {
        String pattern = UrlPatternManager.getBestMatchedUrlPattern(access.getBackendUrl());
        ensureRing(pattern).add(access);
        ensureMetrics(pattern).doStats(access);
    }

    private static ElementRing<NginxAccess> ensureRing(String urlPattern) {
        ConcurrentHashMap<String, ElementRing<NginxAccess>> ringMap = urlRingMap;
        if (!ringMap.containsKey(urlPattern)) {
            ringMap.putIfAbsent(urlPattern, new ElementRing<>(DEFAULT_RING_CAPACITY, NginxAccess.class));
        }
        return ringMap.get(urlPattern);
    }

    private static UrlMetrics ensureMetrics(String urlPattern) {
        ConcurrentHashMap<String, UrlMetrics> metricsMap = urlMetricsMap;
        if (!metricsMap.containsKey(urlPattern)) {
            metricsMap.putIfAbsent(urlPattern, new UrlMetrics(urlPattern));
        }
        return metricsMap.get(urlPattern);
    }

    public static List<UrlMetrics> getLatestUrlMectricsList(int n) {
        ConcurrentHashMap<String, ElementRing<NginxAccess>> ringMap = urlRingMap;
        List<String> patterns = new ArrayList<>(ringMap.keySet());
        return patterns.stream()
                .map(pattern -> UrlMetrics.fromLatestRingElements(pattern, ringMap.get(pattern), n))
                .collect(Collectors.toList());
    }

    public static List<UrlMetrics> getMetricsListAfterRenewTime() {
        return new ArrayList<>(urlMetricsMap.values());
    }

    // 配置一个定时任务，每10分钟收集一次(global.config中配置)
    public synchronized static void harvestMetricsResults() {
        Date previousTime = metricsRenewTime;
        Date currentTime = new Date();
        List<UrlMetrics> metricsList = new ArrayList<>(urlMetricsMap.values());
        urlMetricsMap = new ConcurrentHashMap<>();
        metricsRenewTime = currentTime;
        if (CollectionUtils.isEmpty(metricsList)) {
            return;
        }
        List<UrlMetricsResult> results = metricsList.stream()
                .map(metrics -> UrlMetricsResult.from(metrics, previousTime, currentTime))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        MonitorService.batchSaveUrlMetricsResults(results);
    }

    public static Date getMetricsRenewTime() {
        return new Date(metricsRenewTime.getTime());
    }

}
