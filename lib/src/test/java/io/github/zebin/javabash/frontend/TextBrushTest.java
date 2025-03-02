package io.github.zebin.javabash.frontend;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TextBrushTest {

    @Test
    void test() {

        String string = new TextBrush("cd \"dir\"")
                .paint("\"", Palette.RED_BRIGHT)
                .toString();
        System.out.println(string);
        Assertions.assertEquals("cd \u001B[0;91m\"\u001B[0mdir\u001B[0;91m\"\u001B[0m", string);

        String string1 = new TextBrush("cd \"dir\"")
                .fillSurrounding("\"", Palette.GREEN_BACKGROUND)
                .toString();

        System.out.println(string1);
        Assertions.assertEquals("cd \"\u001B[42mdir\u001B[0m\"", string1);

    }

    @Test
    void test1() {

        String string4 = new TextBrush("cd \"dir\"")
                .fillSurrounding("\"", Palette.GREEN_BACKGROUND)
                .paint("\"", Palette.RED_BOLD)
                .toString();

        System.out.println(string4);
        Assertions.assertEquals("cd \u001B[1;31m\"\u001B[0m\u001B[42mdir\u001B[0m\u001B[1;31m\"\u001B[0m", string4);

    }

    @Test
    void test2() {

        String string3 = new TextBrush("\"")
                .paint("\"", Palette.GREEN_BACKGROUND)
                .toString();

        System.out.println(string3);
        Assertions.assertEquals("\u001B[42m\"\u001B[0m", string3);

    }

    @Test
    void test3() {

        String string3 = new TextBrush("JSON")
                .paint("JSON", Palette.GREEN_BACKGROUND)
                .paint("ON", Palette.YELLOW)
                .toString();

        System.out.println(string3);
        Assertions.assertEquals("\u001B[42mJS\u001B[0m\u001B[0;33mON\u001B[0m", string3);

    }

    @Test
    void test4() {

        String string3 = new TextBrush("")
                .fill(Palette.RED)
                .paint("ERROR", Palette.GREEN_BACKGROUND)
                .toString();

        System.out.println(string3);
        Assertions.assertEquals("", string3);

    }

}