package io.github.yangziwen.webmonitor;

import static io.github.yangziwen.webmonitor.util.ExecUtil.consume;
import static io.github.yangziwen.webmonitor.util.ExecUtil.exec;
import static io.github.yangziwen.webmonitor.util.ExecUtil.waitFor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

public class VueBuilder {

    private VueBuilder() {}

    public static void main(String[] args) {
        String basePath = System.getProperty("basedir");
        String frontendPath = FilenameUtils.concat(basePath, "src/main/frontend");
        runCommand("npm install", frontendPath);
        runCommand("npm run build", frontendPath);
    }

    private static void runCommand(String cmd, String path) {
        waitFor(consume(exec(cmd, new File(path)), VueBuilder::printInputStream));
    }

    private static void printInputStream(InputStream input) throws IOException {
        byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = input.read(bytes)) != -1) {
            System.out.print(new String(ArrayUtils.subarray(bytes, 0, len)));
        }
    }

}
