package io.github.zebin.javabash;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PosixPath implements Iterable<PosixPath> {

    private final String[] segments;
    private final int start;
    private final int end;

    private PosixPath(int start, int end, String... segments) {
        this.segments = segments;
        this.start = start;
        this.end = end;
    }


    public Path toRelativePath() {
        return end - start > 0 ? Path.of("", segments).subpath(start, end) : Path.of("");
    }

    public int length() {
        return end - start;
    }

    @Override
    public String toString() {
        return StreamSupport.stream(Arrays.spliterator(segments, start, end), false)
                .collect(Collectors.joining("/"));
    }

    @Override
    public Iterator<PosixPath> iterator() {
        return new Iterator<>() {
            private PosixPath it = PosixPath.this;

            @Override
            public boolean hasNext() {
                return it != null;
            }

            @Override
            public PosixPath next() {
                final PosixPath it1 = it;
                if (it1.hasMore()) {
                    it = it1.descend();
                } else {
                    it = null;
                }
                return it1;
            }
        };
    }

    public static PosixPath of(String... segments) {
        return new PosixPath(0, segments.length, segments);
    }

    public PosixPath descend() {
        return new PosixPath(start, end - 1, segments);
    }

    public PosixPath climb(PosixPath suffix) {
        String[] dest = new String[length() + suffix.length()];
        System.arraycopy(segments, start, dest, 0, length());
        System.arraycopy(suffix.segments, suffix.start, dest, length(), suffix.length());

        return PosixPath.of(dest);
    }

    public boolean hasMore() {
        return start < end;
    }

    public boolean isRoot() {
        return !hasMore();
    }

    public Stream<PosixPath> descendStream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PosixPath posixPath = (PosixPath) o;
        return Arrays.equals(posixPath.segments, posixPath.start, posixPath.end,
                this.segments, this.start, this.end);
    }

    @Override
    public int hashCode() {
        return arrayHashCode(segments, start, end);
    }

    public static int arrayHashCode(Object[] a, int start, int end) {
        if (a == null) {
            return 0;
        }

        int result = 1;

        for (int i = start; i < end; i++) {
            Object element = a[i];
            result = 31 * result + (element == null ? 0 : element.hashCode());
        }

        return result;
    }
}