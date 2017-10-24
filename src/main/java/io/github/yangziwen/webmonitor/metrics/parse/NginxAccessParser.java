package io.github.yangziwen.webmonitor.metrics.parse;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import io.github.yangziwen.webmonitor.metrics.bean.NginxAccess;

public class NginxAccessParser extends Parser {

    public NginxAccessParser() {
        this.next(new IpParser())
            .next(new UserParser())
            .next(new TimestampParser())
            .next(new RequestMethodParser())
            .next(new UrlParser())
            .next(new ResponseStatusParser())
            .next(new ResponseTimeParser());
    }

    @Override
    protected Parser doParse(String content, int offset) {
        return this;
    }

    public NginxAccess toNginxAccess() {
        NginxAccess access = new NginxAccess();
        access.setUrl(result(UrlParser.class));
        access.setTimestamp(NginxAccess.parseTimestampToDate(result(TimestampParser.class)).getTime());
        access.setCode(NumberUtils.toInt(result(ResponseStatusParser.class)));
        access.setMethod(result(RequestMethodParser.class));
        Double responseTime = NumberUtils.toDouble(result(ResponseTimeParser.class)) * 1000;
        access.setResponseTime(responseTime.intValue());
        access.setBackendUrl(NginxAccess.extractBackendUrl(access.getUrl()));
        return access;
    }

    static class IpParser extends Parser {
        private static final Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}(?:\\.\\d{1,3}){3}$");
        @Override
        protected Parser doParse(String content, int offset) {
            return start(offset)
                    .end(content.indexOf(" ", offset))
                    .result(content, start(), end())
                    .checkAndReturn(IP_PATTERN.matcher(result()).matches());
        }
    }

    static class UserParser extends Parser {
        @Override
        protected Parser doParse(String content, int offset) {
            return start(content.indexOf("-", offset) + 2)
                    .end(content.indexOf("-", start()) - 1)
                    .result(content, start(), end());
        }
    }

    static class TimestampParser extends Parser {
        private static final Pattern TS_PATTERN = Pattern.compile("^\\d{1,2}/\\w{3}/\\d{4}(\\:\\d{2}){3}");
        @Override
        protected Parser doParse(String content, int offset) {
            return start(content.indexOf("[", offset) + 1)
                    .end(content.indexOf("]", start()))
                    .result(content, start(), end())
                    .checkAndReturn(TS_PATTERN.matcher(result()).find());
        }
    }

    static class RequestMethodParser extends Parser {
        private static final List<String> METHODS = Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "TRACE", "OPTIONS");
        @Override
        protected Parser doParse(String content, int offset) {
            return start(content.indexOf("\"", offset) + 1)
                    .end(content.indexOf(" ", start()))
                    .result(content, start(), end())
                    .checkAndReturn(METHODS.contains(result()));
        }
    }

    static class UrlParser extends Parser {
        @Override
        protected Parser doParse(String content, int offset) {
            return start(content.indexOf("/", offset))
                    .end(searchEnd(content, offset))
                    .result(content, start(), end())
                    .checkAndReturn(end() > start());
        }
        private int searchEnd(String content, int offset) {
            for (String protocol : ProtocolParser.PROTOCOLS) {
                int pos = content.indexOf(" " + protocol + "\"", offset);
                if (pos >= 0) {
                    return pos;
                }
            }
            return -1;
        }
    }

    static class ProtocolParser extends Parser {
        private static final List<String> PROTOCOLS = Arrays.asList("HTTP/1.1", "HTTP/1.0", "HTTP/0.9");
        @Override
        protected Parser doParse(String content, int offset) {
            String protocol = findProtocol(content, offset);
            if (StringUtils.isBlank(protocol)) {
                return VOID;
            }
            return start(content.indexOf(protocol, offset))
                    .end(start() + protocol.length())
                    .result(protocol);
        }
        private String findProtocol(String content, int offset) {
            String substring = content.substring(offset).trim();
            for (String protocol : PROTOCOLS) {
                if (substring.startsWith(protocol)) {
                    return protocol;
                }
            }
            return null;
        }

    }

    static class ResponseStatusParser extends Parser {
        private static final Pattern STATUS_PATTERN = Pattern.compile("\\d{3}");
        @Override
        protected Parser doParse(String content, int offset) {
            Matcher matcher = STATUS_PATTERN.matcher(content);
            if (!matcher.find(offset)) {
                return VOID;
            }
            return start(matcher.start())
                    .end(matcher.end())
                    .result(content.substring(start(), end()));
        }

    }

    static class ResponseTimeParser extends Parser {
        @Override
        protected Parser doParse(String content, int offset) {
            String[] arrays = content.substring(offset).split("\\s+");
            String time = arrays[arrays.length - 1];
            return start(content.lastIndexOf(time))
                    .end(content.length())
                    .result(content, start(), end());
        }

    }

}
