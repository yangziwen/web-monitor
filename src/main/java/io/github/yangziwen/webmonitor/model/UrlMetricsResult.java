package io.github.yangziwen.webmonitor.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import io.github.yangziwen.webmonitor.metrics.bean.UrlMetrics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    public static UrlMetricsResult from(UrlMetrics metrics, Date beginTime, Date endTime) {
        if (metrics == null) {
            return null;
        }
        return UrlMetricsResult.builder()
            .url(metrics.getUrlPattern())
            .beginTime(beginTime)
            .endTime(endTime)
            .cnt(metrics.getCnt())
            .errorCnt(metrics.getErrorCnt())
            .sum(metrics.getSum())
            .max(metrics.getMax())
            .min(metrics.getMin())
            .distribution(metrics.getDistribution().toString())
            .build();
    }


}
