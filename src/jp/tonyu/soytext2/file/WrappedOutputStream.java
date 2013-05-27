package jp.tonyu.soytext2.file;

import java.io.IOException;
import java.io.OutputStream;

import jp.tonyu.js.Wrappable;

public class WrappedOutputStream extends OutputStream implements Wrappable {
    OutputStream src;

    public WrappedOutputStream(OutputStream src) {
        super();
        this.src=src;
    }

    public void close() throws IOException {
        src.close();
    }

    public boolean equals(Object arg0) {
        return src.equals(arg0);
    }

    public void flush() throws IOException {
        src.flush();
    }

    public int hashCode() {
        return src.hashCode();
    }

    public String toString() {
        return src.toString();
    }

    public void write(byte[] arg0, int arg1, int arg2) throws IOException {
        src.write(arg0, arg1, arg2);
    }

    public void write(byte[] arg0) throws IOException {
        src.write(arg0);
    }

    public void write(int arg0) throws IOException {
        src.write(arg0);
    }

}
