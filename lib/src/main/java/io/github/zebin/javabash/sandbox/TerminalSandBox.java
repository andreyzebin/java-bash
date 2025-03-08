package io.github.zebin.javabash.sandbox;

import io.github.zebin.javabash.process.TextTerminal;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TerminalSandBox {

    private final TextTerminal delegate;

    public TerminalSandBox(TextTerminal delegate) {
        this.delegate = delegate;
    }

    public PosixPath pwd() {
        return PosixPath.ofPosix(delegate.eval("pwd"));
    }

    public PosixPath up() {
        delegate.eval("cd ..");
        return pwd();
    }

    public String cat(PosixPath pp) {
        return delegate.eval(String.format("cat %s", pp));
    }

    public PosixPath touch(PosixPath newDir) {
        delegate.eval(String.format("touch %s", newDir));
        return newDir.isAbsolute() ? newDir : pwd().climb(newDir);
    }

    public List<PosixPath> listDir() {
        String eval = delegate.eval("ls -a");
        return eval.lines().map(PosixPath::ofPosix).collect(Collectors.toList());
    }

    public PosixPath makeDir(PosixPath newDir) {
        delegate.eval(String.format("mkdir %s", newDir));
        return newDir.isAbsolute() ? newDir : pwd().climb(newDir);
    }

    public boolean exists(PosixPath newDir) {
        return delegate.eval(String.format(
                "if [ -d %s ]; then\n" +
                        "  echo \"YES\"\n" +
                        "fi",
                newDir)).contains("YES");
    }

    public boolean isFolder(PosixPath newDir) {
        return delegate.eval(String.format(
                "if [ -d %s ]; then\n" +
                        "  echo \"YES\"\n" +
                        "fi",
                newDir
        )).contains("YES");
    }

    public boolean isFile(PosixPath newDir) {
        return exists(newDir) && !isFolder(newDir);
    }


    public PosixPath removeDir(PosixPath newDir) {
        delegate.eval(String.format("rm -rf %s", newDir));
        return newDir.isAbsolute() ? newDir : pwd().climb(newDir);
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

    public static void lockDir(TextTerminal bash, Runnable action) {
        var v = lockDir(bash, () -> {
            action.run();
            return 0;
        });
    }

}
