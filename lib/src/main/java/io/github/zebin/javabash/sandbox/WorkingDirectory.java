package io.github.zebin.javabash.sandbox;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
public class WorkingDirectory implements DirectoryTree {

    private final AllFileManager delegate;
    private final Consumer<FileEvent> listener;
    private final PosixPath wd;

    public WorkingDirectory(
            AllFileManager delegate,
            PosixPath wd,
            Consumer<FileEvent> listener
    ) {
        this.delegate = delegate;
        this.listener = listener;
        this.wd = wd;
    }

    @Override
    public Writer put(PosixPath path) {
        return setupDir(() -> {
            validate(path);
            return new StringWriter() {
                @Override
                public void close() throws IOException {
                    setupDir(() -> {
                        validate(path);
                        if (path.length() > 1) {
                            delegate.makeDir(path.descend());
                        }
                        fireChange(path);
                        Writer wr = delegate.write(path);
                        try {
                            wr.write(this.toString());
                            wr.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        return 0;
                    });
                }
            };
        });
    }

    private void fireChange(PosixPath path) {
        log.debug("File changed {}", path);
        listener.accept(
                FileEvent.builder()
                        .type(FileEvent.FileEventType.CHANGED)
                        .path(path).build()
        );
    }

    @Override
    public boolean delete(PosixPath path) {
        return setupDir(() -> {
            validate(path);
            fireChange(path);
            return delegate.remove(path);
        });
    }

    @Override
    public Writer patch(PosixPath path) {
        return setupDir(() -> {
            validate(path);
            return new StringWriter() {
                @Override
                public void close() throws IOException {
                    setupDir(() -> {
                        validate(path);
                        if (path.length() > 1) {
                            delegate.makeDir(path.descend());
                        }
                        fireChange(path);
                        Writer wr = delegate.append(path);
                        try {
                            wr.append(this.toString());
                            wr.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        return 0;
                    });
                }
            };
        });
    }

    @Override
    public Reader get(PosixPath path) {
        return setupDir(() -> {
            validate(path);
            return new StringReader(delegate.read(path));
        });
    }

    @Override
    public boolean exists(PosixPath path) {
        return setupDir(() -> {
            validate(path);
            return delegate.exists(path);
        });
    }

    @Override
    public boolean isDir(PosixPath path) {
        return setupDir(() -> {
            validate(path);
            return delegate.dirExists(path);
        });
    }

    @Override
    public Stream<PosixPath> list(PosixPath path) {
        return setupDir(() -> {
            validate(path);
            return delegate.list(path)
                    .stream()
                    .filter(p -> !(p.equals(PosixPath.CURRENT) || p.equals(PosixPath.LEVEL_UP)));
        });
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

    public <T> T setupDir(Supplier<T> result) {
        PosixPath pwd = delegate.getCurrent();
        log.debug("File manager state saved.");
        try {
            if (!pwd.equals(wd)) {
                delegate.makeDir(wd);
                delegate.go(wd);
            }
            return result.get();
        } finally {
            if (!pwd.equals(wd)) {
                delegate.go(pwd);
            }
            log.debug("File manager state recovered.");
        }
    }
}
