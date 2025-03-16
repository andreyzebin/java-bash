package io.github.zebin.javabash;

import io.github.zebin.javabash.sandbox.BashUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class BashUtilsTest {

    @Test
    void test() {
        Assertions.assertEquals("\"\"", BashUtils.encode(Path.of("")));
        Assertions.assertEquals("\"a/b\"", BashUtils.encode(Path.of("a", "b")));
        Assertions.assertTrue(BashUtils.encode(Path.of("a", "b").toAbsolutePath()).startsWith("\"/"));
    }

}