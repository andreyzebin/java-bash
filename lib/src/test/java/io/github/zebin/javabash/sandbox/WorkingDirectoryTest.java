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
class WorkingDirectoryTest {
    @Test
    void test() throws IOException {
        FileManager sandBox = new FileManager(
                new FunnyTerminal(
                        new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime()))
                )
        );
        sandBox.goUp();

        WorkingDirectory workingDirectory = new WorkingDirectory(sandBox);
        List<PosixPath> found = new LinkedList<>();

        workingDirectory.traverse(
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
        try (Writer v = workingDirectory.put(rand);) {
            v.write("""
                    {
                    "foo": "bar"
                    }
                    """);
        }
        try (Reader reader = workingDirectory.get(rand);) {
            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            FooBar fooBar = objectMapper.readValue(reader, FooBar.class);

            Assertions.assertEquals(FooBar.builder().foo("bar").build(), fooBar);

        }
        workingDirectory.delete(rand);


        PosixPath rand2 = PosixPath.relate().climb("kk", UUID.randomUUID().toString());
        try (Writer v = workingDirectory.put(rand2);) {
            v.write("line1;");
        }

        try (Writer v = workingDirectory.patch(rand2);) {
            v.write("line2;");
        }

        try (Reader reader = workingDirectory.get(rand2);) {
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
        workingDirectory.delete(rand2);
        workingDirectory.delete(rand2.descend());

    }

    @Builder
    @Jacksonized
    @Data
    public static class FooBar {

        private String foo;

    }

}