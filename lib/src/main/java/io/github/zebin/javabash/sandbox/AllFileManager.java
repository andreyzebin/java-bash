package io.github.zebin.javabash.sandbox;

public interface AllFileManager extends DirWalker, DirChanger, DirViewer, BashPower {

    default boolean remove(PosixPath path) {
        return this.dirExists(path) ? this.removeDir(path) : this.removeFile(path);
    }

    default boolean exists(PosixPath path) {
        return this.fileExists(path) || this.dirExists(path);
    }
}