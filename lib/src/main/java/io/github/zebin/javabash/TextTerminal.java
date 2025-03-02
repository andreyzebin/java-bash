package io.github.zebin.javabash;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public interface TextTerminal {

    void setTimeoutMillis(long timeoutMillis);

    int exec(String comm, String mask, Consumer<String> stdout, Consumer<String> stderr);

    default int exec(String comm, Consumer<String> stdout, Consumer<String> stderr) {
        return exec(comm, comm, stdout, stderr);
    }

    default String eval(String comm) {
        StringBuilder stringBuilder = new StringBuilder();
        int exec = exec(comm, stringBuilder::append, stringBuilder::append);
        if (exec != 0) {
            throw new RuntimeException(String.format("Could not evaluate '%s', returned non-zero: " + exec, comm));
        }
        return stringBuilder.toString().lines().collect(Collectors.joining(System.lineSeparator()));
    }

    default String eval(String comm, String mask) {
        StringBuilder stringBuilder = new StringBuilder();
        int exec = exec(comm, mask, stringBuilder::append, stringBuilder::append);
        if (exec != 0) {
            throw new RuntimeException(String.format("Could not evaluate '%s', returned non-zero: " + exec, mask));
        }
        return stringBuilder.toString().lines().collect(Collectors.joining(System.lineSeparator()));
    }

    default void secret(String key, String value) {
        eval(String.format("%s=%s", key, value), String.format("%s=***", key));
    }

    static <T> T transactionalDir(TextTerminal bash, Supplier<T> result) {
        String pwd = bash.eval("pwd");
        try {
            return result.get();
        } finally {
            bash.eval("cd " + pwd);
        }
    }

    static void transactionalDir(TextTerminal bash, Runnable action) {
        String pwd = bash.eval("pwd");
        try {
            action.run();
        } finally {
            bash.eval("cd " + pwd);
        }
    }
}
