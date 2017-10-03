package io.github.yangziwen.webmonitor.stats;

import java.util.concurrent.ConcurrentHashMap;

import io.github.yangziwen.webmonitor.stats.bean.ElementRing;
import io.github.yangziwen.webmonitor.stats.bean.NginxAccess;
import io.github.yangziwen.webmonitor.stats.bean.UrlMetrics;

public class UrlMetricsManager {

    private static final int RING_CAPACITY = 2048;

    private static ConcurrentHashMap<String, ElementRing<NginxAccess>> URL_RING_MAP = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, UrlMetrics> URL_METRICS_MAP = new ConcurrentHashMap<>();

    public static void doStats(NginxAccess access) {
        String pattern = parseUrlPattern(access.getUrl());
        ensureRing(pattern).add(access);
        ensureMetrics(pattern).doStats(access);
    }

    private static String parseUrlPattern(String url) {
        // TODO
        return null;
    }

    private static ElementRing<NginxAccess> ensureRing(String urlPattern) {
        if (!URL_RING_MAP.containsKey(urlPattern)) {
            URL_RING_MAP.putIfAbsent(urlPattern, new ElementRing<>(RING_CAPACITY, NginxAccess.class));
        }
        return URL_RING_MAP.get(urlPattern);
    }

    private static UrlMetrics ensureMetrics(String urlPattern) {
        if (!URL_METRICS_MAP.containsKey(urlPattern)) {
            URL_METRICS_MAP.putIfAbsent(urlPattern, new UrlMetrics(urlPattern));
        }
        return URL_METRICS_MAP.get(urlPattern);
    }

    // TODO 通过定时任务，将旧数据归档

}
