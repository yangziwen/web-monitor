package io.github.yangziwen.webmonitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;

import io.github.yangziwen.webmonitor.controller.IndexController;
import io.github.yangziwen.webmonitor.controller.MonitorController;
import io.github.yangziwen.webmonitor.schedule.TaskConfig;
import it.sauronsoftware.cron4j.Scheduler;
import lombok.extern.slf4j.Slf4j;
import spark.Spark;

@Slf4j
public class Server {

    public static final String DEFAULT_HOST = "0.0.0.0";

    public static final int DEFAULT_PORT = 8050;

    public static final String DEFAULT_STATIC_LOCATION = "/static";

    private Server() {}

    @Deprecated
    public static void main(String[] args) {

        run(DEFAULT_HOST, getPort(), DEFAULT_STATIC_LOCATION);

    }

    public static void run(String ipAddress, Integer port, String staticLocation) {

        port = ObjectUtils.defaultIfNull(port, DEFAULT_PORT);

        ipAddress = StringUtils.defaultString(ipAddress, DEFAULT_HOST);

        staticLocation = StringUtils.defaultString(staticLocation, DEFAULT_STATIC_LOCATION);

        checkPortAvailable(DEFAULT_HOST, port);

        Spark.staticFiles.location(staticLocation);

        Spark.staticFiles.expireTime(28800);

        Spark.ipAddress(ipAddress);

        Spark.port(port);

        initControllers();

        initLandingPage(staticLocation);

        initTasks();

        Runtime.getRuntime().addShutdownHook(new Thread(Spark::stop));

    }

    private static void initControllers() {

        IndexController.init();

        MonitorController.init();

    }

    private static void initTasks() {

        if (CollectionUtils.isEmpty(GlobalConfig.task.tasks)) {
            return;
        }

        Scheduler scheduler = new Scheduler();

        for (TaskConfig task : GlobalConfig.task.tasks) {
            task.schedule(scheduler);
            log.info("scheduled task: {}", JSON.toJSONString(task));
        }

        scheduler.start();

        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::stop));

    }

    private static int getPort() {
        try {
            return Integer.valueOf(System.getProperty("port"));
        } catch (NumberFormatException e) {
            return DEFAULT_PORT;
        }
    }

    private static boolean isPortAvailable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(host, port));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void checkPortAvailable(String host , int port) {
        if (!isPortAvailable(DEFAULT_HOST, port)) {
            log.error("The port {} is not available, press 'Enter' to exit", port);
            try (Reader reader = new InputStreamReader(System.in)) {
                reader.read();
            } catch (IOException e) {
                log.error("Error happened when waiting user's input");
            } finally {
                System.exit(1);
            }
        }
    }

    private static void initLandingPage(String staticLocation) {
        String content = getLandingPageContent(staticLocation);
        Spark.get("/*", (request, response) -> {
            response.type("text/html");
            return content;
        });
    }

    private static String getLandingPageContent(String staticLocation) {
        try (InputStream input = Server.class.getResourceAsStream(staticLocation + "/index_prod.html")) {
            return removeViewportMeta(IOUtils.toString(input));
        } catch (Exception e1) {
            try (InputStream input = Server.class.getResourceAsStream(staticLocation + "/index.html")) {
                return removeViewportMeta(IOUtils.toString(input));
            } catch (Exception e2) {
                return "page not found";
            }
        }
    }

    private static String removeViewportMeta(String content) {
        return content;
    }

}
