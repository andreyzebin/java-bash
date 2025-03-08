package io.github.zebin.javabash.frontend;

import io.github.zebin.javabash.frontend.brush.TextShrink;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TextShrinkTest {


    @Test
    void test() {
        Assertions.assertEquals("/aaaa/bbbb/cccc",
                TextShrink.getShrinkDir("/aaaa/bbbb/cccc", 20));
        Assertions.assertEquals("aaaa/bbbb/cccc",
                TextShrink.getShrinkDir("aaaa/bbbb/cccc", 14));
        Assertions.assertEquals("aaaa/b../cccc",
                TextShrink.getShrinkDir("aaaa/bbbb/cccc", 13));
    }

    @Test
    void test1() {
        Assertions.assertEquals("aaaa//cccc",
                TextShrink.getShrinkDir("aaaa/bbbb/cccc", 12));
        Assertions.assertEquals("aaaa//cccc",
                TextShrink.getShrinkDir("aaaa/bbbb/cccc", 11));
        Assertions.assertEquals("aaaa//cccc",
                TextShrink.getShrinkDir("aaaa/bbbb/cccc", 10));
        Assertions.assertEquals("a..//cccc",
                TextShrink.getShrinkDir("aaaa/bbbb/cccc", 9));
        Assertions.assertEquals("//cccc",
                TextShrink.getShrinkDir("aaaa/bbbb/cccc", 8));
        Assertions.assertEquals("//cccc",
                TextShrink.getShrinkDir("aaaa/bbbb/cccc", 7));
        Assertions.assertEquals("//cccc",
                TextShrink.getShrinkDir("aaaa/bbbb/cccc", 6));
        Assertions.assertEquals("//c..",
                TextShrink.getShrinkDir("aaaa/bbbb/cccc", 5));
        Assertions.assertEquals("//",
                TextShrink.getShrinkDir("aaaa/bbbb/cccc", 4));
        Assertions.assertEquals("//",
                TextShrink.getShrinkDir("aaaa/bbbb/cccc", 1));
    }

}