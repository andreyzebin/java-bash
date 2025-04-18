package io.github.zebin.javabash.sandbox;

import com.google.common.collect.Streams;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PosixPath {

    public static final PosixPath CURRENT = ofPosix(".");
    public static final PosixPath LEVEL_UP = ofPosix("..");
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
        if (BashUtils.isWindows() && isAbsolute()) {
            PosixPath disk = this;
            while (disk.length() > 1) {
                disk = disk.descend();
            }
            return Path.of(disk.getEnd().toUpperCase() + ":\\")
                    .resolve(relativize(disk).toString().replace("/", "\\"));
        }
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


    public Iterator<PosixPath> iterateDescending() {
        return new Iterator<>() {
            private PosixPath it = PosixPath.this;

            @Override
            public boolean hasNext() {
                return it != null;
            }

            @Override
            public PosixPath next() {
                final PosixPath it1 = it;
                if (it1.hasSegments()) {
                    it = it1.descend();
                } else {
                    it = null;
                }
                return it1;
            }
        };
    }

    public Stream<PosixPath> streamDescending() {
        return Streams.stream(iterateDescending());
    }

    public Stream<PosixPath> streamClimbing() {
        return Streams.stream(iterateClimbing());
    }

    private Iterator<PosixPath> iterateClimbing() {
        return streamDescending()
                .collect(Collectors.toCollection(LinkedList::new))
                .descendingIterator();
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

    public String getEnd() {
        if (length() > 0) {
            return segments[endInclusive];
        }
        return null;
    }

    public static PosixPath ofPosix(String posix) {
        if (posix.startsWith("/")) {
            return PosixPath.root().climb(
                    Arrays.stream(posix.split("/")).skip(1)
                            .filter(s -> !s.isBlank())
                            .toArray(String[]::new)
            );
        }

        return PosixPath.relate().climb(
                Arrays.stream(posix.split("/"))
                        .filter(s -> !s.isBlank())
                        .toArray(String[]::new));
    }

    public boolean startsWith(PosixPath pp) {
        PosixPath temp = this;
        while (temp.length() > pp.length()) {
            temp = temp.descend();
        }

        return temp.equals(pp);
    }

    public boolean contains(PosixPath pp) {
        PosixPath temp = this;

        while (temp.length() > 0) {
            if (temp.endsWith(pp)) {
                return true;
            }

            temp = temp.descend();
        }

        return false;
    }

    public boolean endsWith(PosixPath pp) {
        if (pp.length() > length()) {
            return false;
        }

        // This check saves from using climb with absolute
        if (pp.isAbsolute()) {
            return pp.equals(this);
        }


        PosixPath temp = this;

        while (temp.length() + pp.length() > this.length()) {
            temp = temp.descend();
        }

        return temp.climb(pp).equals(this);
    }

    public PosixPath elevate() {
        return new PosixPath(start + 1, endInclusive, false, segments);
    }

    public PosixPath relativize(PosixPath root) {
        if (!startsWith(root)) {
            throw new IllegalArgumentException(String.format(
                    "Could not relativize, because: given <root>: %s is not what <this>: %s starts with", root, this
            ));
        }
        PosixPath temp = this;
        for (int i = 0; i < root.length(); i++) {
            temp = temp.elevate();
        }

        return temp;
    }

    public static PosixPath of(Path path) {
        if (BashUtils.isWindows() && path.isAbsolute()) {
            Path disk = path.getRoot();
            return PosixPath.ofPosix("/" + disk.toString().toLowerCase()
                            .replace("\\", "")
                            .replace(":", ""))
                    .climb(PosixPath.of(disk.relativize(path)));
        }

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

    private boolean hasSegments() {
        return start < endInclusive + 1;
    }

    public boolean isAbsolute() {
        return isAbsolute;
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