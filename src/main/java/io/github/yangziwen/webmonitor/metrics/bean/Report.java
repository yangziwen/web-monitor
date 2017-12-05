package io.github.yangziwen.webmonitor.metrics.bean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import lombok.Getter;

@Getter
public class Report {

    private static final int SLOW_TIME_THRESHOLD = 1500;

    private static final double WARNING_RATIO_THRESHOLD = 0.3D;

    private Status status;

    private List<Detail> details = new ArrayList<>();

    public Report(List<UrlMetrics> metricsList) {
        details = metricsList.stream().map(Detail::new)
                .filter(detail -> Status.OK != detail.getStatus())
                .sorted(Comparator.comparing(Detail::getErrorRatio).reversed()
                        .thenComparing(Detail::getSlowRatio).reversed())
                .collect(Collectors.toList());
        status = CollectionUtils.isEmpty(details)
                ? Status.OK : Status.WARNING;
    }

    @Getter
    public static class Detail {

        private Status status;

        private String url;

        private Long cnt;

        private Double errorRatio;

        private Double slowRatio;

        public Detail(UrlMetrics metrics) {

            this.url = metrics.getUrlPattern();
            this.cnt = metrics.getCnt();
            this.errorRatio = metrics.getErrorCnt() * 1D / metrics.getCnt();
            this.slowRatio = metrics.getDistribution().getSlowCnt(SLOW_TIME_THRESHOLD) * 1D / metrics.getCnt();

            if (errorRatio > WARNING_RATIO_THRESHOLD) {
                status = Status.ERROR_WARNING;
            }
            else if (slowRatio > WARNING_RATIO_THRESHOLD) {
                status = Status.SLOW_WARNING;
            }
            else {
                status = Status.OK;
            }
        }

    }

    public static enum Status {

        OK, WARNING, SLOW_WARNING, ERROR_WARNING;

    }

}
