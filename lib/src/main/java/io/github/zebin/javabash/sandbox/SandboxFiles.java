package io.github.zebin.javabash.sandbox;

import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * <p>Api to an abstract file system with a root path = ""
 * <p>Only relative {@link PosixPath} allowed
 * <p>
 * <p>
 * Use {@link #validate(PosixPath)} in implementation
 */
public interface SandboxFiles {

    default void validate(PosixPath path) {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException(String.format("Invalid path %s, must use relative only", path));
        }
    }

    Writer put(PosixPath path);

    boolean delete(PosixPath path);

    Writer patch(PosixPath path);

    Reader get(PosixPath path);

    boolean exists(PosixPath path);

    boolean isDir(PosixPath path);

    int run(String cmd, Consumer<String> stdOut, Consumer<String> stdErr);

    Stream<PosixPath> list(PosixPath path);

    /**
     * <p> example uses:
     * <p> - all files
     * <p> - all dirs (dirsFirst)
     * <p> - all files x-level down
     * <p> - all files in dirs containing file marker (filesFirst)
     *
     * @param sayEnter must return true to enter directory
     * @param sayBreak if return true then exit dir
     */
    default void traverse(
            PosixPath start,
            // enter dir
            Function<PosixPath, Boolean> sayEnter,
            // stop stream elements in entered dir
            Function<PosixPath, Boolean> sayBreak
    ) {
        AtomicBoolean nextTime = new AtomicBoolean(true);

        list(start)
                .filter(this::isDir)
                .takeWhile(t -> {
                    boolean thisTime = nextTime.get();
                    nextTime.set(!sayBreak.apply(t));
                    return thisTime;
                })
                .filter(sayEnter::apply)
                .forEach(f -> traverse(f, sayEnter, sayBreak));

    }
}
