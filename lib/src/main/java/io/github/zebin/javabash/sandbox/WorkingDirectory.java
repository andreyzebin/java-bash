package io.github.zebin.javabash.sandbox;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
public class WorkingDirectory implements DirectoryTree {

    private final FileManager delegate;
    private final Consumer<FileEvent> listener;

    public WorkingDirectory(
            FileManager delegate,
            Consumer<FileEvent> listener
    ) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public Writer put(PosixPath path) {
        validate(path);
        if (path.length() > 1) {
            delegate.makeDir(path.descend());
        }
        fireChange(path);
        return delegate.write(path);
    }

    private void fireChange(PosixPath path) {
        log.debug("File changed {}", path);
        listener.accept(FileEvent.builder()
                .type(FileEvent.FileEventType.CHANGED)
                .path(path).build());
    }

    @Override
    public boolean delete(PosixPath path) {
        validate(path);
        fireChange(path);
        return delegate.remove(path);
    }

    @Override
    public Writer patch(PosixPath path) {
        validate(path);
        if (path.length() > 1) {
            delegate.makeDir(path.descend());
        }
        fireChange(path);
        return delegate.append(path);
    }

    @Override
    public Reader get(PosixPath path) {
        validate(path);
        return new StringReader(delegate.read(path));
    }

    @Override
    public boolean exists(PosixPath path) {
        validate(path);
        return delegate.exists(path);
    }

    @Override
    public boolean isDir(PosixPath path) {
        validate(path);
        return delegate.dirExists(path);
    }

    @Override
    public int run(String cmd, Consumer<String> stdOut, Consumer<String> stdErr) {
        return delegate.run(cmd, stdOut, stdErr);
    }

    @Override
    public Stream<PosixPath> list(PosixPath path) {
        validate(path);
        return delegate.list(path).stream()
                .filter(p -> !(p.equals(PosixPath.CURRENT) || p.equals(PosixPath.LEVEL_UP)));
    }

    @Builder
    @Data
    public static class FileEvent {
        private final FileEventType type;
        private final PosixPath path;

        public enum FileEventType {
            CHANGED();
        }
    }
}
