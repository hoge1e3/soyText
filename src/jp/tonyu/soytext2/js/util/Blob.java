package jp.tonyu.soytext2.js.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import jp.tonyu.js.Wrappable;
import jp.tonyu.soytext2.file.BinData;
import jp.tonyu.soytext2.file.ByteArrayBinData;
import jp.tonyu.soytext2.file.ReadableBinData;
import jp.tonyu.soytext2.file.ZipMaker;
import jp.tonyu.soytext2.file.ZipReader;

public class Blob implements Wrappable {
    public BinData create() {
        return new ByteArrayBinData();
    }
    public ZipMaker zipWriter(OutputStream out) {
        return new ZipMaker(out);
    }
    public ZipReader zipReader(ReadableBinData in) throws IOException {
        return new ZipReader(in.getInputStream());
    }
    public ZipReader zipReader(InputStream in) throws IOException {
        return new ZipReader(in);
    }
    public String text(ReadableBinData data) throws IOException {
        InputStream in=data.getInputStream();
        StringBuffer res=new StringBuffer();
        InputStreamReader rd=new InputStreamReader(in);
        char[] buf=new char[1024];
        while (true) {
            int r=rd.read(buf);
            if (r<0) break;
            res.append(buf,0,r);
        }
        rd.close();
        return res.toString();
    }
}
