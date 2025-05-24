package io.github.zebin.javabash.sandbox;

import java.util.List;

public interface DirViewer {
    String read(PosixPath pp);

    List<PosixPath> list();

    List<PosixPath> list(PosixPath path);

    boolean exists(PosixPath path);

    boolean dirExists(PosixPath newDir);

    boolean fileExists(PosixPath newDir);

    boolean socketExists(PosixPath newDir);
}
