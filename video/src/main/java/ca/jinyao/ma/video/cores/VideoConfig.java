package ca.jinyao.ma.video.cores;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * Class VideoConfig
 * create by jinyaoMa 0031 2018/8/31 21:44
 */
public class VideoConfig {
    public static final int DEFAULT = 0;

    public static final String TYPE_MOVIE = "movie";
    public static final String TYPE_TVP = "ju";
    public static final String TYPE_ANIME = "dm";

    public static final String URL_BASE = "http://www.y80s.com";
    public static final String URL_MOVIE = "http://www.y80s.com/movie/list/----";
    public static final String URL_TVP = "http://www.y80s.com/ju/list/-----";
    public static final String URL_ANIME = "http://www.y80s.com/dm/list/----14-";

    public static final String PAGE_HOLDER = "-p";

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

    public static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {

                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {

                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
