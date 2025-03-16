package io.github.zebin.javabash.frontend;

import java.util.function.Consumer;

public class TextWriter implements AutoCloseable {

    private final StringBuilder sb = new StringBuilder();
    private final Consumer<String> lines;

    public TextWriter(Consumer<String> lines) {
        this.lines = lines;
    }

    public void println(String line) {
        print(line);
        newLine();
    }

    public void newLine() {
        print(System.lineSeparator());
    }

    public void print(String segment) {

        int fromIndex = 0;
        int indexOf = segment.indexOf(System.lineSeparator(), fromIndex);

        while (indexOf != -1) {
            sb.append(segment.substring(fromIndex, indexOf));
            lines.accept(sb.toString());
            sb.setLength(0);
            fromIndex = indexOf + 1;
            indexOf = segment.indexOf(System.lineSeparator(), fromIndex);
        }

        sb.append(segment.substring(fromIndex, segment.length()));

    }

    @Override
    public void close() throws Exception {
        if (!sb.isEmpty()) {
            lines.accept(sb.toString());
            sb.setLength(0);
        }
    }
}
