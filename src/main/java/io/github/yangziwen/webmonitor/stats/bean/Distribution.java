package io.github.yangziwen.webmonitor.stats.bean;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class Distribution {

    public static final int[] MILLISECOND_CEILS = { 20, 40, 60, 80, 100, 150, 200, 300, 500, 800, 1000, 1500, 2000, 3000, 5000, 10000 };

    private final AtomicInteger[] timeSlots;

    public Distribution() {
        timeSlots = initTimeSlots();
    }

    private Distribution(int[] slots) {
        if (slots == null || slots.length != MILLISECOND_CEILS.length + 1) {
            throw new IllegalArgumentException("slots is invalid");
        }
        this.timeSlots = new AtomicInteger[slots.length];
        for (int i = 0; i < slots.length; i++) {
            timeSlots[i] = new AtomicInteger(slots[i]);
        }
    }

    private static AtomicInteger[] initTimeSlots() {
        AtomicInteger[] slots = new AtomicInteger[MILLISECOND_CEILS.length + 1];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = new AtomicInteger(0);
        }
        return slots;
    }

    public void doStats(int value) {
        if (value <= 0) {
            return;
        }
        timeSlots[findIndex(value)].incrementAndGet();
    }

    public Map<String, Integer> toMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        int[] ceils = MILLISECOND_CEILS;
        int len = ceils.length;
        for (int i = 0, floor = 0, ceil = ceils[i];
                i < len;
                i++, floor = ceil) {
            ceil = ceils[i];
            map.put(floor + "-" + ceil, timeSlots[i].get());
        }
        map.put(ceils[len - 1] + "+", timeSlots[len].get());
        return map;
    }

    @Override
    public String toString() {
        return StringUtils.join(timeSlots, '|');
    }

    private int findIndex(int value) {
        int[] ceils = MILLISECOND_CEILS;
        int left = 0;
        int right = ceils.length - 1;
        if (value <= ceils[left]) {
            return left;
        }
        if (value > ceils[right]) {
            return right + 1;
        }
        while (right - left > 1) {
            int middle = (right + left) / 2;
            if (value <= ceils[middle]) {
                right = middle;
            } else {
                left = middle;
            }
        }
        return right;
    }

    public Distribution merge(int[] slots) {
        if (slots == null || slots.length < this.timeSlots.length) {
            return this;
        }
        for (int i = 0; i < this.timeSlots.length; i++) {
            this.timeSlots[i].addAndGet(slots[i]);
        }
        return this;
    }

    public Distribution merge(AtomicInteger[] slots) {
        if (slots == null || slots.length < this.timeSlots.length) {
            return this;
        }
        for (int i = 0; i < this.timeSlots.length; i++) {
            this.timeSlots[i].addAndGet(slots[i].get());
        }
        return this;
    }

    public Distribution merge(Distribution other) {
        if (other == null) {
            return this;
        }
        return merge(other.timeSlots);
    }

    public Distribution merge(String value) {
        return merge(parse(value));
    }

    public static Distribution parse(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        int[] slots = Arrays.stream(StringUtils.split(value, '|'))
                .mapToInt(NumberUtils::toInt)
                .toArray();
        if (slots.length < MILLISECOND_CEILS.length + 1) {
            return null;
        }
        return new Distribution(slots);
    }

}
