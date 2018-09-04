package ca.jinyao.ma.audio.cachers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class CacheProperties
 * create by jinyaoMa 0010 2018/8/10 0:13
 */
public class CacheProperties extends Properties {
    String propertyPath = "";

    private CacheProperties(String path) {
        propertyPath = path;
    }

    public static CacheProperties getInstance(String path, String filename) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir.getPath() + File.separator + filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        CacheProperties pro = new CacheProperties(dir.getPath() + File.separator + filename);
        try {
            InputStream is = new FileInputStream(file);
            pro.load(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pro;
    }

    public String get(String key) {
        String value = "";
        Object object = super.get(key);
        if (object != null) {
            value = (String) object;
        }
        return value;
    }

    public String put(String key, String value) {
        super.setProperty(key, value);
        try {
            this.store(new FileOutputStream(this.propertyPath), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

    public String remove(Object key) {
        String value = (String) super.remove(key);
        try {
            this.store(new FileOutputStream(this.propertyPath), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }
}
