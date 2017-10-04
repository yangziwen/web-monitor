package io.github.yangziwen.webmonitor.metrics.bean;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import io.github.yangziwen.webmonitor.util.DateUtil;

public class NginxAccess {

    private static final String NGINX_TIMESTAMP_PATTERN = "dd/MMM/yyyy:HH:mm:ss Z";

    private static final List<String> NGINX_URL_PREFIX_LIST = Lists.newArrayList(
            "/rest/git",
            "/rest/review",
            "/rest/files",
            "/rest/search",
            "/rest/user");

    private String url;

    private String backendUrl;

    private String method;

    private String referrer;

    private int code;

    private long timestamp;

    private int responseTime;

    private String upstream;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public void setBackendUrl(String backendUrl) {
        this.backendUrl = backendUrl;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public String getUpstream() {
        return upstream;
    }

    public void setUpstream(String upstream) {
        this.upstream = upstream;
    }

    public static NginxAccess fromJsonObject(JSONObject accessObj) {
        if (accessObj == null) {
            return null;
        }
        try {
            NginxAccess access = new NginxAccess();
            access.setUrl(accessObj.getString("url"));
            access.setMethod(accessObj.getString("method"));
            access.setCode(NumberUtils.toInt(accessObj.getString("response_code")));
            access.setUpstream(accessObj.getString("upstream"));
            access.setTimestamp(DateUtil.parseDateQuietly(accessObj.getString("timestamp"), Locale.US, NGINX_TIMESTAMP_PATTERN).getTime());
            access.setResponseTime(new Double(NumberUtils.toDouble(accessObj.getString("response_time")) * 1000).intValue());
            access.setReferrer(accessObj.getString("referrer"));
            for (String prefix : NGINX_URL_PREFIX_LIST) {
                if (access.getUrl().startsWith(prefix)) {
                    access.setBackendUrl(StringUtils.replaceOnce(access.getUrl(), prefix, ""));
                    break;
                }
            }
            if (StringUtils.isBlank(access.getBackendUrl())) {
                access.setBackendUrl(access.getUrl());
            }
            return access;
        } catch (Exception e) {
            return null;
        }
    }

}
