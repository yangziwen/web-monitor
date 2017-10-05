package io.github.yangziwen.webmonitor.metrics.bean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.alibaba.fastjson.annotation.JSONField;

import javafx.util.Pair;

public class Metrics {

    protected AtomicLong cnt = new AtomicLong(0);

    protected AtomicInteger min = new AtomicInteger(0);

    protected AtomicInteger max = new AtomicInteger(0);

    protected AtomicLong sum = new AtomicLong(0L);

    @JSONField(serialize = false)
    protected Distribution distribution = new Distribution();

    public long getCnt() {
        return cnt.get();
    }

    public int getMin() {
        return min.get();
    }

    public int getMax() {
        return max.get();
    }

    public long getSum() {
        return sum.get();
    }

    public int getAvg() {
        if (cnt.get() == 0) {
            return 0;
        }
        return new Long(sum.get() / cnt.get()).intValue();
    }

    @SuppressWarnings("restriction")
    public List<Pair<String, Integer>> getDistributionList() {
        Map<String, Integer> map =  this.distribution.toMap();
        return map.entrySet().stream()
                .map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public void setDistribution(Distribution distribution) {
        this.distribution = distribution;
    }

    public void doStats(int value) {
        if (value <= 0) {
            return;
        }
        cnt.incrementAndGet();
        sum.addAndGet(value);
        int maxValue;
        while (value > (maxValue = max.get())) {
            if (max.compareAndSet(maxValue, value)) {
                break;
            }
        }
        int minValue;
        while (value < (minValue = min.get()) || minValue <= 0) {
            if (min.compareAndSet(minValue, value)) {
                break;
            }
        }
        distribution.doStats(value);
    }

    public Metrics merge(Metrics other) {
        if (other == null) {
            return this;
        }
        cnt.addAndGet(other.getCnt());
        sum.addAndGet(other.getSum());
        int thisMax, otherMax;
        while ((otherMax = other.getMax()) > (thisMax = this.getMax())) {
            if (max.compareAndSet(thisMax, otherMax)) {
                break;
            }
        }
        int thisMin, otherMin;
        while ((otherMin = other.getMin()) < (thisMin = this.getMin()) || thisMin <= 0) {
            if (min.compareAndSet(thisMin, otherMin)) {
                break;
            }
        }
        this.distribution.merge(other.distribution);
        return this;
    }


}
