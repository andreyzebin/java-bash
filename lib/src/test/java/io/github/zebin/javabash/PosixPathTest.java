package io.github.zebin.javabash;

import com.google.common.collect.Streams;
import io.github.zebin.javabash.sandbox.PosixPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
class PosixPathTest {

    @Test
    void testEmptyPAth() {
        PosixPath posixPath = PosixPath.relate().climb();

        Assertions.assertEquals(Path.of(""), posixPath.toPath());
        Assertions.assertEquals("", posixPath.toString());
        Assertions.assertFalse(posixPath.isAbsolute());
    }

    @Test
    void testContains() {
        Assertions.assertFalse(
                PosixPath.ofPosix("/abc/def/hij").contains(PosixPath.ofPosix("def/hij1"))
        );
    }

    @Test
    void testRelativize() {
        Assertions.assertEquals(PosixPath.ofPosix("def"),
                PosixPath.ofPosix("/abc/def").relativize(PosixPath.ofPosix("/abc")));
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
                PosixPath.ofPosix("/abc/def").contains(PosixPath.ofPosix("/abc"))
        );
        Assertions.assertFalse(
                PosixPath.ofPosix("/abc/def").contains(PosixPath.ofPosix("/abc1"))
        );
        Assertions.assertTrue(
                PosixPath.ofPosix("/abc/def").contains(PosixPath.ofPosix("abc"))
        );


        Assertions.assertTrue(
                PosixPath.ofPosix("/abc/def/hij").endsWith(PosixPath.ofPosix("def/hij"))
        );
        Assertions.assertFalse(
                PosixPath.ofPosix("/abc/def/hij").endsWith(PosixPath.ofPosix("def/hij1"))
        );

        Assertions.assertTrue(
                PosixPath.ofPosix("/abc/def/hij").contains(PosixPath.ofPosix("def/hij"))
        );
        Assertions.assertFalse(
                PosixPath.ofPosix("/abc/def/hij").contains(PosixPath.ofPosix("/hij"))
        );


        Assertions.assertTrue(
                PosixPath.ofPosix("/abc/def/hij").contains(PosixPath.ofPosix("def"))
        );
    }

    @Test
    void testOfPosix() {
        Assertions.assertEquals("/abc/def", PosixPath.ofPosix("/abc/def").toString());
        Assertions.assertEquals("abc/def", PosixPath.ofPosix("abc/def").toString());
    }

    @Test
    void testEnd() {
        Assertions.assertEquals("def", PosixPath.ofPosix("/abc/def").getEnd());
        Assertions.assertEquals("abc", PosixPath.ofPosix("abc/def").descend().getEnd());
        Assertions.assertNull(PosixPath.ofPosix("abc/def").descend().descend().getEnd());
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
        Assertions.assertTrue(posixPath.isAbsolute());
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
    void testIteratorBackwards() {
        Iterator<PosixPath> iterator = PosixPath.relate().climb("foo", "bar")
                .iterateDescending();

        List<PosixPath> found = new LinkedList<>();

        Iterator<PosixPath> backwardsIterator = Streams.stream(iterator)
                .collect(Collectors.toCollection(LinkedList::new))
                .descendingIterator();
        Streams.stream(backwardsIterator).forEach(pp -> {
            found.add(pp.climb("conf.properties"));
        });

        found.stream().map(PosixPath::toString).forEach(log::info);
    }

    @Test
    void testIteratorBackwardsAbs() {
        List<String> found = new LinkedList<>();
        PosixPath.root().climb("foo", "bar")
                .streamClimbing()
                .forEach(pp -> found.add(pp.climb("conf.properties").toString()));

        Assertions.assertIterableEquals(
                List.of("/conf.properties",
                        "/foo/conf.properties",
                        "/foo/bar/conf.properties"),
                found
        );
        found.forEach(log::info);
    }

    @Test
    void testIterator() {
        Iterator<PosixPath> iterator = PosixPath.relate().climb("foo", "bar")
                .iterateDescending();


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
        Assertions.assertFalse(next.isAbsolute());
    }

}