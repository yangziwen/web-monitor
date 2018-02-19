package io.github.yangziwen.webmonitor.schedule;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.fastjson.annotation.JSONField;
import com.typesafe.config.Config;

import io.github.yangziwen.webmonitor.GlobalConfig;
import it.sauronsoftware.cron4j.Scheduler;

public class TaskConfig {

    private Type type;

    private String cron;

    private Class<?> clazz;

    private Method method;

    private String cmd;

    private File outFile;

    @JSONField(name = "type")
    public Type type() {
        return type;
    }

    public void type(Type type) {
        this.type = type;
    }

    @JSONField(name = "cron")
    public String cron() {
        return cron;
    }

    public void cron(String cron) {
        this.cron = cron;
    }

    @JSONField(name = "class")
    public String className() {
        if (clazz == null) {
            return null;
        }
        return clazz.getName();
    }

    public Class<?> clazz() {
        return clazz;
    }

    public void clazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    @JSONField(name = "method")
    public String methodName() {
        if (method == null) {
            return null;
        }
        return method.getName();
    }

    public Method method() {
        return method;
    }

    public void method(Method method) {
        this.method = method;
    }

    @JSONField(name = "cmd")
    public String cmd() {
        return cmd;
    }

    public void cmd(String cmd) {
        this.cmd = cmd;
    }

    @JSONField
    public File outFile() {
        return outFile;
    }

    public void outFile(File file) {
        this.outFile = file;
    }

    public Runnable runnable() {
        if (type != Type.METHOD) {
            return null;
        }
        return () -> {
            try {
                boolean isStatic = Modifier.isStatic(method().getModifiers());
                Object instance = isStatic ? null : clazz().newInstance();
                method().invoke(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    public void schedule(Scheduler scheduler) {
        type().schedule(this, scheduler);
    }

    public static List<TaskConfig> from(Config config, List<String> taskNames) {
        return taskNames.stream()
                .map(name -> config.getConfig(name))
                .map(GlobalConfig.task::newTaskConfig)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static enum Type {

        PROCESS {
            @Override
            void schedule(TaskConfig config, Scheduler scheduler) {
                ProcessTask processTask = new ProcessTask(config.cmd().split("\\s+"));
                if (config.outFile() != null) {
                    processTask.setStdoutFile(config.outFile());
                    processTask.setStderrFile(config.outFile());
                }
                scheduler.schedule(config.cron(), processTask);
            }
        },
        METHOD {
            @Override
            void schedule(TaskConfig config, Scheduler scheduler) {
                scheduler.schedule(config.cron(), config.runnable());
            }
        };

        abstract void schedule(TaskConfig config, Scheduler scheduler);

        public static Type parse(String value) {
            return Arrays.stream(Type.values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .reduce(null, (a, b) -> b);
        }
    }
}
