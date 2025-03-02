package io.github.zebin.javabash.frontend;

public class BashCSS {

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
            bashTML.paint(f, Palette.YELLOW_BOLD);
        }

        bashTML.fillSurrounding("\"", Palette.MAGENTA);

        for (String f : new String[]{
                "\""}
        ) {
            bashTML.paint(f, Palette.RED_BRIGHT);
        }

        for (String f : new String[]{
                "false",
                "true"}
        ) {
            bashTML.paint(f, Palette.GREEN_BRIGHT);
        }

        return bashTML;
    }

    public static TextBrush stdRender(TextBrush bashTML) {
        return bashTML.fill(Palette.GREEN);
    }

    public static TextBrush stdErrRender(TextBrush textBrush) {
        return textBrush
                .fill(Palette.RED)
                .paint("ERROR", Palette.RED_BOLD_BRIGHT)
                .paint("FAIL", Palette.RED_BOLD_BRIGHT);
    }
}
