package io.github.yangziwen.webmonitor.metrics.parse;

public abstract class Parser {

    protected static final Parser VOID = new Parser() {
        @Override
        protected Parser doParse(String content, int offset) {
            return VOID;
        }
        @Override
        public Parser next() {
            return VOID;
        }
        @Override
        public Parser parse(String content, int offset) {
            return VOID;
        }
        @Override
        public String result(Class<? extends Parser> clazz) {
            return null;
        }
        @Override
        public Parser reset() {
            return VOID;
        }
    };

    private int start;

    private int end;

    private String result;

    private Parser next = VOID;

    public int start() {
        return start;
    }

    public Parser start(int start) {
        this.start = start;
        return this;
    }

    public int end() {
        return end;
    }

    public Parser end(int end) {
        this.end = end;
        return this;
    }

    public String result() {
        return result;
    }

    public Parser result(String result) {
        this.result = result;
        return this;
    }

    public Parser result(String content, int start, int end) {
        if (content != null && start >= 0 && end >= 0 && end >= start && content.length() >= end) {
            this.result = content.substring(start, end);
        }
        return this;
    }

    public String result(Class<? extends Parser> clazz) {
        if (clazz.isInstance(this)) {
            return result();
        }
        return next().result(clazz);
    }

    public Parser next() {
        return next;
    }

    public Parser next(Parser next) {
        this.next = next;
        return next;
    }

    public Parser reset() {
        start(0).end(0).result((String)null).next().reset();
        return this;
    }

    public Parser parse(String content) {
        return parse(content, 0);
    }

    public Parser parse(String content, int offset) {
        doParse(content, offset).next().parse(content, end());
        return this;
    }

    public Parser checkAndReturn(boolean isValid) {
        return isValid ? this : VOID;
    }

    protected abstract Parser doParse(String content, int offset);

}
