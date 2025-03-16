package io.github.zebin.javabash.sandbox;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SandboxFilesImpl implements SandboxFiles {

    private final FileManager delegate;

    public SandboxFilesImpl(FileManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public Writer put(PosixPath path) {
        validate(path);
        return delegate.write(path);
    }

    @Override
    public boolean delete(PosixPath path) {
        validate(path);
        return delegate.remove(path);
    }

    @Override
    public Writer patch(PosixPath path) {
        validate(path);
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
}
