/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */

package io.github.yangziwen.webmonitor.command;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.webmonitor.model.UrlPattern;
import io.github.yangziwen.webmonitor.service.MonitorService;
import io.github.yangziwen.webmonitor.util.ApiScanner;
import io.github.yangziwen.webmonitor.util.ApiScanner.Result;
import lombok.extern.slf4j.Slf4j;
import spark.utils.CollectionUtils;

/**
 * ScanApiCommand
 *
 * @author Yang Ziwen(yangziwen@baidu.com)
 */
@Slf4j
@Parameters(separators = "=", commandDescription = "scan api info of spring mvc project")
public class ScanApiCommand implements Command {

    @Parameter(
            names = {"-h", "--help"},
            description = "print this message",
            help = true)
    public boolean help;

    @Parameter(
            names = {"-p", "--project-path"},
            description = "specify the project path")
    public File projectFolder;

    @Override
    public void invoke(JCommander commander) {
        if (help) {
            commander.usage(name());
            return;
        }
        List<Result> results = ApiScanner.scan(projectFolder);
        if (CollectionUtils.isEmpty(results)) {
            log.info("no api info found in project[{}]", projectFolder.getAbsolutePath());
            return;
        }
        List<UrlPattern> patterns = results.stream().map(result -> UrlPattern.builder()
                .url(result.getRequestUrl())
                .project(result.getProject().getArtifactId())
                .build())
                .collect(Collectors.toMap(UrlPattern::getUrl, pattern -> pattern, (v1, v2) -> v2))
                .values().stream().collect(Collectors.toList());
        List<String> projects = patterns.stream()
                .map(UrlPattern::getProject)
                .distinct()
                .collect(Collectors.toList());
        MonitorService.deleteUrlPatternsByParams(new Criteria().and("project").in(projects));
        MonitorService.batchSaveUrlPatterns(patterns);
        log.info("import {} apis of project{} successfully", patterns.size(), projects);
    }

}
