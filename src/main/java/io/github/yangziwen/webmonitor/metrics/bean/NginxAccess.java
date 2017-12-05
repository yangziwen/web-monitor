package io.github.yangziwen.webmonitor.metrics.bean;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import io.github.yangziwen.webmonitor.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NginxAccess {

    private static final String NGINX_TIMESTAMP_PATTERN = "dd/MMM/yyyy:HH:mm:ss Z";

    private static final List<String> NGINX_URL_PREFIX_LIST = Lists.newArrayList(
            "/rest/git",
            "/rest/review",
            "/rest/files",
            "/rest/search",
            "/rest/user",
            "/git",
            "/review");

    private String url;

    private String backendUrl;

    private String method;

    private String referrer;

    private int code;

    private long timestamp;

    private int responseTime;

    private String upstream;

    public static NginxAccess fromJsonObject(JSONObject accessObj) {
        if (accessObj == null) {
            return null;
        }
        try {
            String url = accessObj.getString("url");
            return NginxAccess.builder()
                .url(url)
                .method(accessObj.getString("method"))
                .code(NumberUtils.toInt(accessObj.getString("response_code")))
                .upstream(accessObj.getString("upstream"))
                .timestamp(parseTimestampToDate(accessObj.getString("timestamp")).getTime())
                .responseTime(new Double(NumberUtils.toDouble(accessObj.getString("response_time")) * 1000).intValue())
                .referrer(accessObj.getString("referrer"))
                .backendUrl(extractBackendUrl(url))
                .build();
        } catch (Exception e) {
            return null;
        }
    }

    public static String extractBackendUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return url;
        }
        for (String prefix : NGINX_URL_PREFIX_LIST) {
            if (url.startsWith(prefix)) {
                return StringUtils.replaceOnce(url, prefix, "");
            }
        }
        return url;
    }

    public static Date parseTimestampToDate(String timestamp) {
        return DateUtil.parseDateQuietly(timestamp, Locale.US, NGINX_TIMESTAMP_PATTERN);
    }

}
