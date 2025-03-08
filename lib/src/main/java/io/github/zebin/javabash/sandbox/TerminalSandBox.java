package io.github.zebin.javabash.sandbox;

import io.github.zebin.javabash.process.TextTerminal;

import java.util.function.Supplier;

public class TerminalSandBox {

    private final TextTerminal delegate;

    public TerminalSandBox(TextTerminal delegate) {
        this.delegate = delegate;
    }

    public PosixPath pwd() {
        return PosixPath.ofPosix(delegate.eval("pwd"));
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
