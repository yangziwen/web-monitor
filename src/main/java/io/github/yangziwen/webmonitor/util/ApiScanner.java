/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */

package io.github.yangziwen.webmonitor.util;

import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.ARTIFACT_ID;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.CONSUMES;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.CONTROLLER_ANNOTATIONS;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.DEFAULT_VALUE;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.GROUP_ID;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.HEADERS;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.METHOD;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.NAME;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.PARENT;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.PATH;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.PRODUCES;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.REQUIRED;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.SPRING_MVC_SUPPORTED_ARG_TYPES;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.VALUE;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Constants.VERSION;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Patterns.ACL_ITEM_ROLE_PATTERN;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Patterns.ACL_ITEM_TARGET_PATTERN;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Patterns.ACL_PATTERN;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Patterns.API_ACCESS_PATTERN;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Patterns.CLASS_PATTERN;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Patterns.JAVA_DOC_PATTERN;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Patterns.METHOD_PATTERN;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Patterns.PACKAGE_PATTERN;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Patterns.REQUEST_MAPPING_CONFIG_PATTERN;
import static io.github.yangziwen.webmonitor.util.ApiScanner.Patterns.REQUEST_MAPPING_PATTERN;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.strip;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import io.github.yangziwen.webmonitor.util.ApiScanner.Constants.JavaDoc;

/**
 * ApiScanner
 *
 * @author Yang Ziwen(yangziwen@baidu.com)
 */
public class ApiScanner {

    private static final Logger logger = LoggerFactory.getLogger(ApiScanner.class);

    static interface Patterns {

        Pattern PACKAGE_PATTERN = Pattern.compile("package (.+);");

        Pattern CLASS_PATTERN = Pattern.compile("\\s+class\\s+([A-Z][\\w]*)\\s*(?:extends .*?)?(?:implements .*?)?\\{[\\w\\W]*\\}\\s*$");

        Pattern REQUEST_MAPPING_PATTERN = Pattern.compile("@RequestMapping\\((.+?)\\)");

        Pattern REQUEST_MAPPING_CONFIG_PATTERN = Pattern.compile("(\\w+)\\s*=\\s*(.+?)(?=,\\s*\\w+\\s*=)");

        Pattern METHOD_PATTERN = Pattern.compile("(?:(?:public)|(?:private)|(?:protected)\\s+)([\\w_<>, \\?]*)\\s+(\\w[\\w_]*)\\s*\\(([^\\{]*)\\)\\s*(?:throws .*?)?\\{");

        Pattern ACL_PATTERN = Pattern.compile("@Acl\\(\\{([\\w\\W]+?)\\}\\)");

        Pattern ACL_ITEM_ROLE_PATTERN = Pattern.compile("roles\\s*=\\s*\\{([^\\}]+?)\\}");

        Pattern ACL_ITEM_TARGET_PATTERN = Pattern.compile("convert\\s*=\\s*\".+?\\((.+?)\\)\"");

        Pattern API_ACCESS_PATTERN = Pattern.compile("@ApiAccess(?:\\(\\s*userType\\s*=\\s*(.+?)\\s*\\))?");

        Pattern JAVA_DOC_PATTERN = Pattern.compile("/\\*\\*[\\w\\W]+?\\*/");
    }

    static interface Constants {

        String NAME = "name";

        String VALUE = "value";

        String PATH = "path";

        String REQUIRED = "required";

        String DEFAULT_VALUE = "defaultValue";

        String METHOD = "method";

        String HEADERS = "headers";

        String PRODUCES = "produces";

        String CONSUMES = "consumes";

        String GROUP_ID = "groupId";

        String ARTIFACT_ID = "artifactId";

        String VERSION = "version";

        String PARENT = "parent";

        String[] CONTROLLER_ANNOTATIONS = { "@Controller", "@RestController" };

        String[] SPRING_MVC_SUPPORTED_ARG_TYPES = {
                "ServletRequest",
                "ServletResponse",
                "WebRequest",
                "HttpSession",
                "Locale",
                "InputStream",
                "OutputStream",
                "Reader",
                "Writer",
                "HttpMethod",
                "Principal",
                "HttpEntity",
                "ModelMap",
                "ModelAttribute"
        };

        static interface JavaDoc {

            String DESCRIPTION = "__description__";

            String PARAM = "__param__";

            String DEPRECATED = "__DEPRECATED__";

            String AUTHOR = "__author__";

        }
    }

    /**
     * 按文件树递归扫描文件，提取web接口信息列表
     * @param file
     * @param filters
     * @return
     */
    public static List<Result> scan(File file, FileFilter...filters) {
        if (file == null) {
            logger.error("file is null!");
            return emptyList();
        }
        if (!file.canRead()) {
            logger.warn("file[{}] is not readable!", normalizePath(file));
            return emptyList();
        }
        if (file.isFile()) {
            if (!file.getName().endsWith(".java")) {
//                logger.warn("file[{}] is not a java source file!", normalizePath(file));
                return emptyList();
            }
            return scan(readFileToString(file));
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            File pomFile = Arrays.stream(files)
                    .filter(f -> f.getName().equals("pom.xml"))
                    .findFirst()
                    .orElse(null);
            Project project = Project.fromPom(pomFile);
            if (filters != null) {
                Predicate<File> predicate = f -> Arrays.stream(filters)
                        .allMatch(filter -> filter.accept(f));
                files = Arrays.stream(files)
                        .filter(predicate)
                        .collect(Collectors.toList())
                        .toArray(new File[0]);
            }
            return Arrays.stream(files)
                    .flatMap(f -> scan(f, filters).stream())
                    .collect(collectingAndThen(toList(), list -> {
                        if (project != null) {
                            list.stream()
                                .filter(r -> r.getProject() == null)
                                .forEach(r -> r.setProject(project));
                        }
                        return list;
                    }));
        }
        return emptyList();
    }


    /**
     * 按内容提取web接口u信息列表
     * @param content
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Result> scan(String content) {
        if (!isController(content)) {
            return emptyList();
        }

        Matcher packageMatcher = PACKAGE_PATTERN.matcher(content);
        if (!packageMatcher.find()) {
            return emptyList();
        }
        String packageName = packageMatcher.group(1);

        Matcher classMatcher = CLASS_PATTERN.matcher(content);
        if (!classMatcher.find()) {
            return null;
        }
        String className = classMatcher.group(1);

        List<Result> resultList = new ArrayList<Result>();

        Matcher requestMappingMatcher = REQUEST_MAPPING_PATTERN.matcher(content);
        Matcher methodMatcher = METHOD_PATTERN.matcher(content);
        Map<String, List<String>> baseConfigs = new LinkedHashMap<>();
        baseConfigs.put(PATH, Lists.newArrayList(""));
        int lastMethodEnd = classMatcher.start();

        while (requestMappingMatcher.find()) {
            try {

            if (requestMappingMatcher.start() < classMatcher.start()) {
                baseConfigs = parseRequestMappingConfigs(requestMappingMatcher.group(1));
                continue;
            }

            Map<String, List<String>> configs = parseRequestMappingConfigs(requestMappingMatcher.group(1));

            while (methodMatcher.find()) {
                if (methodMatcher.start() > requestMappingMatcher.end()) {
                    break;
                }
                lastMethodEnd = methodMatcher.end();
            }

            Result result = new Result();
            result.setClassName(packageName + "." + className);
            result.setReturnType(methodMatcher.group(1).trim());
            result.setMethodName(methodMatcher.group(2).trim());
            result.setRequestMethods(firstAvailable(configs.get(METHOD), baseConfigs.get(METHOD)));
            result.setProduces(firstAvailable(configs.get(PRODUCES), baseConfigs.get(PRODUCES)));
            result.setConsumes(firstAvailable(configs.get(CONSUMES), baseConfigs.get(CONSUMES)));
            result.setHeaders(firstAvailable(configs.get(HEADERS), baseConfigs.get(HEADERS)));
            result.setParameters(parseParameters(methodMatcher.group(3)));

            String aboveContent = content.substring(lastMethodEnd, methodMatcher.start());
            result.setAcl(parseAuthorities(aboveContent));
            result.setApiAccessUserType(parseApiAccessUserType(aboveContent));
            result.setDeprecated(parseDeprecated(aboveContent));

            Map<String, String> docMap = parseJavaDoc(aboveContent);
            result.setDescription(docMap.get(JavaDoc.DESCRIPTION));
            for (Parameter param : result.getParameters()) {
                param.setDescription(docMap.get(JavaDoc.PARAM + param.getName()));
            }
            if (docMap.containsKey(JavaDoc.DEPRECATED)) {
                result.setDeprecated(true);
            }
            String authors = docMap.get(JavaDoc.AUTHOR);
            result.setAuthors(StringUtils.isNotBlank(authors)
                    ? Splitter.on(",").splitToList(authors) : Collections.emptyList());

            for (String base : baseConfigs.get(PATH)) {
                for (String path : configs.get(PATH)) {
                    String requestUrl = (base + "/" + path)
                            .replaceAll("/+", "/")
                            .replaceAll("\\\\{2}", "\\\\");
                    Result r = result.clone();
                    r.setRequestUrl(requestUrl);
                    resultList.add(r);
                }
            }

            lastMethodEnd = methodMatcher.end();
            } catch (Exception e) {
            }
        }

        return resultList;
    }

    @SuppressWarnings("unchecked")
    private static <V> V firstAvailable(V... values) {
        if (values == null) {
            return null;
        }
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static boolean isController(String content) {
        return Arrays.stream(CONTROLLER_ANNOTATIONS)
                .anyMatch(content::contains);
    }

    private static Map<String, List<String>> parseRequestMappingConfigs(String content) {
        Map<String, List<String>> configs = new LinkedHashMap<String, List<String>>();
        content = content.trim();
        if (!content.contains(",") && !content.contains("=")) {
            content = PATH + " = " + content;
        }
        content += ",v=0";
        Matcher matcher = REQUEST_MAPPING_CONFIG_PATTERN.matcher(content);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            if (VALUE.equals(key)) {
                key = PATH;
            }
            configs.put(key, normalizeConfigValue(value));
        }
        if (configs.get(PATH) == null) {
            configs.put(PATH, Lists.newArrayList(""));
        }
        return configs;
    }

    private static boolean parseDeprecated(String content) {
        return content.contains("@Deprecated");
    }

    private static Map<String, String> parseJavaDoc(String content) {
        Matcher matcher = JAVA_DOC_PATTERN.matcher(content);
        String doc = null;
        while (matcher.find()) {
            doc = matcher.group();
        }
        if (isBlank(doc)) {
            return Collections.emptyMap();
        }
        Map<String, String> docMap = new HashMap<>();
        StringBuilder descBuff = new StringBuilder();
        String[] lines = doc.split("\r|\n");
        for (String line : lines) {
            line = line.trim()
                    .replaceAll("\r|\n$", "")
                    .replaceAll("^\\*+/", "")
                    .replaceAll("^/?\\*+", "")
                    .trim();
            if (isBlank(line)) {
                continue;
            }
            if (!line.startsWith("@")) {
                descBuff.append(line).append("\n");
                continue;
            }
            if (line.startsWith("@param")) {
                line = line.replaceAll("^@param", "").trim();
                String[] array = line.split("\\s+", 2);
                if (array.length < 2) {
                    continue;
                }
                String paramName = array[0];
                String paramDesc = array[1];
                docMap.put(JavaDoc.PARAM + paramName, paramDesc);
                continue;
            }
            if (line.startsWith("@author")) {
                line = line.replaceAll("^@author", "").trim();
                if (docMap.containsKey(JavaDoc.AUTHOR)) {
                    docMap.put(JavaDoc.AUTHOR, docMap.get(JavaDoc.AUTHOR) + "," + line);
                } else {
                    docMap.put(JavaDoc.AUTHOR, line);
                }
                continue;
            }
            if (line.startsWith("@deprecated")) {
                docMap.put(JavaDoc.DEPRECATED, "true");
            }
        }
        docMap.put(JavaDoc.DESCRIPTION, strip(descBuff.toString(), "\n"));
        return docMap;
    }

    private static String parseApiAccessUserType(String content) {
        Matcher apiAccessMatcher = API_ACCESS_PATTERN.matcher(content);
        if (!apiAccessMatcher.find()) {
            return null;
        }
        String userType = StringUtils.defaultIfBlank(apiAccessMatcher.group(1), "ALL");
        return StringUtils.removeStart(userType, "UserType.");
    }

    private static List<String> parseAuthorities(String content) {
        Matcher aclMatcher = ACL_PATTERN.matcher(content);
        if (!aclMatcher.find()) {
            return null;
        }
        List<String> authorities = new ArrayList<String>();
        String aclItems = aclMatcher.group(1);
        for (String item : aclItems.split("@AclItem")) {
            if (isBlank(item)) {
                continue;
            }
            Matcher roleMatcher = ACL_ITEM_ROLE_PATTERN.matcher(item);
            Matcher targetMatcher = ACL_ITEM_TARGET_PATTERN.matcher(item);
            if(roleMatcher.find() && targetMatcher.find()) {
                String role = roleMatcher.group(1).replaceAll("\"", "").toLowerCase();
                String target = targetMatcher.group(1);
                authorities.add(target + ":" + role);
            }
        }
        return authorities;
    }

    private static List<Parameter> parseParameters(String content) {

        StringBuilder buff = new StringBuilder();
        for (String line : content.split("\r\n")) {
            buff.append(strip(line, "\r\n").replaceAll("//.*$", "")).append("\n");
        }
        content = buff.toString();

        List<String> list = new ArrayList<>();

        Matcher matcher = Parameter.ANNOTATION_PATTERN.matcher(content);
        List<Integer[]> annoPosList = new ArrayList<>();
        while (matcher.find()) {
            annoPosList.add(new Integer[] {matcher.start(), matcher.end()});
        }

        int begin = 0;
        int end = -1;
        while ((end = content.indexOf(",", end + 1)) > 0) {
            final int curEnd = end;
            if (annoPosList.stream().anyMatch(pos -> curEnd > pos[0] && curEnd < pos[1])) {
                continue;
            }
            list.add(content.substring(begin, end));
            begin = end + 1;
        }
        list.add(content.substring(begin, content.length()));

        return list.stream()
                .filter(StringUtils::isNotBlank)
                .map(Parameter::parse)
                .filter(Objects::nonNull)
                .filter(p -> !Arrays.stream(SPRING_MVC_SUPPORTED_ARG_TYPES).anyMatch(p.getType()::endsWith))
                .collect(toList());
    }

    private static List<String> normalizeConfigValue(String value) {
        return Arrays.stream(strip(value.trim(), "{}").split("\\s*,\\s*"))
                .map(String::trim)
                .map(s -> strip(s, "\""))
                .map(s -> s.replaceAll("^RequestMethod\\.", ""))
                .collect(toList());
    }

    private static String normalizePath(File file) {
        return FilenameUtils.normalize(file.getAbsolutePath());
    }

    private static String readFileToString(File file) {
        try {
            return FileUtils.readFileToString(file);
        } catch (IOException e) {
            logger.error("failed to read content of file[{}]!", normalizePath(file));
            return null;
        }
    }

    public static class Project {

        private String groupId;

        private String artifactId;

        private String version;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public static Project fromPom(File pom) {
            if (pom == null) {
                return null;
            }
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(pom);
                Project project = new Project();
                Element projectEle = doc.getDocumentElement();
                Element parentEle = null;
                NodeList list = projectEle.getChildNodes();
                for (int i = 0, l = list.getLength(); i < l; i++) {
                    Node node = list.item(i);
                    if (GROUP_ID.equals(node.getNodeName())) {
                        project.setGroupId(node.getTextContent());
                    }
                    else if (ARTIFACT_ID.equals(node.getNodeName())) {
                        project.setArtifactId(node.getTextContent());
                    }
                    else if (VERSION.equals(node.getNodeName())) {
                        project.setVersion(node.getTextContent());
                    }
                    else if (PARENT.equals(node.getNodeName())) {
                        parentEle = (Element) node;
                    }
                }
                if (isBlank(project.getGroupId()) && parentEle != null) {
                    NodeList pl = parentEle.getElementsByTagName(GROUP_ID);
                    if (pl.getLength() > 0) {
                        project.setGroupId(pl.item(0).getTextContent());
                    }
                }
                return project;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    public static class Result implements Cloneable {

        /**
         * 项目信息
         */
        private Project project;

        /**
         * 请求url
         */
        private String requestUrl;

        /**
         * 类名(包括包名)
         */
        private String className;

        /**
         * 方法名
         */
        private String methodName;

        /**
         * 描述信息
         */
        private String description;

        /**
         * 权限注解
         */
        private List<String> acl;

        /**
         * 允许访问api的用户类型
         */
        private String apiAccessUserType;

        /**
         * 请求参数
         */
        private List<Parameter> parameters;

        /**
         * 返回值
         */
        private String returnType;

        /**
         * 请求方法
         */
        private List<String> requestMethods;

        /**
         * consumes
         */
        private List<String> consumes;

        /**
         * produces
         */
        private List<String> produces;

        /**
         * headers
         */
        private List<String> headers;

        /**
         * 是否已被废弃
         */
        private Boolean deprecated;

        /**
         * 作者列表
         */
        private List<String> authors;

        public Project getProject() {
            return project;
        }

        public void setProject(Project project) {
            this.project = project;
        }

        public String getRequestUrl() {
            return requestUrl;
        }

        public void setRequestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getAcl() {
            return acl;
        }

        public void setAcl(List<String> acl) {
            this.acl = acl;
        }

        public String getApiAccessUserType() {
            return apiAccessUserType;
        }

        public void setApiAccessUserType(String apiAccessUserType) {
            this.apiAccessUserType = apiAccessUserType;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        public List<Parameter> getParameters() {
            return parameters;
        }

        public void setParameters(List<Parameter> parameters) {
            this.parameters = parameters;
        }

        public List<String> getRequestMethods() {
            return requestMethods;
        }

        public void setRequestMethods(List<String> requestMethods) {
            this.requestMethods = requestMethods;
        }

        public List<String> getConsumes() {
            return consumes;
        }

        public void setConsumes(List<String> consumes) {
            this.consumes = consumes;
        }

        public List<String> getProduces() {
            return produces;
        }

        public void setProduces(List<String> produces) {
            this.produces = produces;
        }

        public List<String> getHeaders() {
            return headers;
        }

        public void setHeaders(List<String> headers) {
            this.headers = headers;
        }

        public Boolean getDeprecated() {
            return deprecated;
        }

        public void setDeprecated(Boolean deprecated) {
            this.deprecated = deprecated;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public void setAuthors(List<String> authors) {
            this.authors = authors;
        }

        @Override
        public Result clone() {
            try {
                return (Result) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static class Parameter {

        static Pattern ANNOTATION_PATTERN = Pattern.compile("@(\\w+)(?:\\((.+?)\\))?");

        private String name;

        private String type;

        @JSONField(serialize = false)
        private Boolean required;

        private String defaultValue;

        private String description;

        private Source source = Source.REQUEST_PARAM;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = required;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Source getSource() {
            return source;
        }

        public void setSource(Source source) {
            this.source = source;
        }

        public static enum Source {
            PATH_VARIABLE, REQUEST_PARAM, REQUEST_BODY;

            public static Source from(String str) {
                try {
                    return valueOf(str.replaceAll("(?!^)([A-Z])", "_$1").toUpperCase());
                } catch (Exception e) {
                    return null;
                }
            }
        }

        public static Parameter parse(String content) {
            Matcher matcher = ANNOTATION_PATTERN.matcher(content);
            Parameter parameter = new Parameter();
            parameter.setRequired(false);
            if (matcher.find()) {
                Source source = Source.from(matcher.group(1));
                if (source == null) {
                    return null;
                }
                parameter.setSource(source);
                if (matcher.group(2) != null) {
                    String[] array = matcher.group(2).split("\\s*,\\s*");
                    for (int i = 0; i < array.length; i++) {
                        String[] pair = array[i].split("\\s*=\\s*");
                        if (pair.length == 1) {
                            parameter.setName(strip(pair[0], "\""));
                            continue;
                        }
                        switch (pair[0]) {
                            case VALUE:
                            case NAME:
                                parameter.setName(strip(pair[1], "\""));
                                break;
                            case REQUIRED:
                                parameter.setRequired(BooleanUtils.toBoolean(pair[1]));
                                break;
                            case DEFAULT_VALUE:
                                parameter.setDefaultValue(strip(pair[1], "\""));
                                break;
                            default:
                        }
                    }
                    if (parameter.getDefaultValue() == null) {
                        parameter.setRequired(true);
                    }
                } else {
                    parameter.setRequired(true);
                }
                content = content.substring(matcher.end(), content.length());
            }
            String[] array = content.trim().split("\\s+");
            parameter.setType(array[0]);
            if (isBlank(parameter.getName())) {
                parameter.setName(array[1]);
            }
            return parameter;
        }

    }

}
