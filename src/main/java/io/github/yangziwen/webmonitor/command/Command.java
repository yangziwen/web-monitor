package io.github.yangziwen.webmonitor.command;

import com.beust.jcommander.JCommander;

public interface Command {

    public void invoke(JCommander commander);

    default String name() {
        return this
                .getClass()
                .getSimpleName()
                .replaceAll("(?<!^)([A-Z])", "-$1")
                .toLowerCase()
                .replaceAll("-command$", "");
    }

}
