package io.github.zebin.javabash.sandbox;

import io.github.zebin.javabash.process.TextTerminal;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.util.function.Supplier;

@Slf4j
public class PosixUtils {

    public static final String WIN_BASH_PATH = "C:\\Program Files\\Git\\bin\\bash.exe";

    public static Thread asyncRead(BufferedReader t, PrintWriter out) {
        Thread thread = new Thread(
                () -> {
                    try {
                        String line;
                        while ((line = t.readLine()) != null) {
                            out.println(line);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        thread.start();
        return thread;
    }

    public static String cdAbs(Path jump) {
        return "cd " + escape(toAbsolutePosix(jump));
    }

    public static String cd(Path jump) {
        return "cd " + escape(toPosix(jump));
    }

    public static String escape(String posix) {
        return "\"" + posix.replace("\"", "\\\"") + "\"";
    }

    public static String escape(PosixPath posix) {
        return escape(posix.toString());
    }

    public static String decode(String value) {
        return removeSurrounding(removeSurrounding(value, "'"), "\"");
    }

    private static String removeSurrounding(String value, String prefix) {
        if (value.startsWith(prefix) && value.endsWith(prefix)) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public static String toAbsolutePosix(Path p) {
        return toPosix("/" + p.toAbsolutePath().normalize(), true);
    }

    private static String toPosix(String p, boolean isAbsolute) {
        String replace = p
                .replace("\\", "/")
                .replace(":", "");

        if (isAbsolute && !replace.startsWith("/")) {
            replace = "/" + replace;
        }

        return replace;
    }

    public static String toPosix(Path p) {
        return toPosix(p.toString(), p.isAbsolute());
    }

    public static String encode(Path p) {
        return escape(toPosix(p));
    }

    public static void append(StringBuffer buf, String line) {

        if (!buf.isEmpty()) {
            buf.append(System.lineSeparator());
        }
        buf.append(line);
    }

    public static Process runShell(Runtime runtime, String path) {
        Process terminal = null;
        try {
            terminal = runtime.exec(new String[]{path});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return terminal;
    }

    public static Process runShellForOs(Runtime r) {
        String osNameLowercased = System.getProperty("os.name").toLowerCase();
        log.debug("OS name = {}", osNameLowercased);
        boolean isWindows = osNameLowercased.startsWith("windows");

        if (isWindows) {
            log.debug("Windows OS is found");
            return runShell(r, WIN_BASH_PATH);
        }
        log.debug("Linux OS is found");
        return runShell(r, "bash");
    }

    private static void openSecret(TextTerminal bash, String key, String value) {
        bash.eval(String.format("%s=%s", key, value), String.format("%s=***", key));
    }

    private static void closeSecret(TextTerminal bash, String key) {
        bash.eval(String.format("%s=''", key), String.format("%s=''", key));
    }

    public static <T> T withSecret(
            TextTerminal bash,
            String key, String value,
            Supplier<T> result
    ) {
        try {
            openSecret(bash, key, value);
            return result.get();
        } finally {
            closeSecret(bash, key);
        }
    }

    public static <T> T lockDir(TextTerminal bash, Supplier<T> result) {
        String pwd = bash.eval("pwd");
        try {
            return result.get();
        } finally {
            bash.eval("cd " + pwd);
        }
    }
}
