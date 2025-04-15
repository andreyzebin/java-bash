package io.github.zebin.javabash.sandbox;

import io.github.zebin.javabash.process.TextTerminal;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FileManager implements DirWalker, DirChanger, DirViewer, BashPower {
    private final TextTerminal delegate;

    public FileManager(TextTerminal delegate) {
        this.delegate = delegate;
    }

    @Override
    public PosixPath getCurrent() {
        return PosixPath.ofPosix(delegate.eval("pwd"));
    }

    @Override
    public PosixPath goUp() {
        return go(PosixPath.LEVEL_UP);
    }

    @Override
    public PosixPath go(PosixPath path) {
        delegate.eval(String.format("cd %s", path));
        return getCurrent();
    }

    @Override
    public TextTerminal getTerminal() {
        return delegate;
    }

    @Override
    public String read(PosixPath pp) {
        return delegate.eval(String.format("cat %s", pp));
    }

    @Override
    public Writer write(PosixPath pp) {
        return new StringWriter() {
            @Override
            public void close() throws IOException {
                delegate.eval(String.format("echo %s > %s", BashUtils.escape(toString()), pp));
                super.close();
            }
        };
    }

    @Override
    public Writer append(PosixPath pp) {
        return new StringWriter() {
            @Override
            public void close() throws IOException {
                delegate.eval(String.format("echo %s >> %s", BashUtils.escape(toString()), pp));
                super.close();
            }
        };
    }

    @Override
    public PosixPath makeFile(PosixPath newDir) {
        delegate.eval(String.format("touch %s", newDir));
        return newDir.isAbsolute() ? newDir : getCurrent().climb(newDir);
    }

    @Override
    public List<PosixPath> list() {
        return list(getCurrent());
    }

    @Override
    public List<PosixPath> list(PosixPath path) {
        String eval = delegate.eval(String.format("ls -A %s", path));
        return eval.lines().map(path::climb).collect(Collectors.toList());
    }

    @Override
    public PosixPath makeDir(PosixPath newDir) {
        delegate.eval(String.format("mkdir -p %s", newDir));
        return newDir.isAbsolute() ? newDir : getCurrent().climb(newDir);
    }

    @Override
    public boolean dirExists(PosixPath newDir) {
        return delegate.eval(String.format("if [ -d %s ]; then echo \"YES\"; else echo \"NO\"; fi", newDir))
                .contains("YES");
    }

    @Override
    public boolean exists(PosixPath path) {
        return fileExists(path) || dirExists(path);
    }

    @Override
    public boolean fileExists(PosixPath newDir) {
        return delegate.eval(String.format("if [ -f %s ]; then echo \"YES\"; else echo \"NO\"; fi", newDir))
                .contains("YES");
    }

    @Override
    public boolean removeDir(PosixPath newDir) {
        String eval = delegate.eval(String.format("rm -vrf %s", newDir));
        return eval.lines().findAny().isPresent();
    }

    @Override
    public boolean remove(PosixPath path) {
        if (dirExists(path)) {
            return removeDir(path);
        }

        return removeFile(path);
    }

    @Override
    public boolean removeFile(PosixPath file) {
        StringBuilder err = new StringBuilder();
        StringBuilder std = new StringBuilder();
        int eval = delegate.exec(String.format("rm -vf %s", file), std::append, err::append);
        if (eval != 0) {
            throw new RuntimeException(String.format("Could not remove file %s, because %s", file, err));
        }

        return std.toString().lines().findAny().isPresent();
    }


}
