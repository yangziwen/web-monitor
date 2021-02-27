package io.github.yangziwen.webmonitor.controller;

import static io.github.yangziwen.webmonitor.controller.ResultEnum.OK;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;

import io.github.yangziwen.webmonitor.util.SystemUtil;
import spark.Spark;

public class SystemController extends BaseController {

    private SystemController() {}

    public static void init() {

        Spark.get("/system/info.json", (request, response) -> {
            response.type(CONTENT_TYPE_JSON);
            return OK.newResult()
                    .data(ImmutableMap.<String, Object>builder()
                            .put("processId", SystemUtil.getCurrentProcessId())
                            .put("logicalProcessorCount", SystemUtil.getLogicalProcessorCount())
                            .put("physicalProcessorCount", SystemUtil.getPhysicalProcessorCount())
                            .put("cpuUsageRate", SystemUtil.getCurrentCpuUsageRate())
                            .build());
        }, JSON::toJSONString);

    }

}
