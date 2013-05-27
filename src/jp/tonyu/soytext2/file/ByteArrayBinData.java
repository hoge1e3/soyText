package jp.tonyu.soytext2.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.tonyu.debug.Log;
import jp.tonyu.js.Wrappable;

public class ByteArrayBinData implements BinData, Wrappable {
    byte[] b;

    public ByteArrayBinData(byte[] byteArray) {
        b=byteArray;
    }

    public ByteArrayBinData() {
        b=null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (b==null) Log.die("Buffer is null");
        return new WrappedInputStream(new ByteArrayInputStream(b));
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                b=toByteArray();
            }
        };
        return  new WrappedOutputStream( o );
    }

}
