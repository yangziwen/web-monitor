package io.github.yangziwen.webmonitor.util;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class Progress {

    private String name;

    private long total;

    private long current = 0L;

    private int renderedCharLength;

    public Progress(String name, Long total) {
        this.name = name;
        this.total = total;
    }

    public double getPercent() {
        if (total <= 0) {
            return 0D;
        }
        return current * 1.0D / total;
    }

    public String getProgress() {
        return new BigDecimal(getPercent() * 100)
                .setScale(1, BigDecimal.ROUND_HALF_UP)
                .toPlainString() + "%";
    }

    public Progress render() {
        System.err.print(StringUtils.repeat('\b', renderedCharLength));
        String str = name + ": " + getProgress();
        renderedCharLength = str.toCharArray().length;
        System.out.print(str);
        return this;
    }

    public Progress update(long current) {
        setCurrent(current);
        render();
        return this;
    }

}
