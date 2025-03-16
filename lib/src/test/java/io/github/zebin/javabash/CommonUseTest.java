package io.github.zebin.javabash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zebin.javabash.frontend.TextBrush;
import io.github.zebin.javabash.frontend.TerminalPalette;
import io.github.zebin.javabash.frontend.FunnyTerminal;
import io.github.zebin.javabash.frontend.FunnyTerminalConfigs;
import io.github.zebin.javabash.process.TerminalProcess;
import io.github.zebin.javabash.process.TextTerminal;
import io.github.zebin.javabash.sandbox.BashUtils;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
class CommonUseTest {

    @Test
    void exec() {

        final TextTerminal bashState = new FunnyTerminal(
                new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime()))
        );
        final TextTerminal bashState2 = new FunnyTerminal(
                new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime())),
                FunnyTerminalConfigs.DEFAULT
                        .withDir(s -> new TextBrush(s).fill(TerminalPalette.CYAN).toString())
        );
        StringBuilder sb = new StringBuilder();

        bashState.exec("echo abc", sb::append, sb::append);
        bashState.exec("echo abc", sb::append, sb::append);

        Assertions.assertEquals("abc;abc", sb.toString().lines().collect(Collectors.joining(";")));
        Assertions.assertEquals("ffu", bashState.eval("echo ffu"));
        Assertions.assertEquals("ffu", bashState2.eval("echo ffu"));
        try {
            Assertions.assertTrue(bashState.eval("mkdir tmp3456gg").isEmpty());
            Assertions.assertTrue(bashState.eval("cd tmp3456gg").isEmpty());
            Assertions.assertTrue(bashState.eval("cd ..").isEmpty());
        } finally {
            Assertions.assertTrue(bashState.eval("rm -rf tmp3456gg").isEmpty());
        }

        Assertions.assertThrows(RuntimeException.class, () -> bashState2.eval("cd ffukku"));
        StringBuilder err = new StringBuilder();
        bashState.exec("cd JJJ___kkk", (ff) -> {
        }, err::append);
        Assertions.assertTrue(err.toString().contains("No such file"));

    }

    @Test
    void test() throws JsonProcessingException {
        TextTerminal t = new FunnyTerminal(new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime())));

        UUID jsonTempFile = UUID.randomUUID();
        try {
            t.eval(String.format("touch %s", jsonTempFile));
            t.eval(String.format("echo '{ \"foo\":\"bar\" }' > %s", jsonTempFile));
            t.eval(String.format("curl -s -H \"Content-Type: application/json\" " +
                            "--data @%s " +
                            "-X PUT http://localhost:4000/dir1/f", jsonTempFile)
            );

            String eval = t.eval("curl -s http://localhost:4000/dir1/f");

            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            FooBar fooBar = objectMapper.readValue(eval, FooBar.class);

            Assertions.assertEquals(FooBar.builder().foo("bar").build(), fooBar);
        } finally {
            t.eval(String.format("rm %s", jsonTempFile));
        }

    }

    @Builder
    @Jacksonized
    @Data
    public static class FooBar {

        private String foo;

    }
}