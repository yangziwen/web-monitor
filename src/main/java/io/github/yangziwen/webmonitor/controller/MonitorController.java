package io.github.yangziwen.webmonitor.controller;

import static io.github.yangziwen.webmonitor.controller.ResultEnum.BAD_REQUEST;
import static io.github.yangziwen.webmonitor.controller.ResultEnum.OK;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.github.yangziwen.webmonitor.metrics.UrlMetricsManager;
import io.github.yangziwen.webmonitor.metrics.UrlPatternManager;
import io.github.yangziwen.webmonitor.metrics.bean.NginxAccess;
import io.github.yangziwen.webmonitor.metrics.bean.UrlMetrics;
import io.github.yangziwen.webmonitor.service.MonitorService;
import spark.Spark;

public class MonitorController extends BaseController {

    private static final String DEFAULT_METRICS_SORT = "url_asc";

    public static void init() {

        // 从数据库加载全部url并装填到内存中
        Spark.get("/monitor/url/reload.json", (request, response) -> {
            response.type(CONTENT_TYPE_JSON);
            MonitorService.reloadUrlPatterns();
            return OK.newResult()
                    .message("loaded %d url patterns", UrlPatternManager.getLoadedUrlPatternCount());
        }, JSON::toJSONString);

        // 接收logstash发送日志的接口
        Spark.post("/monitor/nginx/access.json", (request, response) -> {
            response.type(CONTENT_TYPE_JSON);
            JSONObject accessObj = JSON.parseObject(request.body())
                    .getJSONObject("nginx")
                    .getJSONObject("access");
            NginxAccess access = NginxAccess.fromJsonObject(accessObj);
            if (access == null) {
                return BAD_REQUEST.newResult();
            }
            UrlMetricsManager.doStats(access);
            return OK.newResult();
        }, JSON::toJSONString);

        // 获取每个接口最近n次请求的统计结果
        Spark.get("/monitor/metrics/latest/list.json", (request, response) -> {
            response.type(CONTENT_TYPE_JSON);
            int n = NumberUtils.toInt(request.queryParams("n"), 64);
            String sort = request.queryParamOrDefault("sort", DEFAULT_METRICS_SORT);
            List<UrlMetrics> list = UrlMetricsManager.getLatestUrlMectricsList(n);
            list.sort(createComparator(sort));
            return OK.newResult().data(list);
        }, JSON::toJSONString);

    }

    public static Comparator<UrlMetrics> createComparator(String sort) {
        if (StringUtils.isBlank(sort)) {
            sort = DEFAULT_METRICS_SORT;
        }
        String[] array = StringUtils.split(sort, "_");
        if (array.length < 2) {
            array = new String[]{ array[0], "asc" };
        }
        Comparator<UrlMetrics> comparator;
        if ("avg".equals(array[0])) {
            comparator = Comparator.comparing(UrlMetrics::getAvg);
        }
        else if ("max".equals(array[0])) {
            comparator = Comparator.comparing(UrlMetrics::getMax);
        }
        else if ("min".equals(array[0])) {
            comparator = Comparator.comparing(UrlMetrics::getMin);
        }
        else if ("cnt".equals(array[0])) {
            comparator = Comparator.comparing(UrlMetrics::getCnt);
        }
        else if ("error".equals(array[0])) {
            comparator = Comparator.comparing(UrlMetrics::getErrorCnt);
        }
        else {
            comparator = Comparator.comparing(UrlMetrics::getUrlPattern);
        }
        if ("desc".equals(array[1])) {
            comparator = comparator.reversed();
        }
        return comparator;
    }
}
