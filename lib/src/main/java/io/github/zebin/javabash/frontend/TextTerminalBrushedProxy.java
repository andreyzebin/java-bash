package io.github.zebin.javabash.frontend;

import io.github.zebin.javabash.*;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class TextTerminalBrushedProxy implements TextTerminal {

    private final TextTerminal delegate;
    private final String rand = UUID.randomUUID().toString().substring(0, 4);
    private final TerminalColors css;


    public TextTerminalBrushedProxy(TextTerminal delegate, TerminalColors css) {
        this.delegate = delegate;
        this.css = css;
    }

    public TextTerminalBrushedProxy(TextTerminal delegate) {
        this.delegate = delegate;
        this.css = TerminalColors.DEFAULT;
    }

    @Override
    public void setTimeoutMillis(long timeoutMillis) {
        delegate.setTimeoutMillis(timeoutMillis);
    }

    @Override
    public int exec(String comm, String mask, Consumer<String> stdout, Consumer<String> stderr) {
        try (
                LineWriter errLog = new LineWriter(s -> log.error(css.stderr.apply(s)));
                LineWriter outLog = new LineWriter(s -> log.debug(css.stdout.apply(s)));
                LineWriter cmdLog = new LineWriter(s -> log.debug(css.stdin.apply(s)));
        ) {

            String pwd = delegate.eval("pwd");
            String user = delegate.eval("whoami");
            MDC.put("terminal.user", css.user.apply(user));
            MDC.put("terminal.dir", css.dir.apply(pwd));
            MDC.put("terminal.id", css.id.apply(rand));
            MDC.put("terminal.command", comm);
            cmdLog.println(css.cmd.apply(mask));
            return delegate.exec(
                    comm,
                    fork(stdout, outLog::print),
                    fork(stderr, errLog::print)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            MDC.clear();
        }
    }


    public static <T> Consumer<T> fork(Consumer<T> left, Consumer<T> right) {
        return (ffu) -> {
            left.accept(ffu);
            right.accept(ffu);
        };
    }

    @Builder
    @Data
    @With
    public static class TerminalColors {

        public static final TerminalColors DEFAULT = getDefault();

        public static TerminalColors getDefault() {
            ColorPool defaults = ColorPool.defaults();
            ColorPool defaults2 = ColorPool.defaults();
            return builder()
                    .dir(
                            s -> new TextBrush(TextShrink.getShrinkDir(s, 30))
                                    .fill(defaults2.getColor(TextShrink.getShrinkDir(s, 30)))
                                    .toString()
                    )
                    .stderr(s -> BashCSS.stdErrRender(new TextBrush(s)).toString())
                    .stdout(s -> BashCSS.stdRender(new TextBrush(s)).toString())
                    .stdin(s -> s)
                    .id(s -> new TextBrush(s).fill(defaults.getColor(s)).toString())
                    .cmd(BashCSS::bashRender)
                    .user(s -> new TextBrush(s).fill(Palette.MAGENTA).toString())
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


    public static class ColorPool {

        private final Set<Palette> pool;
        private final Map<String, Palette> colors = new HashMap<>();


        public ColorPool(Set<Palette> pool) {
            this.pool = pool;
        }

        public static ColorPool defaults() {
            Set<Palette> pool = new HashSet<>();
            pool.add(Palette.BLUE);
            pool.add(Palette.GREEN);
            pool.add(Palette.CYAN);
            pool.add(Palette.MAGENTA);
            pool.add(Palette.RED);

            return new ColorPool(pool);
        }


        public Palette getColor(String rand) {
            return colors.computeIfAbsent(rand, (kk) -> {
                Iterator<Palette> iterator = pool.iterator();
                if (iterator.hasNext()) {
                    Palette next = iterator.next();
                    iterator.remove();
                    return next;
                } else {
                    return Palette.WHITE_BRIGHT;
                }
            });
        }
    }
}