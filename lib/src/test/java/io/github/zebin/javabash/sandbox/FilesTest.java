package io.github.zebin.javabash.sandbox;

import io.github.zebin.javabash.frontend.FunnyTerminal;
import io.github.zebin.javabash.process.TerminalProcess;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@Slf4j
class FilesTest {

    @Test
    void test() {
        FileManager sandBox = new FileManager(
                new FunnyTerminal(
                        new TerminalProcess(BashUtils.runShellForOs(Runtime.getRuntime()))
                )
        );

        sandBox.getCurrent();
        sandBox.goUp();
        sandBox.list();

        PosixPath tempFolderRel = PosixPath.ofPosix(UUID.randomUUID().toString());
        PosixPath tempFolderAbs = null;
        try {
            tempFolderAbs = sandBox.makeDir(tempFolderRel);

            log.info("Made dir {}", tempFolderAbs);
            sandBox.go(tempFolderAbs);

            PosixPath newFolderRel = PosixPath.ofPosix("tmp12345");
            PosixPath newFolderAbsolute = sandBox.makeDir(newFolderRel);

            PosixPath newFileRelative = PosixPath.ofPosix("file123456");
            PosixPath newFileAbsolute = sandBox.makeFile(newFileRelative);


            // file exists
            Assertions.assertTrue(sandBox.exists(newFileRelative));
            // file exists absolute
            Assertions.assertTrue(sandBox.exists(newFileAbsolute));
            // folder exists
            Assertions.assertTrue(sandBox.exists(newFolderRel));
            // folder exists absolute
            Assertions.assertTrue(sandBox.exists(newFolderAbsolute));

            // file is not a folder
            Assertions.assertFalse(sandBox.dirExists(newFileRelative));
            // non-existing path is not a folder
            Assertions.assertFalse(sandBox.dirExists(PosixPath.ofPosix("non-existing-path")));
            // folder is folder
            Assertions.assertTrue(sandBox.dirExists(newFolderRel));
            // . == local directory is a folder
            Assertions.assertTrue(sandBox.dirExists(PosixPath.CURRENT));

            // file is file
            Assertions.assertTrue(sandBox.fileExists(newFileRelative));
            // non existing file is not a file
            Assertions.assertFalse(sandBox.fileExists(PosixPath.ofPosix("non-existing-file")));
            // folder is not a file
            Assertions.assertFalse(sandBox.fileExists(newFolderRel));
            // . == local directory is not a file
            Assertions.assertFalse(sandBox.fileExists(PosixPath.CURRENT));

            Assertions.assertFalse(sandBox.remove(PosixPath.ofPosix("non-existing-file")));
            Assertions.assertTrue(sandBox.remove(newFileRelative));
            Assertions.assertTrue(sandBox.remove(newFolderRel));

        } finally {
            sandBox.goUp();
            if (tempFolderAbs != null) {
                sandBox.remove(tempFolderAbs);
            }
        }

        log.info("Removed {}", tempFolderAbs);
    }

}