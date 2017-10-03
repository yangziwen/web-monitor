package io.github.yangziwen.webmonitor.schedule;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import it.sauronsoftware.cron4j.TaskExecutionContext;

public class ProcessTask extends it.sauronsoftware.cron4j.ProcessTask {

    private boolean append = true;

    public ProcessTask(String[] command) {
        super(command);
    }

    public ProcessTask(String[] command, String[] envs, File directory) {
        super(command, envs, directory);
    }

    public boolean getAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    @Override
    public void execute(TaskExecutionContext context) throws RuntimeException {
        Process p;
        try {
            p = exec();
        } catch (IOException e) {
            throw new RuntimeException(toString() + " cannot be started", e);
        }
        InputStream in = buildInputStream(getStdinFile());
        OutputStream out = buildOutputStream(getStdoutFile());
        OutputStream err = buildOutputStream(getStderrFile());
        if (in != null) {
            StreamBridge b = new StreamBridge(in, p.getOutputStream());
            b.start();
        }
        if (out != null) {
            StreamBridge b = new StreamBridge(p.getInputStream(), out);
            b.start();
        }
        if (err != null) {
            StreamBridge b = new StreamBridge(p.getErrorStream(), err);
            b.start();
        }
        int r;
        try {
            r = p.waitFor();
            if (out != null || err != null) {
                TimeUnit.MILLISECONDS.sleep(30L);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(toString() + " has been interrupted");
        } finally {
            closeQuietly(in);
            closeQuietly(out);
            closeQuietly(err);
            p.destroy();
        }
        if (r != 0) {
            throw new RuntimeException(toString() + " returns with error code " + r);
        }
    }

    private Process exec() throws IOException {
        Runtime rt = Runtime.getRuntime();
        return rt.exec(getCommand(), getEnvs(), getDirectory());
    }

    private InputStream buildInputStream(File file) {
        if (file != null) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private OutputStream buildOutputStream(File file) {
        if (file != null) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try {
                return new FileOutputStream(file, append);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private static final void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable t) {
            }
        }
    }

}
