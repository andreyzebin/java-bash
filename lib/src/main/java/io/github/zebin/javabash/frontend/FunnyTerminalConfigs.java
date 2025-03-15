package io.github.zebin.javabash.frontend;

import io.github.zebin.javabash.frontend.brush.TerminalPalette;
import io.github.zebin.javabash.frontend.brush.TextBrush;
import io.github.zebin.javabash.frontend.brush.TextShrink;
import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.util.function.Function;

@Builder
@Data
@With
public class FunnyTerminalConfigs {

    public static final FunnyTerminalConfigs DEFAULT = getDefault();

    @Builder.Default
    private Function<String, String> stderr = s -> s;

    @Builder.Default
    private Function<String, String> stdout = s -> s;
    @Builder.Default
    private Function<String, String> stdin = s -> s;
    @Builder.Default
    private Function<String, String> user = s -> s;
    @Builder.Default
    private Function<String, String> dir = s -> s;
    @Builder.Default
    private Function<String, String> id = s -> s;
    @Builder.Default
    private Function<String, String> cmd = s -> s;


    public static FunnyTerminalConfigs getDefault() {
        FunnyTerminal.ColorPool defaults = FunnyTerminal.ColorPool.defaults();
        FunnyTerminal.ColorPool defaults2 = FunnyTerminal.ColorPool.defaults();
        return builder()
                .dir(
                        s -> new TextBrush(TextShrink.getShrinkDir(s, 30))
                                .fill(defaults2.getColor(TextShrink.getShrinkDir(s, 30)))
                                .toString()
                )
                .stderr(s -> stdErrRender(new TextBrush(s)).toString())
                .stdout(s -> stdRender(new TextBrush(s)).toString())
                .stdin(s -> s)
                .id(s -> new TextBrush(s).fill(defaults.getColor(s)).toString())
                .cmd(cmd1 -> bashHighlights(new TextBrush(cmd1)).toString())
                .user(s -> new TextBrush(s).fill(TerminalPalette.MAGENTA).toString())
                .build();
    }

    public static TextBrush bashHighlights(TextBrush bashTML) {
        for (String f : new String[]{
                "mkdir ",
                "pwd ",
                "rm ",
                "ssh-add ",
                "cd ",
                "ls ",
                "git clone ",
                "git checkout ",
                "git add ",
                "git push ",
                "git commit ",
                "git ",
                "if ",
                " fi",
                "then ",
                "else ",
                "echo ",
                "touch "}
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
