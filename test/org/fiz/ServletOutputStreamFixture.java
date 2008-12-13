package org.fiz;

import java.io.*;
import javax.servlet.*;

/**
 * This class provides a dummy implementation of the ServletOutputStream class
 * for use in testing.
 */
public class ServletOutputStreamFixture extends ServletOutputStream {
    protected ByteArrayOutputStream out;

    public ServletOutputStreamFixture(ByteArrayOutputStream out) {
        this.out = out;
    }

    public void close() throws IOException {
        out.close();
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    public void write(int b) throws IOException {
        out.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }
}
