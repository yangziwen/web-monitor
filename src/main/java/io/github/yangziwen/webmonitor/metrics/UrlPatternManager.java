package io.github.yangziwen.webmonitor.metrics;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

import io.github.yangziwen.webmonitor.service.MonitorService;

public class UrlPatternManager {

    private static final Logger logger = LoggerFactory.getLogger(UrlPatternManager.class);

    private static final String PATTERN_UNKNOWN = "unknown";

    private static final PathMatcher URL_PATH_MATCHER = new AntPathMatcher();

    // 不包含模糊匹配模式的url
    private static Set<String> SIMPLE_URL_PATTERNS = Collections.emptySet();

    // 第一节路径中包含模糊匹配模式的url
    private static Set<String> COMPLICATED_URL_PATTERNS = Collections.emptySet();

    // 第一节路径中不包含模块匹配模式，但后续路径中包括模糊匹配模式的url
    private static Multimap<String, String> PREFIX_KEYED_URL_MAP = ImmutableSetMultimap.of();

    static {
        MonitorService.reloadUrlPatterns();
        logger.info("loaded {} url patterns", getLoadedUrlPatternCount());
    }

    private UrlPatternManager() {}

    public static void reloadUrlPatterns(Collection<String> urlPatterns) {
        Multimap<String, String> prefixKeyedUrlMap = HashMultimap.create();
        Set<String> simpleUrlPatterns = new HashSet<>();
        Set<String> complicatedUrlPatterns = new HashSet<>();
        for (String pattern : urlPatterns) {
            if (!pattern.contains("*") && !pattern.contains("{")) {
                simpleUrlPatterns.add(pattern);
                continue;
            }
            String[] array = StringUtils.split(pattern, "/");
            String prefix = array[0];
            if (prefix.contains("*") || prefix.contains("{")) {
                complicatedUrlPatterns.add(pattern);
                continue;
            }
            prefixKeyedUrlMap.put(prefix, pattern);
        }
        SIMPLE_URL_PATTERNS = Collections.unmodifiableSet(simpleUrlPatterns);
        COMPLICATED_URL_PATTERNS = Collections.unmodifiableSet(complicatedUrlPatterns);
        PREFIX_KEYED_URL_MAP = ImmutableSetMultimap.copyOf(prefixKeyedUrlMap);
    }

    public static String getBestMatchedUrlPattern(String url) {

        if (StringUtils.isEmpty(url)) {
            return PATTERN_UNKNOWN;
        }

        int paramStartIdx = url.indexOf("?");

        String path = paramStartIdx >= 0 ? url.substring(0, paramStartIdx) : url;

        if (StringUtils.isEmpty(path)) {
            return PATTERN_UNKNOWN;
        }

        if (SIMPLE_URL_PATTERNS.contains(path)) {
            return path;
        }

        String prefix = StringUtils.split(path, "/")[0];

        Collection<String> urlPatterns = Collections.emptySet();
        if (PREFIX_KEYED_URL_MAP.containsKey(prefix)) {
            urlPatterns = PREFIX_KEYED_URL_MAP.get(prefix);
        }
        else if (prefix.contains("*") || prefix.contains("{")) {
            urlPatterns = COMPLICATED_URL_PATTERNS;
        }

        return urlPatterns.stream()
                .filter(pattern -> URL_PATH_MATCHER.match(pattern, path))
                .sorted(URL_PATH_MATCHER.getPatternComparator(path))
                .findFirst()
                .orElse(PATTERN_UNKNOWN);

    }

    public static int getLoadedUrlPatternCount() {
        return SIMPLE_URL_PATTERNS.size()
                + COMPLICATED_URL_PATTERNS.size()
                + PREFIX_KEYED_URL_MAP.values().size();
    }


}
