package io.github.zebin.javabash.process;

import io.github.zebin.javabash.frontend.FunnyTerminal;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class TerminalProcess implements TextTerminal {

    private final Process process;
    private final byte[] buffer = new byte[4000];
    private final PrintWriter pw;
    private final String fin = UUID.randomUUID().toString().substring(0, 8);
    private long timeoutMillis = 1800_000; // 30 minutes
    private int relaxTimeMillis = 1;

    public TerminalProcess(Process process) {
        this.process = process;
        pw = new PrintWriter(process.getOutputStream());
        pw.println(String.format("fu=%s ; fu+=%s ; echo $fu", fin.substring(0, 4), fin.substring(4, 8)));

        execute(
                process,
                buffer,
                fin,
                (ff) -> {
                }, (ff) -> {
                });
    }

    public void setTimeoutMillis(long timeout) {
        timeoutMillis = timeout;
    }

    public void setRelaxTimeMillis(int relax) {
        relaxTimeMillis = relax;
    }

    @Override
    public int exec(String comm, String mask, Consumer<String> stdout, Consumer<String> stderr) {
        StringBuilder ret = new StringBuilder();
        StringBuilder err = new StringBuilder();

        pw.println(comm);
        pw.println("return=$?");
        pw.println("echo $fu");
        execute(process, buffer, fin, stdout, stderr);
        pw.println("echo $return");
        pw.println("echo $fu");

        execute(process, buffer, fin, ret::append, err::append);
        return Integer.parseInt(ret.toString().lines().collect(Collectors.joining()));
    }

    private void execute(Process process, byte[] buffer, String fin, Consumer<String> wr, Consumer<String> stderr) {
        long start = System.currentTimeMillis();
        pw.flush();
        InputStream out = process.getInputStream();
        InputStream err = process.getErrorStream();
        AtomicBoolean writtenOut = new AtomicBoolean();
        AtomicBoolean writtenErr = new AtomicBoolean();
        // wait $fu$
        int matched = 0;
        boolean isFinished = false;
        try {
            while (isAlive(process) && !isFinished) {
                log.trace("Process alive...");
                writtenOut.set(true);
                writtenErr.set(true);

                while (writtenOut.get() || writtenErr.get()) {
                    if (System.currentTimeMillis() - start > timeoutMillis) {
                        throw new RuntimeException("Failed execution, because of timeout!");
                    }
                    writtenOut.set(false);
                    writtenErr.set(false);
                    log.trace("Pulling stdout...");
                    matched = pull(
                            buffer,
                            fin,
                            FunnyTerminal.fork(wr, (fu) -> writtenOut.set(true)),
                            out,
                            matched
                    );
                    log.trace("Pulling stderr...");
                    pullErr(
                            buffer,
                            FunnyTerminal.fork(stderr, (fu) -> writtenErr.set(true)),
                            err
                    );

                    if (matched == fin.length()) {
                        // eof
                        isFinished = true;
                    }
                }

                if (isAlive(process) && !isFinished) {
                    try {
                        Thread.sleep(relaxTimeMillis);
                    } catch (InterruptedException e) {
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int pull(byte[] buffer, String fin, Consumer<String> wr, InputStream out, int matched)
            throws IOException {
        int no = out.available();
        log.trace("Available {} bytes", no);
        if (no > 0) {
            int n = out.read(buffer, 0, Math.min(no, buffer.length));
            log.trace("Read {} bytes", n);
            String bufSeg = new String(buffer, 0, n);
            for (int i = 0; i < bufSeg.length(); i++) {
                char toFind = fin.charAt(matched);
                char cChar = bufSeg.charAt(i);
                if (toFind == cChar) {
                    matched++;
                    if (matched == fin.length()) {
                        break;
                    }
                } else {
                    wr.accept(fin.substring(0, matched));
                    wr.accept(String.valueOf(cChar));
                    matched = 0;
                }
            }
        }
        return matched;
    }

    private static void pullErr(byte[] buffer, Consumer<String> wr, InputStream out)
            throws IOException {
        int no = out.available();
        if (no > 0) {
            int n = out.read(buffer, 0, Math.min(no, buffer.length));
            String bufSeg = new String(buffer, 0, n);
            wr.accept(bufSeg);
        }
    }

    private boolean isAlive(Process p) {
        try {
            p.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

}