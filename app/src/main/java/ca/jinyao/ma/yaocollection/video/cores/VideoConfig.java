package ca.jinyao.ma.yaocollection.video.cores;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

/**
 * Class VideoConfig
 * create by jinyaoMa 0031 2018/8/31 21:44
 */
public class VideoConfig {
    public static final int DEFAULT = 0;

    public static final String TYPE_MOVIE = "movie";
    public static final String TYPE_TVP = "ju";
    public static final String TYPE_ANIME = "dm";

    public static final String BASE_URL = "http://www.y80s.com/";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64)" +
            " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";

    public static String imagePath = "/mnt/sdcard/Yao/video/image/";
    public static String downloadPath = "/mnt/sdcard/Yao/video/download/";

    public static Connection getConnectionFor(String url) {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .maxBodySize(DEFAULT)
                .ignoreContentType(true);
    }
}
