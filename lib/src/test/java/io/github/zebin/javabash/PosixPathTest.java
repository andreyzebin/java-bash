package io.github.zebin.javabash;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

class PosixPathTest {

    @Test
    void testEmptyPAth() {
        PosixPath posixPath = PosixPath.of();

        Assertions.assertEquals(Path.of(""), posixPath.toRelativePath());
        Assertions.assertEquals("", posixPath.toString());
    }

    @Test
    void testSingleEmptySegment() {
        PosixPath posixPath = PosixPath.of("");

        Assertions.assertEquals(Path.of(""), posixPath.toRelativePath());
        Assertions.assertEquals("", posixPath.toString());
    }

    @Test
    void testSingleSegment() {
        PosixPath posixPath = PosixPath.of("abc");

        Assertions.assertEquals(Path.of("abc"), posixPath.toRelativePath());
        Assertions.assertEquals("abc", posixPath.toString());
    }

    @Test
    void testTwoSegments() {
        PosixPath posixPath = PosixPath.of("foo", "bar");

        Assertions.assertEquals(Path.of("foo", "bar"), posixPath.toRelativePath());
        Assertions.assertEquals("foo/bar", posixPath.toString());
    }

    @Test
    void testIterator() {
        Iterator<PosixPath> iterator = PosixPath.of("foo", "bar")
                .iterator();

        PosixPath next = null;
        List<PosixPath> seq = new ArrayList<>();
        while (iterator.hasNext()) {
            next = iterator.next();
            seq.add(next);
        }

        Assertions.assertIterableEquals(List.of(
                PosixPath.of("foo", "bar"),
                PosixPath.of("foo"),
                PosixPath.of()), seq);
        Assertions.assertEquals(Path.of(""), next.toRelativePath());
        Assertions.assertTrue(next.isRoot());
    }

    @Test
    void testDescend() {

        Assertions.assertIterableEquals(
                List.of(PosixPath.of("foo", "bar"),
                        PosixPath.of("foo"),
                        PosixPath.of()),
                PosixPath.of("foo", "bar")
                        .descendStream()
                        .collect(Collectors.toList())
        );
    }

    @Test
    void testDescendReverted() {

        List<PosixPath> descend = new ArrayList<>(PosixPath.of("foo", "bar")
                .descendStream()
                .map(pp -> pp.climb(PosixPath.of("obj.yaml")))
                .collect(Collectors.toList()));
        Collections.reverse(descend);

        Assertions.assertIterableEquals(
                List.of(PosixPath.of("obj.yaml"),
                        PosixPath.of("foo", "obj.yaml"),
                        PosixPath.of("foo", "bar", "obj.yaml")
                ),
                descend
        );
    }

    @Test
    void testDescendWithClimb() {

        Assertions.assertIterableEquals(
                List.of(PosixPath.of("foo", "bar", "obj.yaml"),
                        PosixPath.of("foo", "obj.yaml"),
                        PosixPath.of("obj.yaml")),
                PosixPath.of("foo", "bar")
                        .descendStream()
                        .map(pp -> pp.climb(PosixPath.of("obj.yaml")))
                        .collect(Collectors.toList())
        );
    }

}