package jp.tonyu.soytext2.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.mozilla.javascript.Scriptable;

import jp.tonyu.js.BlankScriptableObject;
import jp.tonyu.js.Scriptables;
import jp.tonyu.js.Wrappable;
import jp.tonyu.util.SFile;

public class ZipReader implements Wrappable {
    ZipInputStream in;
    public ZipReader(InputStream in) {
        this.in=new ZipInputStream(in);
    }
    public Scriptable next() throws IOException {
        try {
            ZipEntry e=in.getNextEntry();
            if (e==null) {
                in.close();
                return null;
            }
            ByteArrayBinData b=new ByteArrayBinData();
            OutputStream bout = b.getOutputStream();
            SFile.redirect(in, bout);
            bout.close();
            Scriptable res=new BlankScriptableObject();
            Scriptables.put(res, "name", e.getName());
            Scriptables.put(res, "body", b);
            return res;
        } catch (IOException e) {
            in.close();
            throw e;
        }
    }
}
