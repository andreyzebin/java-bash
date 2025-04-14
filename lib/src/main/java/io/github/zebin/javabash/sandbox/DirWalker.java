package io.github.zebin.javabash.sandbox;

public interface DirWalker {

    PosixPath getCurrent();
    PosixPath goUp();
    PosixPath go(PosixPath path);
}
