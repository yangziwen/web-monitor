package io.github.yangziwen.webmonitor.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.jetty.util.BlockingArrayQueue;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import io.github.yangziwen.webmonitor.command.converter.DateConverter;
import io.github.yangziwen.webmonitor.command.converter.TimeIntervalConverter;
import io.github.yangziwen.webmonitor.metrics.UrlMetricsManager;
import io.github.yangziwen.webmonitor.metrics.UrlPatternManager;
import io.github.yangziwen.webmonitor.metrics.bean.NginxAccess;
import io.github.yangziwen.webmonitor.metrics.bean.UrlMetrics;
import io.github.yangziwen.webmonitor.metrics.parse.NginxAccessParser;
import io.github.yangziwen.webmonitor.model.UrlMetricsResult;
import io.github.yangziwen.webmonitor.service.MonitorService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Parameters(separators = "=", commandDescription = "import data from access log files")
public class ImportDataCommand implements Command {

    @Parameter(
            names = {"-h", "--help"},
            description = "print this message",
            help = true)
    public boolean help;

    @Parameter(
            names = {"-tn", "--thread-number"},
            description = "specify the thread number")
    public int threadNum = 5;

    @Parameter(
            names = {"-ft", "--from-time"},
            description = "specify the from time (yyyy-MM-dd[ HH:mm:ss])",
            converter = DateConverter.class,
            required = true)
    public Date fromTime;

    @Parameter(
            names = {"-tt", "--to-time"},
            description = "specify the to time (yyyy-MM-dd[ HH:mm:ss])",
            converter = DateConverter.class)
    public Date toTime = new Date();

    @Parameter(
            names = {"-i", "--interval"},
            description = "specify the statistic interval(10m, 1h, 3d), must be not less than 10m",
            converter = TimeIntervalConverter.class)
    public Long interval = 10 * DateUtils.MILLIS_PER_MINUTE;

    @Parameter(
            names = {"-f", "--access-logs"},
            description = "the paths of nginx access log files",
            required = true)
    public List<File> logFiles;


    @Override
    public void invoke(JCommander commander) {
        if (help) {
            commander.usage(name());
            return;
        }
        if (interval < 10 * DateUtils.MILLIS_PER_MINUTE) {
            log.error("interval is too short");
            return;
        }
        if (!logFiles.stream().allMatch(File::isFile)) {
            log.error("some log files are not existed");
            return;
        }
        new Processor(logFiles, threadNum, fromTime, toTime, interval).run();
    }

    public static class Processor {

        private final int threadNum;

        private Date toTime;

        private long interval;

        private Date beginTime;

        private Date endTime;

        private BlockingQueue<ReaderContext> fromQueue;

        private BlockingQueue<ReaderContext> toQueue;

        private List<Thread> threads;

        private CountDownLatch latch;

        private AtomicBoolean finished = new AtomicBoolean(false);

        private ConcurrentHashMap<String, UrlMetrics> metricsMap = new ConcurrentHashMap<>();

        public Processor(List<File> files, int threadNum, Date fromTime, Date toTime, long interval) {
            this.threadNum = threadNum;
            this.toTime = toTime;
            this.interval = interval;
            this.beginTime = fromTime;
            this.endTime = new Date(beginTime.getTime() + interval);
            this.fromQueue = new BlockingArrayQueue<>(files.size());
            this.toQueue = new BlockingArrayQueue<>(files.size());
            this.latch = new CountDownLatch(threadNum);
            this.threads = IntStream.range(0, threadNum).mapToObj(i -> {
                Thread thread = new Thread(this::consumeReader);
                thread.setDaemon(true);
                thread.setName("process-access-log-thread-" + i);
                return thread;
            }).collect(Collectors.toList());
            fromQueue.addAll(files.stream().map(ReaderContext::new).collect(Collectors.toList()));
        }

        private void consumeReader() {
            while (!finished.get()) {
                ReaderContext context = fromQueue.poll();
                if (context == null) {
                    blockThread();
                    continue;
                }
                try {
                    String timePattern = "yyyy-MM-dd HH:mm";
                    String beginTimeStr = DateFormatUtils.format(beginTime, timePattern);
                    String endTimeStr = DateFormatUtils.format(endTime, timePattern);
                    process(context);
                    log.info("finished to process file[{}] in the range of [{} - {}]", context.getFile().getName(), beginTimeStr, endTimeStr);
                } catch (IOException e) {
                    log.error("error happend when process file {}", context.getFile(), e);
                }
            }
        }

        private void process(ReaderContext context) throws IOException {
            NginxAccess access = context.getLatestRecord();
            if (access != null) {
                if (access.getTimestamp() > endTime.getTime()) {
                    toQueue.add(context);
                    return;
                }
                String pattern = UrlPatternManager.getBestMatchedUrlPattern(access.getBackendUrl());
                UrlMetricsManager.ensureMetrics(pattern, metricsMap).doStats(access);
                context.setLatestRecord(null);
            }
            NginxAccessParser parser = new NginxAccessParser();
            String line = null;
            while ((line = context.getReader().readLine()) != null) {
                parser.parse(line);
                access = parser.toNginxAccess();
                if (access.getTimestamp() < beginTime.getTime()) {
                    continue;
                }
                if (access.getTimestamp() > endTime.getTime()) {
                    context.setLatestRecord(access);
                    toQueue.add(context);
                    return;
                }
                String pattern = UrlPatternManager.getBestMatchedUrlPattern(access.getBackendUrl());
                UrlMetricsManager.ensureMetrics(pattern, metricsMap).doStats(access);
            }
        }

        public void run() {
            threads.forEach(Thread::start);
            while (true) {
                ImportDataCommand.waitQuietly(latch);
                persistMetricsResults(beginTime, endTime, metricsMap);
                metricsMap = new ConcurrentHashMap<>();
                beginTime = endTime;
                endTime = new Date(beginTime.getTime() + interval);
                if (beginTime.getTime() >= toTime.getTime()) {
                    finished.set(true);
                    passAllThreads();
                    break;
                }
                BlockingQueue<ReaderContext> temp = fromQueue;
                fromQueue = toQueue;
                toQueue = temp;
                latch = new CountDownLatch(this.threadNum);
                passAllThreads();
            }
            close();
        }

        public void close() {
            fromQueue.forEach(ReaderContext::close);
            toQueue.forEach(ReaderContext::close);
        }

        public synchronized void blockThread() {
            try {
                latch.countDown();
                this.wait();
            } catch (InterruptedException e) {
                // be quiet
            }
        }

        public synchronized void passAllThreads() {
            this.notifyAll();
        }

    }

    @Data
    private static class ReaderContext {

        private File file;

        private BufferedReader reader;

        private NginxAccess latestRecord;

        public ReaderContext(File file) {
            this.file = file;
            try {
                this.reader = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public void close() {
            IOUtils.closeQuietly(reader);
        }

    }

    private static void waitQuietly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            // be quiet
        }
    }

    private static void persistMetricsResults(Date beginTime, Date endTime, Map<String, UrlMetrics> metricsMap) {
        List<UrlMetricsResult> results = metricsMap.values().stream()
                .map(metrics -> UrlMetricsResult.from(metrics, beginTime, endTime))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        MonitorService.batchSaveUrlMetricsResults(results);
        String timePattern = "yyyy-MM-dd HH:mm";
        log.info("persist [{}] metrics results in the range of [{} - {}]",
                metricsMap.size(),
                DateFormatUtils.format(beginTime, timePattern),
                DateFormatUtils.format(endTime, timePattern));
    }

}
