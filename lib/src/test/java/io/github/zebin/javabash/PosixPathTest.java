package io.github.zebin.javabash;

import io.github.zebin.javabash.sandbox.PosixPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

class PosixPathTest {

    @Test
    void testEmptyPAth() {
        PosixPath posixPath = PosixPath.relate().climb();

        Assertions.assertEquals(Path.of(""), posixPath.toPath());
        Assertions.assertEquals("", posixPath.toString());
    }

    @Test
    void testStartsWith() {
        Assertions.assertTrue(
                PosixPath.ofPosix("/abc/def").startsWith(PosixPath.ofPosix("/abc"))
        );
        Assertions.assertFalse(
                PosixPath.ofPosix("/abc/def").startsWith(PosixPath.ofPosix("/abc1"))
        );

        Assertions.assertTrue(
                PosixPath.ofPosix("/abc/def/hij").endsWith(PosixPath.ofPosix("def/hij"))
        );
        Assertions.assertFalse(
                PosixPath.ofPosix("/abc/def/hij").endsWith(PosixPath.ofPosix("def/hij1"))
        );
    }

    @Test
    void testOfPosix() {
        Assertions.assertEquals("/abc/def", PosixPath.ofPosix("/abc/def").toString());
        Assertions.assertEquals("abc/def", PosixPath.ofPosix("abc/def").toString());
    }

    @Test
    void testOfPath() {
        Assertions.assertEquals(
                "",
                PosixPath.of(Path.of("")).toString()
        );
        Assertions.assertEquals(
                "/",
                PosixPath.of(Path.of("/".replace("/", File.separator))).toString()
        );
        Assertions.assertEquals(
                "/abc/def",
                PosixPath.of(Path.of("/abc/def".replace("/", File.separator))).toString()
        );
        Assertions.assertEquals(
                "abc/def",
                PosixPath.of(Path.of("abc/def".replace("/", File.separator))).toString()
        );
    }

    @Test
    void testRootPAth() {
        PosixPath posixPath = PosixPath.root();

        Assertions.assertEquals(Path.of(File.separator), posixPath.toPath());
        Assertions.assertEquals("/", posixPath.toString());
    }

    @Test
    void testRootPAthAbc() {
        PosixPath posixPath = PosixPath.root().climb("abc");

        Assertions.assertEquals(Path.of(File.separator, "abc"), posixPath.toPath());
        Assertions.assertEquals("/abc", posixPath.toString());
    }


    @Test
    void testSingleEmptySegment() {
        PosixPath posixPath = PosixPath.relate();

        Assertions.assertEquals(Path.of(""), posixPath.toPath());
        Assertions.assertEquals("", posixPath.toString());
    }

    @Test
    void testSingleSegment() {
        PosixPath posixPath = PosixPath.relate().climb("abc");

        Assertions.assertEquals(Path.of("abc"), posixPath.toPath());
        Assertions.assertEquals("abc", posixPath.toString());
    }

    @Test
    void testTwoSegments() {
        PosixPath posixPath = PosixPath.relate().climb("foo", "bar");

        Assertions.assertEquals(Path.of("foo", "bar"), posixPath.toPath());
        Assertions.assertEquals("foo/bar", posixPath.toString());
    }

    @Test
    void testIterator() {
        Iterator<PosixPath> iterator = PosixPath.relate().climb("foo", "bar")
                .iterator();

        PosixPath next = null;
        List<PosixPath> seq = new ArrayList<>();
        while (iterator.hasNext()) {
            next = iterator.next();
            seq.add(next);
        }

        Assertions.assertIterableEquals(
                List.of(
                        PosixPath.relate().climb("foo", "bar"),
                        PosixPath.relate().climb("foo"),
                        PosixPath.relate().climb()
                ),
                seq
        );

        Assertions.assertEquals(Path.of(""), next.toPath());
        Assertions.assertTrue(next.isAbsolute());
    }

    @Test
    void testDescend() {

        Assertions.assertIterableEquals(
                List.of(PosixPath.relate().climb("foo", "bar"),
                        PosixPath.relate().climb("foo"),
                        PosixPath.relate().climb()),
                PosixPath.relate().climb("foo", "bar")
                        .descendStream()
                        .collect(Collectors.toList())
        );
    }

    @Test
    void testDescendReverted() {

        List<PosixPath> descend = new ArrayList<>(PosixPath.relate().climb("foo", "bar")
                .descendStream()
                .map(pp -> pp.climb(PosixPath.relate().climb("obj.yaml")))
                .collect(Collectors.toList()));
        Collections.reverse(descend);

        Assertions.assertIterableEquals(
                List.of(PosixPath.relate().climb("obj.yaml"),
                        PosixPath.relate().climb("foo", "obj.yaml"),
                        PosixPath.relate().climb("foo", "bar", "obj.yaml")
                ),
                descend
        );
    }

    @Test
    void testDescendWithClimb() {

        Assertions.assertIterableEquals(
                List.of(PosixPath.relate().climb("foo", "bar", "obj.yaml"),
                        PosixPath.relate().climb("foo", "obj.yaml"),
                        PosixPath.relate().climb("obj.yaml")),
                PosixPath.relate().climb("foo", "bar")
                        .descendStream()
                        .map(pp -> pp.climb(PosixPath.relate().climb("obj.yaml")))
                        .collect(Collectors.toList())
        );
    }

}