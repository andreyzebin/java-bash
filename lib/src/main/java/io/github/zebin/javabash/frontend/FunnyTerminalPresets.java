package io.github.zebin.javabash.frontend;

import io.github.zebin.javabash.frontend.brush.TerminalPalette;
import io.github.zebin.javabash.frontend.brush.TextBrush;

public class FunnyTerminalPresets {

    public static String bashRender(String cmd) {
        return bashHighlights(new TextBrush(cmd)).toString();
    }

    public static TextBrush bashHighlights(TextBrush bashTML) {
        for (String f : new String[]{
                "mkdir",
                "pwd",
                "rm -rf",
                "rm",
                "ssh-add",
                "cd ",
                "git clone",
                "git checkout",
                "git add",
                "git push",
                "git commit",
                "git "}
        ) {
            bashTML.paint(f, TerminalPalette.YELLOW_BOLD);
        }

        bashTML.fillSurrounding("\"", TerminalPalette.MAGENTA);

        for (String f : new String[]{
                "\""}
        ) {
            bashTML.paint(f, TerminalPalette.RED_BRIGHT);
        }

        for (String f : new String[]{
                "false",
                "true"}
        ) {
            bashTML.paint(f, TerminalPalette.GREEN_BRIGHT);
        }

        return bashTML;
    }

    public static TextBrush stdRender(TextBrush bashTML) {
        return bashTML.fill(TerminalPalette.GREEN);
    }

    public static TextBrush stdErrRender(TextBrush textBrush) {
        return textBrush
                .fill(TerminalPalette.RED)
                .paint("ERROR", TerminalPalette.RED_BOLD_BRIGHT)
                .paint("FAIL", TerminalPalette.RED_BOLD_BRIGHT);
    }
}
