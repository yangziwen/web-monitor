package io.github.yangziwen.webmonitor;

import static io.github.yangziwen.webmonitor.util.ExecUtil.consume;
import static io.github.yangziwen.webmonitor.util.ExecUtil.exec;
import static io.github.yangziwen.webmonitor.util.ExecUtil.waitFor;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

public class VueBuilder {

    private VueBuilder() {}

    public static void main(String[] args) {
        String basePath = System.getProperty("basedir");
        String frontendPath = FilenameUtils.concat(basePath, "src/main/frontend");
        waitFor(consume(exec("npm run build", new File(frontendPath)), input -> {
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = input.read(bytes)) != -1) {
                System.out.print(new String(ArrayUtils.subarray(bytes, 0, len)));
            }
        }));
    }

}
