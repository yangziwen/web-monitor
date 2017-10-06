package io.github.yangziwen.webmonitor.metrics.bean;

import java.util.Date;

import io.github.yangziwen.webmonitor.model.UrlMetricsResult;

public class PeriodUrlMetrics extends UrlMetrics {

    public PeriodUrlMetrics(UrlMetricsResult result) {
        super(result.getUrl());
        setBeginTime(result.getBeginTime());
        setEndTime(result.getEndTime());
        merge(result);
    }

    private Date beginTime;

    private Date endTime;

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }


}
