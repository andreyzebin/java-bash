package io.github.zebin.javabash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zebin.javabash.frontend.brush.TextBrush;
import io.github.zebin.javabash.frontend.brush.TerminalPalette;
import io.github.zebin.javabash.frontend.TerminalBrushedProxy;
import io.github.zebin.javabash.frontend.brush.TerminalBrushConfigs;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
class CommonUseTest {

    @Test
    void exec() {

        final TextTerminal bashState = new TerminalBrushedProxy(new TerminalProcess(PosixUtils.runShellForOs(Runtime.getRuntime())));
        final TextTerminal bashState2 = new TerminalBrushedProxy(new TerminalProcess(PosixUtils.runShellForOs(Runtime.getRuntime())),
                TerminalBrushConfigs.DEFAULT
                        .withDir(s -> new TextBrush(s).fill(TerminalPalette.CYAN).toString()));
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
        TextTerminal t = new TerminalBrushedProxy(new TerminalProcess(PosixUtils.runShellForOs(Runtime.getRuntime())));

        String eval = t.eval(
                "curl -s https://raw.githubusercontent.com/spdx/license-list-data/main/json/licenses.json");

        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.readValue(eval, Licenses.class);
    }

    @Builder
    @Jacksonized
    @Data
    public static class Licenses {

        private List<Object> licenses;

    }
}