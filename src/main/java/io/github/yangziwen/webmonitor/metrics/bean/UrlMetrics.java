package io.github.yangziwen.webmonitor.metrics.bean;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import io.github.yangziwen.webmonitor.model.UrlMetricsResult;

public class UrlMetrics extends Metrics {

    private String urlPattern;

    private String project;

    private AtomicLong errorCnt = new AtomicLong(0L);

    public UrlMetrics(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public UrlMetrics(String urlPattern, String project) {
        this.urlPattern = urlPattern;
        this.project = project;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public long getErrorCnt() {
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

    public UrlMetrics merge(UrlMetricsResult other) {
        if (other == null) {
            return this;
        }
        cnt.addAndGet(other.getCnt());
        errorCnt.addAndGet(other.getErrorCnt());
        sum.addAndGet(other.getSum());
        int thisMax, otherMax;
        while ((otherMax = other.getMax()) > (thisMax = this.getMax())) {
            if (max.compareAndSet(thisMax, otherMax)) {
                break;
            }
        }
        int thisMin, otherMin;
        while ((otherMin = other.getMin()) < (thisMin = this.getMin()) || thisMin <= 0) {
            if (min.compareAndSet(thisMin, otherMin)) {
                break;
            }
        }
        this.distribution.merge(other.getDistribution());
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
