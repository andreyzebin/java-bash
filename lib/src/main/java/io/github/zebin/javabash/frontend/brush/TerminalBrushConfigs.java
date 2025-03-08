package io.github.zebin.javabash.frontend.brush;

import io.github.zebin.javabash.frontend.TerminalBrushedProxy;
import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.util.function.Function;

@Builder
@Data
@With
public class TerminalBrushConfigs {

    public static final TerminalBrushConfigs DEFAULT = getDefault();

    public static TerminalBrushConfigs getDefault() {
        TerminalBrushedProxy.ColorPool defaults = TerminalBrushedProxy.ColorPool.defaults();
        TerminalBrushedProxy.ColorPool defaults2 = TerminalBrushedProxy.ColorPool.defaults();
        return builder()
                .dir(
                        s -> new TextBrush(TextShrink.getShrinkDir(s, 30))
                                .fill(defaults2.getColor(TextShrink.getShrinkDir(s, 30)))
                                .toString()
                )
                .stderr(s -> TerminalBrushPresets.stdErrRender(new TextBrush(s)).toString())
                .stdout(s -> TerminalBrushPresets.stdRender(new TextBrush(s)).toString())
                .stdin(s -> s)
                .id(s -> new TextBrush(s).fill(defaults.getColor(s)).toString())
                .cmd(TerminalBrushPresets::bashRender)
                .user(s -> new TextBrush(s).fill(TerminalPalette.MAGENTA).toString())
                .build();
    }

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
}
