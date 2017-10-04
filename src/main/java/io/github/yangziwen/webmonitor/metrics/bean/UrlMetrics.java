package io.github.yangziwen.webmonitor.metrics.bean;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UrlMetrics extends Metrics {

    private String urlPattern;

    private AtomicInteger errorCnt = new AtomicInteger(0);

    public UrlMetrics(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public Integer getErrorCnt() {
        return errorCnt.get();
    }

    public void doStats(NginxAccess access) {
        super.doStats(access.getResponseTime());
        if (access.getCode() >= 500) {
            errorCnt.incrementAndGet();
        }
    }

    public UrlMetrics merge(UrlMetrics other) {
        if (other == null) {
            return this;
        }
        super.merge(other);
        errorCnt.addAndGet(other.getErrorCnt());
        return this;
    }

    public static UrlMetrics fromLatestRingElements(
            String urlPattern,
            ElementRing<NginxAccess> ring,
            int n) {
        UrlMetrics metrics = new UrlMetrics(urlPattern);
        if (ring == null) {
            return metrics;
        }
        List<NginxAccess> list = ring.latest(n);
        for (NginxAccess access : list) {
            metrics.doStats(access);
        }
        return metrics;
    }

}
