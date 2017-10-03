package io.github.yangziwen.webmonitor.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ExecUtil {

    private ExecUtil() {}

    public static Process exec(String cmd, File dir) {
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd.split("\\s+"));
            builder.directory(dir);
            builder.redirectErrorStream(true);
            return builder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Process consume(Process process, Callback<T> callback) {
        if(process == null) {
            return process;
        }
        try (InputStream in = process.getInputStream()) {
            callback.call(in);
            return process;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int waitFor(Process process) {
        if(process == null) {
            return -1;
        }
        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            process.destroy();
        }
    }

    public static int execAndWaitFor(String cmd, File dir) {
        return waitFor(exec(cmd, dir));
    }

    public static interface Callback<V> {

        public void call(InputStream input) throws Exception;

    }

}
