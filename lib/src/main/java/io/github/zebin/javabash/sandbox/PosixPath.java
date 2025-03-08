package io.github.zebin.javabash.sandbox;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PosixPath implements Iterable<PosixPath> {

    private final String[] segments;
    private final int start;
    private final int endInclusive;
    private final boolean isAbsolute;

    private PosixPath(int start, int endInclusive, boolean isAbsolute, String... segments) {
        this.segments = segments;
        this.start = start;
        this.endInclusive = endInclusive;
        this.isAbsolute = isAbsolute;
    }


    public Path toPath() {
        return length() > 0 ?
                (
                        isAbsolute ? Path.of(
                                File.separator,
                                Path.of(File.separator, segments).subpath(0, endInclusive + 1).toString()
                        ) :
                                Path.of("", segments).subpath(0, endInclusive + 1)
                ) :
                (
                        isAbsolute ? Path.of(File.separator) :
                                Path.of("")
                );
    }

    public int length() {
        return endInclusive - start + 1;
    }

    @Override
    public String toString() {
        return (isAbsolute ? "/" : "") + StreamSupport.stream(
                Arrays.spliterator(segments, start, endInclusive + 1),
                false
        ).collect(Collectors.joining("/"));
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

    public static PosixPath root() {
        return new PosixPath(0, -1, true);
    }

    public static PosixPath relate() {
        return new PosixPath(0, -1, false);
    }

    public PosixPath descend() {
        return new PosixPath(start, endInclusive - 1, isAbsolute, segments);
    }

    public static PosixPath ofPosix(String posix) {
        if (posix.startsWith("/")) {
            return PosixPath.root().climb(
                    Arrays.stream(posix.split("/")).skip(1)
                            .toArray(String[]::new)
            );
        }

        return PosixPath.relate().climb(posix.split("/"));
    }

    public boolean startsWith(PosixPath pp) {
        PosixPath temp = this;
        while (temp.length() > pp.length()) {
            temp = temp.descend();
        }

        return temp.equals(pp);
    }

    public boolean endsWith(PosixPath pp) {
        if (pp.isAbsolute()) {
            return false;
        }
        PosixPath temp = this;

        while (temp.length() + pp.length() > this.length()) {
            temp = temp.descend();
        }

        return temp.climb(pp).equals(this);
    }

    public static PosixPath of(Path path) {
        List<String> list = new ArrayList<>();
        path.iterator().forEachRemaining((k) -> {
            if (!k.toString().isBlank()) {
                list.add(k.toString());
            }
        });

        if (path.isAbsolute()) {
            return PosixPath.root().climb(
                    list.toArray(String[]::new)
            );
        }

        return PosixPath.relate().climb(
                list.toArray(String[]::new)
        );
    }

    public PosixPath climb(String... suffix) {
        for (String seg : suffix) {
            if (seg.contains("/") || seg.contains("\\")) {
                throw new IllegalArgumentException("Wrong argument: Segment must not contain path separators!");
            }
            if (seg.isBlank()) {
                throw new IllegalArgumentException("Wrong argument: Segment must not be blank!");
            }
            if (!seg.trim().equals(seg)) {
                throw new IllegalArgumentException("Wrong argument: Segment must not contain leading or trailing spaces!");
            }
        }

        String[] dest = new String[length() + suffix.length];
        System.arraycopy(segments, start, dest, 0, length());
        System.arraycopy(suffix, 0, dest, length(), suffix.length);

        return new PosixPath(0, length() + suffix.length - 1, isAbsolute, dest);
    }

    public PosixPath climb(PosixPath suffix) {
        if (suffix.isAbsolute) {
            throw new IllegalArgumentException("Wrong argument: Can not append another absolute path!");
        }
        return climb(suffix.segments);
    }

    public boolean hasMore() {
        return start < endInclusive + 1;
    }

    public boolean isAbsolute() {
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
        return toString().equals(posixPath.toString());
    }

    @Override
    public int hashCode() {
        return arrayHashCode(segments, start, endInclusive);
    }

    private static int arrayHashCode(Object[] a, int start, int end) {
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