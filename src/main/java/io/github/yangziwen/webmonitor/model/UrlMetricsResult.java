package io.github.yangziwen.webmonitor.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import io.github.yangziwen.webmonitor.metrics.bean.UrlMetrics;

@Table(name = "url_metrics_result")
public class UrlMetricsResult {

    @Id
    @Column
    private Long id;

    @Column
    private String url;

    @Column
    private Date beginTime;

    @Column
    private Date endTime;

    @Column
    private Long cnt;

    @Column
    private Long errorCnt;

    @Column
    private Long sum;

    @Column
    private Integer max;

    @Column
    private Integer min;

    @Column
    private String distribution;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

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

    public Long getCnt() {
        return cnt;
    }

    public void setCnt(Long cnt) {
        this.cnt = cnt;
    }

    public Long getErrorCnt() {
        return errorCnt;
    }

    public void setErrorCnt(Long errorCnt) {
        this.errorCnt = errorCnt;
    }

    public Long getSum() {
        return sum;
    }

    public void setSum(Long sum) {
        this.sum = sum;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public static UrlMetricsResult from(UrlMetrics metrics, Date beginTime, Date endTime) {
        if (metrics == null) {
            return null;
        }
        UrlMetricsResult result = new UrlMetricsResult();
        result.setUrl(metrics.getUrlPattern());
        result.setBeginTime(beginTime);
        result.setEndTime(endTime);
        result.setCnt(metrics.getCnt());
        result.setErrorCnt(metrics.getErrorCnt());
        result.setSum(metrics.getSum());
        result.setMax(metrics.getMax());
        result.setMin(metrics.getMin());
        result.setDistribution(metrics.getDistribution().toString());
        return result;
    }


}
