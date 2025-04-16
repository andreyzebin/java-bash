package io.github.zebin.javabash.sandbox;

import java.io.Reader;
import java.io.Writer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * <p>Api to an abstract file system with a root path = ""
 * <p>Only relative {@link PosixPath} allowed
 * <p>
 * <p>
 * Use {@link #validate(PosixPath)} in implementation
 */
public interface DirectoryTree {

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

    Stream<PosixPath> list(PosixPath path);

    default <T> T setupDir(Supplier<T> result) {
        return result.get();
    }

    /**
     * Traverse tree of directories
     * <p> example uses:
     * <p> - all files
     * <p> - all dirs (dirsFirst)
     * <p> - all files x-level down
     * <p> - all files in dirs containing file marker (filesFirst)
     *
     * @param sayEnter if you want to enter directory
     */
    default void traverse(
            PosixPath start,
            // Stop stream elements in entered dir
            Function<PosixPath, Boolean> sayEnter
    ) {
        list(start)
                .filter(this::isDir)
                .filter(sayEnter::apply)
                .forEach(f -> traverse(f, sayEnter));

    }
}
