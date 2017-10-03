package io.github.yangziwen.webmonitor.controller;

import static io.github.yangziwen.webmonitor.controller.ResultEnum.OK;

import com.alibaba.fastjson.JSON;

import spark.Spark;

public class IndexController extends BaseController {

    public static void init() {

        Spark.get("/index.json", (request, response) -> {
            response.type(CONTENT_TYPE_JSON);
            return OK.newResult();
        }, JSON::toJSONString);

    }

}
