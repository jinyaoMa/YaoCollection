package ca.jinyao.ma.yaocollection.video.components;

import android.support.annotation.NonNull;

/**
 * Class Episode
 * create by jinyaoMa 0031 2018/8/31 22:47
 */
public class Episode {
    private String name;
    private String size;
    private String downloadUrl;

    public Episode(String name, String size, @NonNull String downloadUrl) {
        this.name = name;
        this.size = size;
        this.downloadUrl = downloadUrl;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        return size;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
