package io.github.zebin.javabash;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class PosixUtilsTest {

    @Test
    void test() {
        Assertions.assertEquals("\"\"", PosixUtils.encode(Path.of("")));
        Assertions.assertEquals("\"a/b\"", PosixUtils.encode(Path.of("a", "b")));
        Assertions.assertTrue(PosixUtils.encode(Path.of("a", "b").toAbsolutePath()).startsWith("\"/"));
    }

}