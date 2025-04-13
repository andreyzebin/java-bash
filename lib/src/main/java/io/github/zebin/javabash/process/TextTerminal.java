package io.github.zebin.javabash.process;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface TextTerminal {

    int exec(String comm, String mask, Consumer<String> stdout, Consumer<String> stderr);

    default int exec(String comm, Consumer<String> stdout, Consumer<String> stderr) {
        return exec(comm, comm, stdout, stderr);
    }

    default String eval(String comm) {
        StringBuilder stringBuilder = new StringBuilder();
        int exec = exec(comm, stringBuilder::append, stringBuilder::append);
        if (exec != 0) {
            throw new RuntimeException(String.format("Line '%s' failed with code: %d", comm, exec),
                    new RuntimeException("Outputs: " + stringBuilder));
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
}
