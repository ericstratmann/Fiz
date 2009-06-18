package org.fiz;

import java.io.*;
import javax.servlet.*;

/**
 * This class provides a dummy implementation of the ServletOutputStream class
 * for use in testing.
 */
public class ServletOutputStreamFixture extends ServletOutputStream {
    protected ByteArrayOutputStream out;

    // The following variables are used to generate errors on certain
    // operations, for testing purposes.

    protected boolean errorInWrite = false;
    protected boolean errorInFlush = false;

    public ServletOutputStreamFixture(ByteArrayOutputStream out) {
        this.out = out;
    }

    public void close() throws IOException {
        out.close();
    }

    public void flush() throws IOException {
        if (errorInFlush) {
            throw new IOException("error during flush");
        }
        out.flush();
    }

    public void write(byte[] b) throws IOException {
        if (errorInWrite) {
            throw new IOException("error during write");
        }
        out.write(b);
    }

    public void write(int b) throws IOException {
        if (errorInWrite) {
            throw new IOException("error during write");
        }
        out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (errorInWrite) {
            throw new IOException("error during write");
        }
        out.write(b, off, len);
    }

    // The following methods are used to arrange for errors during future
    // calls.

    public void setWriteError() {
        errorInWrite = true;
    }

    public void setFlushError() {
        errorInFlush = true;
    }
}
