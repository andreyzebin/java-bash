package io.github.zebin.javabash.sandbox;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zebin.javabash.frontend.FunnyTerminal;
import io.github.zebin.javabash.process.TerminalProcess;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Slf4j
class SandboxFilesImplTest {
    @Test
    void test() throws IOException {
        FileManager sandBox = new FileManager(
                new FunnyTerminal(
                        new TerminalProcess(PosixUtils.runShellForOs(Runtime.getRuntime()))
                )
        );
        sandBox.goUp();

        SandboxFilesImpl sandboxFilesImpl = new SandboxFilesImpl(sandBox);
        List<PosixPath> found = new LinkedList<>();

        sandboxFilesImpl.traverse(
                PosixPath.ofPosix(""),
                f -> {
                    return !f.endsWith(PosixPath.ofPosix(".git")) &&
                            !f.endsWith(PosixPath.ofPosix("build")) &&
                            !f.endsWith(PosixPath.ofPosix(".gradle")) &&
                            !f.endsWith(PosixPath.ofPosix("gradle")) &&
                            !f.endsWith(PosixPath.ofPosix(".idea")) &&
                            !f.endsWith(PosixPath.ofPosix(".github"));
                },
                f -> {
                    found.add(f);
                    return false;
                }
        );

        found.forEach(f -> log.info(f.toString()));

        PosixPath rand = PosixPath.ofPosix(UUID.randomUUID().toString());
        try (Writer v = sandboxFilesImpl.put(rand);) {
            v.write("""
                    {
                    "foo": "bar"
                    }
                    """);
        }
        try (Reader reader = sandboxFilesImpl.get(rand);) {
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            FooBar fooBar = objectMapper.readValue(reader, FooBar.class);

            Assertions.assertEquals(FooBar.builder().foo("bar").build(), fooBar);

        }
        sandboxFilesImpl.delete(rand);


        PosixPath rand2 = PosixPath.ofPosix(UUID.randomUUID().toString());
        try (Writer v = sandboxFilesImpl.put(rand2);) {
            v.write("line1;");
        }

        try (Writer v = sandboxFilesImpl.patch(rand2);) {
            v.write("line2;");
        }

        try (Reader reader = sandboxFilesImpl.get(rand2);) {
            char[] arr = new char[8 * 1024];
            StringBuilder buffer = new StringBuilder();
            int numCharsRead;
            while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
                buffer.append(arr, 0, numCharsRead);
            }
            Assertions.assertEquals(
                    """
                            line1;
                            line2;""", buffer.toString());

        }
        sandboxFilesImpl.delete(rand2);

    }

    @Builder
    @Jacksonized
    @Data
    public static class FooBar {

        private String foo;

    }

}