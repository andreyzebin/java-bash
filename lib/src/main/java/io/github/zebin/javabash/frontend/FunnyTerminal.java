package io.github.zebin.javabash.frontend;

import io.github.zebin.javabash.frontend.brush.TerminalPalette;
import io.github.zebin.javabash.process.TextTerminal;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.*;
import java.util.function.Consumer;

@Slf4j
public class FunnyTerminal implements TextTerminal {

    private final TextTerminal delegate;
    private final String rand = UUID.randomUUID().toString().substring(0, 4);
    private final FunnyTerminalConfigs css;


    public FunnyTerminal(TextTerminal delegate, FunnyTerminalConfigs css) {
        this.delegate = delegate;
        this.css = css;
    }

    public FunnyTerminal(TextTerminal delegate) {
        this.delegate = delegate;
        this.css = FunnyTerminalConfigs.DEFAULT;
    }

    @Override
    public int exec(String comm, String mask, Consumer<String> stdout, Consumer<String> stderr) {
        try (
                LineWriter errLog = new LineWriter(s -> log.debug(css.getStderr().apply(s)));
                LineWriter outLog = new LineWriter(s -> log.debug(css.getStdout().apply(s)));
                LineWriter cmdLog = new LineWriter(s -> log.debug(css.getStdin().apply(s)));
        ) {

            String pwd = delegate.eval("pwd");
            String user = delegate.eval("whoami");
            MDC.put("terminal.user", css.getUser().apply(user));
            MDC.put("terminal.dir", css.getDir().apply(pwd));
            MDC.put("terminal.id", css.getId().apply(rand));
            MDC.put("terminal.command", comm);
            cmdLog.println(css.getCmd().apply(mask));
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


    public static class ColorPool {

        private final Set<TerminalPalette> pool;
        private final Map<String, TerminalPalette> colors = new HashMap<>();


        public ColorPool(Set<TerminalPalette> pool) {
            this.pool = pool;
        }

        public static ColorPool defaults() {
            Set<TerminalPalette> pool = new HashSet<>();
            pool.add(TerminalPalette.BLUE);
            pool.add(TerminalPalette.GREEN);
            pool.add(TerminalPalette.CYAN);
            pool.add(TerminalPalette.MAGENTA);
            pool.add(TerminalPalette.RED);

            return new ColorPool(pool);
        }


        public TerminalPalette getColor(String rand) {
            return colors.computeIfAbsent(rand, (kk) -> {
                Iterator<TerminalPalette> iterator = pool.iterator();
                if (iterator.hasNext()) {
                    TerminalPalette next = iterator.next();
                    iterator.remove();
                    return next;
                } else {
                    return TerminalPalette.WHITE_BRIGHT;
                }
            });
        }
    }
}