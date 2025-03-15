package io.github.zebin.javabash.sandbox;

import io.github.zebin.javabash.process.TextTerminal;

import java.util.List;
import java.util.stream.Collectors;

public class FileManager {

    private final TextTerminal delegate;

    public FileManager(TextTerminal delegate) {
        this.delegate = delegate;
    }

    public PosixPath getCurrent() {
        return PosixPath.ofPosix(delegate.eval("pwd"));
    }

    public PosixPath goUp() {
            return go(PosixPath.LEVEL_UP);
    }

    public PosixPath go(PosixPath path) {
        delegate.eval(String.format("cd %s", path));
        return getCurrent();
    }

    public String read(PosixPath pp) {
        return delegate.eval(String.format("cat %s", pp));
    }

    public PosixPath makeFile(PosixPath newDir) {
        delegate.eval(String.format("touch %s", newDir));
        return newDir.isAbsolute() ? newDir : getCurrent().climb(newDir);
    }

    public List<PosixPath> list() {
        return list(getCurrent());
    }

    public List<PosixPath> list(PosixPath path) {
        String eval = delegate.eval(String.format("ls -a %s", path));
        return eval.lines().map(PosixPath::ofPosix).collect(Collectors.toList());
    }

    public PosixPath makeDir(PosixPath newDir) {
        delegate.eval(String.format("mkdir %s", newDir));
        return newDir.isAbsolute() ? newDir : getCurrent().climb(newDir);
    }

    public boolean dirExists(PosixPath newDir) {
        return delegate.eval(String.format("if [ -d %s ]; then echo \"YES\"; else echo \"NO\"; fi", newDir))
                .contains("YES");
    }

    public boolean exists(PosixPath path) {
        return fileExists(path) || dirExists(path);
    }

    public boolean fileExists(PosixPath newDir) {
        return delegate.eval(String.format("if [ -f %s ]; then echo \"YES\"; else echo \"NO\"; fi", newDir))
                .contains("YES");
    }

    public boolean isFolder(PosixPath newDir) {
        return dirExists(newDir);
    }

    public boolean isFile(PosixPath newDir) {
        return exists(newDir) && !isFolder(newDir);
    }

    public PosixPath removeDir(PosixPath newDir) {
        delegate.eval(String.format("rm -rf %s", newDir));
        return newDir.isAbsolute() ? newDir : getCurrent().climb(newDir);
    }

    public PosixPath remove(PosixPath path) {
        if (!exists(path)) {
            throw new IllegalArgumentException(String.format("Cannot remove, because object is missing %s", path));
        }
        if (isFolder(path)) {
            return removeDir(path);
        }
        if (isFile(path)) {
            return removeFile(path);
        }

        throw new IllegalArgumentException(String.format("Unknown object %s", path));
    }

    public PosixPath removeFile(PosixPath newDir) {
        if (isFolder(newDir)) {
            throw new IllegalArgumentException(
                    String.format("Wanted to remove file %s, while actually it is a folder!", newDir)
            );
        }
        delegate.eval(String.format("rm %s", newDir));
        return newDir.isAbsolute() ? newDir : getCurrent().climb(newDir);
    }

}
