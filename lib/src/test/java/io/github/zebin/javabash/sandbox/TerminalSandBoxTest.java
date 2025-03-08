package io.github.zebin.javabash.sandbox;

import io.github.zebin.javabash.frontend.FunnyTerminal;
import io.github.zebin.javabash.process.TerminalProcess;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class TerminalSandBoxTest {

    @Test
    void test() {
        TerminalSandBox sandBox = new TerminalSandBox(new FunnyTerminal(
                new TerminalProcess(PosixUtils.runShellForOs(Runtime.getRuntime()))
        ));

        log.info(sandBox.pwd().toString());
        sandBox.up();
        log.info(sandBox.pwd().toString());

        if (!sandBox.exists(PosixPath.ofPosix("tmp12345"))) {
            log.info(sandBox.makeDir(PosixPath.ofPosix("tmp12345")).toString());
            log.info("isFile=" + sandBox.isFolder(PosixPath.ofPosix("tmp12345")));
            log.info("isFile=" + sandBox.isFolder(PosixPath.ofPosix(".")));
            sandBox.touch(PosixPath.ofPosix("file123456"));
            log.info("isFile=" + sandBox.isFolder(PosixPath.ofPosix("file123456")));
            log.info("isFile=" + sandBox.isFolder(PosixPath.ofPosix("file1234567")));

        }

        sandBox.listDir().forEach(pp -> log.info(pp.toString()));
        log.info(sandBox.removeDir(PosixPath.ofPosix("tmp12345")).toString());
        sandBox.listDir().forEach(pp -> log.info(pp.toString()));

    }

}