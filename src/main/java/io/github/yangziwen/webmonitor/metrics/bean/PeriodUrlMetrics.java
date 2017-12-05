package io.github.yangziwen.webmonitor.metrics.bean;

import java.util.Date;

import io.github.yangziwen.webmonitor.model.UrlMetricsResult;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PeriodUrlMetrics extends UrlMetrics {

    public PeriodUrlMetrics(UrlMetricsResult result) {
        super(result.getUrl());
        setBeginTime(result.getBeginTime());
        setEndTime(result.getEndTime());
        merge(result);
    }

    private Date beginTime;

    private Date endTime;

}
