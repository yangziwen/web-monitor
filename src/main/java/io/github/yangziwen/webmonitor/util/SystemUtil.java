package io.github.yangziwen.webmonitor.util;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

@Slf4j
public class SystemUtil {

    private static final SystemInfo SYSTEM_INFO = new SystemInfo();

    private static final OperatingSystem OPERATING_SYSTEM = SYSTEM_INFO.getOperatingSystem();

    private static final HardwareAbstractionLayer HARDWARE = SYSTEM_INFO.getHardware();

    private static volatile BigDecimal cpuUsageRate = BigDecimal.ONE.negate();

    static {
        estimateCpuUsageRatePeriodlly();
    }

    private SystemUtil() {}

    public static int getLogicalProcessorCount() {
        return HARDWARE.getProcessor().getLogicalProcessorCount();
    }

    public static int getPhysicalProcessorCount() {
        return HARDWARE.getProcessor().getPhysicalProcessorCount();
    }

    public static int getCurrentProcessId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (StringUtils.isEmpty(name)) {
            return -1;
        }
        return NumberUtils.toInt(StringUtils.split(name, "@")[0], -1);
    }

    public static BigDecimal getCurrentCpuUsageRate() {
        return cpuUsageRate;
    }

    private static void sleepQuietly(long timeout, TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException e) {
            log.error("sleep {}{} is interrupted by error ", timeout, unit, e);
        }
    }

    // 秒级估算cpu使用率
    private static void estimateCpuUsageRatePeriodlly() {
        Thread thread = new Thread(() -> {
            long previousProcessTime = -1L;
            long currentProcessTime = 0L;
            while (true) {
                OSProcess process = OPERATING_SYSTEM.getProcess(getCurrentProcessId());
                currentProcessTime = process.getKernelTime() + process.getUserTime();
                if (previousProcessTime > 0) {
                    cpuUsageRate = BigDecimal.valueOf(currentProcessTime - previousProcessTime)
                            .divide(BigDecimal.valueOf(1000L))
                            .divide(BigDecimal.valueOf(getLogicalProcessorCount()), 4, RoundingMode.HALF_UP);
                }
                previousProcessTime = currentProcessTime;
                sleepQuietly(1L, TimeUnit.SECONDS);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

}
