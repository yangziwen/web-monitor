package io.github.yangziwen.webmonitor.controller;

public enum ResultEnum {

    OK(200),
    BAD_REQUEST(400)
    ;

    private int code;

    private ResultEnum(int code) {
        this.code = code;
    }

    public Result newResult() {
        return new Result(this);
    }

    public static class Result {

        private int code;

        private String status;

        private String message;

        private Object data;

        private Result(ResultEnum codeEnum) {
            this.code = codeEnum.code;
            this.status = codeEnum.name();
        }

        public int getCode() {
            return code;
        }

        public String getStatus() {
            return status;
        }

        public Object getData() {
            return data;
        }

        public String getMessage() {
            return message;
        }

        public Result message(String message) {
            this.message = message;
            return this;
        }

        public Result message(String message, Object...args) {
            this.message = String.format(message, args);
            return this;
        }

        public Result data(Object data) {
            this.data = data;
            return this;
        }

    }

}
