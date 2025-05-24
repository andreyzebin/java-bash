package io.github.zebin.javabash.sandbox;

import java.io.Writer;

public interface DirChanger {

    Writer write(PosixPath pp);

    Writer append(PosixPath pp);

    PosixPath makeFile(PosixPath newDir);

    PosixPath makeDir(PosixPath newDir);

    boolean removeDir(PosixPath newDir);

    boolean remove(PosixPath path);

    /**
     * Files and Sockets
     */
    boolean removeFile(PosixPath file);
}