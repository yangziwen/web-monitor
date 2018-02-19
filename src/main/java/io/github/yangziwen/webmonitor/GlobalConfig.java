package io.github.yangziwen.webmonitor;

import static io.github.yangziwen.webmonitor.util.ConfigUtil.getConfig;
import static io.github.yangziwen.webmonitor.util.ConfigUtil.getIntOrDefault;
import static io.github.yangziwen.webmonitor.util.ConfigUtil.getStringList;
import static io.github.yangziwen.webmonitor.util.ConfigUtil.getStringOrDefault;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.strip;

import java.io.File;
import java.util.List;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import io.github.yangziwen.webmonitor.schedule.TaskConfig;

public interface GlobalConfig {

    static Config config = load();

    static Config load() {
        return ConfigFactory.parseFile(new File("conf/global.config"));
    }

    interface database {

        Config config = getConfig(GlobalConfig.config, "database");

        static String driver_class_name = getStringOrDefault(config, "driver-class-name", "");

        static String url = getStringOrDefault(config, "url", "");

        static String username = getStringOrDefault(config, "username", "");

        static String password = getStringOrDefault(config, "password", "");

        static int min_idle = getIntOrDefault(config, "min-idle", 1);

        static int max_idle = getIntOrDefault(config, "max-idle", 1);

        static int max_active = getIntOrDefault(config, "max-active", 1);

    }

    interface task {

        Config config = getConfig(GlobalConfig.config, "task");

        List<String> enabledList = getStringList(config, "enabled");

        List<TaskConfig> tasks = TaskConfig.from(config, enabledList);

        static TaskConfig newTaskConfig(Config config) {
            if (config == null) {
                return null;
            }
            try {
                TaskConfig task = new TaskConfig();
                task.type(TaskConfig.Type.parse(strip(config.getString("type"), "\"")));
                task.cron(strip(config.getString("cron"), "\""));
                if (task.type() == TaskConfig.Type.METHOD) {
                    task.clazz(Class.forName(strip(config.getString("className"), "\"")));
                    task.method(task.clazz().getDeclaredMethod(strip(config.getString("methodName"), "\"")));
                }
                if (task.type() == TaskConfig.Type.PROCESS) {
                    task.cmd(strip(config.getString("cmd"), "\""));
                    String outFileName = strip(getStringOrDefault(config, "outfile", null), "\"");
                    if (isNotBlank(outFileName)) {
                        task.outFile(new File(outFileName));
                    }
                }
                return task;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

}

