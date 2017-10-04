package io.github.yangziwen.webmonitor.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

public class MultiPathMatcher extends AntPathMatcher {

    private static final int CACHE_TURNOFF_THRESHOLD = 65536;

    private Map<String, MultiPathStringMatcher> multiPathStringMatcherCache = new ConcurrentHashMap<
            String, MultiPathStringMatcher>(256);

    @Override
    protected boolean doMatch(String pattern, String path,
            boolean fullMatch, Map<String, String> uriTemplateVariables) {
        boolean match = super.doMatch(pattern, path, fullMatch,
                uriTemplateVariables);
        if (!StringUtils.contains(pattern, "{**")) {
            return match;
        }
        if (true == match) {
            stripKeyFlag(uriTemplateVariables, "**");
        } else {
            return getMultiPathStringMatcher(pattern).matchStrings(path,
                    uriTemplateVariables);
        }
        return match;
    }

    private void stripKeyFlag(Map<String, String> map, String flag) {
        if (MapUtils.isEmpty(map)) {
            return;
        }
        Set<Entry<String, String>> keySet = new HashSet<>();
        for (Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().contains(flag)) {
                keySet.add(entry);
            }
        }
        for (Entry<String, String> entry : keySet) {
            map.put(entry.getKey().replace(flag, ""), entry.getValue());
            map.remove(entry.getKey());
        }
    }

    private MultiPathStringMatcher getMultiPathStringMatcher(String pattern) {
        if (multiPathStringMatcherCache.size() > CACHE_TURNOFF_THRESHOLD) {
            multiPathStringMatcherCache.clear();
        }

        MultiPathStringMatcher matcher = multiPathStringMatcherCache
                .get(pattern);
        if (matcher == null) {
            matcher = new MultiPathStringMatcher(pattern);
            multiPathStringMatcherCache.put(pattern, matcher);
        }
        return matcher;
    }

    public static class MultiPathStringMatcher {

        /**
         * 匹配路径的正则表达式
         */
        private static final Pattern GLOB_PATTERN = Pattern
                .compile("\\?|\\*{1,2}|\\{((?:\\{[^/]+?\\}|[^/{}]|\\\\[{}])+?)\\}");

        private static final String SINGLE_DIR_PATTERN = "[^/]*?";
        private static final String MULTI_DIR_PATTERN = ".*?";

        private final Pattern pattern;

        private final List<String> variableNames = new LinkedList<String>();

        public MultiPathStringMatcher(String pattern) {
            StringBuilder patternBuilder = new StringBuilder();
            if (pattern.endsWith("}.*")) {
                pattern = pattern.substring(0, pattern.length() - 2);
            }
            Matcher m = GLOB_PATTERN.matcher(pattern);
            int end = 0;
            while (m.find()) {
                patternBuilder.append(quote(pattern, end, m.start()));
                String match = m.group();
                if ("?".equals(match)) {
                    patternBuilder.append('.');
                } else if ("*".equals(match)) {
                    patternBuilder.append(SINGLE_DIR_PATTERN);
                } else if ("**".equals(match)) {
                    patternBuilder.append(MULTI_DIR_PATTERN);
                } else if (match.startsWith("{") && match.endsWith("}")) {
                    int colonIdx = match.indexOf(':');
                    String variableName = null;
                    String variablePattern = null;
                    patternBuilder.append("(?<");
                    if (colonIdx == -1) {
                        variableName = m.group(1);
                        variablePattern = MULTI_DIR_PATTERN;
                    } else {
                        variableName = match.substring(1, colonIdx);
                        variablePattern = match.substring(colonIdx + 1,
                                match.length() - 1);
                    }
                    if (variableName.startsWith("**")) {
                        variableName = variableName.substring(2,
                                variableName.length());
                    }
                    patternBuilder.append(variableName).append('>');
                    patternBuilder.append(variablePattern);
                    patternBuilder.append(')');
                    this.variableNames.add(variableName);
                }
                end = m.end();
            }
            patternBuilder.append(quote(pattern, end, pattern.length()));
            this.pattern = Pattern.compile(patternBuilder.toString());
        }

        private String quote(String s, int start, int end) {
            if (start == end) {
                return "";
            }
            return Pattern.quote(s.substring(start, end));
        }

        public boolean matchStrings(String path,
                Map<String, String> uriTemplateVariables) {
            Matcher matcher = this.pattern.matcher(path);
            if (matcher.matches()) {
                if (uriTemplateVariables != null) {
                    for (String name : variableNames) {
                        String value = matcher.group(name);
                        uriTemplateVariables.put(name, value);
                    }
                }
                return true;
            }
            return false;
        }

    }

}