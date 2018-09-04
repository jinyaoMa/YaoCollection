package ca.jinyao.ma.audio.cores;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import ca.jinyao.ma.audio.cachers.CacheProperties;

/**
 * Class AudioConfig
 * create by jinyaoMa 0009 2018/8/9 22:09
 */
public class AudioConfig {
    // Constants
    public static final String TAG = "audio";
    public static final int NONE = -1;
    public static final int DEFAULT = 0; // Used in [index, mode]
    public static final int SECOND = 1000; // a second = 1000 millisecond
    public static final String EMPTY_STRING = "";

    public static final int MODE_NORMAL = 0; // start from DEFAULT
    public static final int MODE_REPEAT = 1;
    public static final int MODE_REPEAT_LIST = 2;
    public static final int MODE_RANDOM = 3;

    public static final int TAB_SONG = 0;
    public static final int TAB_ALBUM = 1;
    public static final int TAB_ARTIST = 2;
    public static final int TAB_PLAYLIST = 3;

    public static final int REF_QQ = 0;
    public static final int REF_163 = 1;
    public static final int PAGE_ITEM_LIMIT = 20; // for page with item limited
    public static final int HEADERS = 0;
    public static final int URL_FOR_PATH = 1;
    public static final int DATA_FOR_PATH = 2;
    public static final int URL_FOR_ALBUM_SONG = 3;
    public static final int DATA_FOR_ALBUM_SONG = 4;
    public static final int URL_FOR_ARTIST_ALBUM = 5;
    public static final int DATA_FOR_ARTIST_ALBUM = 6;
    public static final int URL_FOR_ARTIST_SONG = 7;
    public static final int DATA_FOR_ARTIST_SONG = 8;
    public static final int URL_FOR_PLAYLIST_SONG = 9;
    public static final int DATA_FOR_PLAYLIST_SONG = 10;
    public static final int URL_FOR_SEARCH_SONG = 11;
    public static final int DATA_FOR_SEARCH_SONG = 12;
    public static final int URL_FOR_SEARCH_ALBUM = 13;
    public static final int DATA_FOR_SEARCH_ALBUM = 14;
    public static final int URL_FOR_SEARCH_ARTIST = 15;
    public static final int DATA_FOR_SEARCH_ARTIST = 16;
    public static final int URL_FOR_SEARCH_PLAYLIST = 17;
    public static final int DATA_FOR_SEARCH_PLAYLIST = 18;
    public static final int URL_FOR_LYRIC = 19;
    public static final int DATA_FOR_LYRIC = 20;
    public static final int COVER_PATH_FOR_ALBUM = 21;
    public static final int COVER_PATH_FOR_ARTIST = 22;
    public static final int URL_FOR_PLAYLIST_TAGS = 23;
    public static final int DATA_FOR_PLAYLIST_TAGS = 24;
    public static final int URL_FOR_PLAYLIST_BROWSE = 25;
    public static final int DATA_FOR_PLAYLIST_BROWSE = 26;
    public static final String[][] SOURCE = {
            {
                    "Cookie/:hb/qqmusic_fromtag=66/:t/Referer/:hb/http://y.qq.com/",
                    "http://c.y.qq.com/base/fcgi-bin/fcg_music_express_mobile3.fcg",
                    "format=json&platform=yqq&cid=205361747&guid=-1&filename=C400{id}.m4a&songmid={id}",
                    "http://c.y.qq.com/v8/fcg-bin/fcg_v8_album_info_cp.fcg",
                    "albummid={id}&charset=utf-8",
                    "http://c.y.qq.com/v8/fcg-bin/fcg_v8_singer_album.fcg",
                    "format=json&inCharset=utf8&outCharset=utf-8&platform=yqq&singermid={id}&order=time&begin={page}&num=" + String.format("%d", PAGE_ITEM_LIMIT),
                    "http://c.y.qq.com/v8/fcg-bin/fcg_v8_singer_track_cp.fcg",
                    "format=json&platform=yqq&singermid={id}&order=listen&begin={page}&num=" + String.format("%d", PAGE_ITEM_LIMIT),
                    "http://c.y.qq.com/qzone/fcg-bin/fcg_ucc_getcdinfo_byids_cp.fcg",
                    "type=1&json=1&utf8=1&onlysong=0&format=json&inCharset=utf8&outCharset=utf-8&platform=yqq&disstid={id}",
                    "http://c.y.qq.com/soso/fcgi-bin/client_search_cp",
                    "cr=1&t=0&aggr=1&format=json&inCharset=utf8&outCharset=utf-8&platform=yqq&w={keyword}&p={page}&n=" + String.format("%d", PAGE_ITEM_LIMIT),
                    "http://c.y.qq.com/soso/fcgi-bin/client_search_cp",
                    "cr=1&t=8&sem=10&format=json&inCharset=utf8&outCharset=utf-8&platform=yqq&w={keyword}&p={page}&n=" + String.format("%d", PAGE_ITEM_LIMIT),
                    "http://c.y.qq.com/soso/fcgi-bin/client_search_cp",
                    "cr=1&t=9&format=json&inCharset=utf8&outCharset=utf-8&platform=yqq&w={keyword}&p={page}&n=" + String.format("%d", PAGE_ITEM_LIMIT),
                    "http://c.y.qq.com/soso/fcgi-bin/client_music_search_songlist",
                    "format=json&platform=yqq&outCharset=utf-8&inCharset=utf8&query={keyword}&page_no={page}&num_per_page=" + String.format("%d", PAGE_ITEM_LIMIT),
                    "http://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg",
                    "g_tk=5381&format=jsonp&inCharset=utf8&outCharset=utf-8&platform=yqq&nobase64=1&songmid={id}&jsonpCallback=" + TAG,
                    "http://y.gtimg.cn/music/photo_new/T002R300x300M000{id}.jpg",
                    "http://y.gtimg.cn/music/photo_new/T001R300x300M000{id}.jpg",
                    "http://c.y.qq.com/splcloud/fcgi-bin/fcg_get_diss_tag_conf.fcg",
                    "format=json&inCharset=utf8&outCharset=utf-8&platform=yqq",
                    "http://c.y.qq.com/splcloud/fcgi-bin/fcg_get_diss_by_tag.fcg",
                    "format=json&inCharset=utf8&outCharset=utf-8&platform=yqq&sortId=2&categoryId={tag}&sin={page}&ein={extra}" // EXTRA_HOLDER for calculating PAGE_ITEM_LIMIT
            },
            {
                    "Cookie/:hb/appver=6;os=pc/:t/Referer/:hb/http://music.163.com/",
                    "http://music.163.com/api/song/enhance/player/url",
                    "ids=[{id}]&br=128000",
                    "http://music.163.com/api/album/{id}",
                    "total=true&ext=true",
                    "http://music.163.com/api/artist/albums/{id}",
                    "total=true&ext=true&offset={page}&limit=" + String.format("%d", PAGE_ITEM_LIMIT),
                    "http://music.163.com/api/artist/{id}",
                    "total=true&ext=true",
                    "http://music.163.com/api/playlist/detail",
                    "id={id}&updateTime=-1",
                    "http://music.163.com/api/search/pc",
                    "type=1&s={keyword}&offset={page}&limit=" + String.format("%d", PAGE_ITEM_LIMIT),
                    "http://music.163.com/api/search/pc",
                    "type=10&s={keyword}&offset={page}&limit=" + String.format("%d", PAGE_ITEM_LIMIT),
                    "http://music.163.com/api/search/pc",
                    "type=100&s={keyword}&offset={page}&limit=" + String.format("%d", PAGE_ITEM_LIMIT),
                    "http://music.163.com/api/search/pc",
                    "type=1000&s={keyword}&offset={page}&limit=" + String.format("%d", PAGE_ITEM_LIMIT),
                    "http://music.163.com/api/song/lyric",
                    "lv=-1&tv=-1&id={id}",
                    "",                                                                             // No album cover path sample
                    "",                                                                             // No artist cover path sample
                    "http://music.163.com/api/playlist/catalogue",
                    "total=true&ext=true",
                    "http://music.163.com/api/playlist/list",
                    "offset={page}&cat={tag}&limit=" + String.format("%d", PAGE_ITEM_LIMIT)
            }
    };
    public static final String HEADERS_TAGS_DELIMITER = "/:t/";                            // Delimiters and holders are used in the constant variable SOURCE above
    public static final String HEADERS_HEAD_BODY_DELIMITER = "/:hb/";
    public static final String DATA_TAGS_DELIMITER = "&";
    public static final String DATA_HEAD_BODY_DELIMITER = "=";
    public static final String ID_HOLDER = "{id}";
    public static final String PAGE_HOLDER = "{page}";
    public static final String KEYWORD_HOLDER = "{keyword}";
    public static final String TAG_HOLDER = "{tag}";
    public static final String EXTRA_HOLDER = "{extra}";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64)" +
            " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";

    public static final long CACHE_MIN_FREE_SPACE = 512l * 1024 * 1024;
    public static String imageCachePath = "/mnt/sdcard/Yao/audio/image/";
    public static String songCachePath = "/mnt/sdcard/Yao/audio/song/";
    public static String lyricCachePath = "/mnt/sdcard/Yao/audio/lyric/";
    public static String cachePropertiesFilename = "cache";
    public static int imageCacheLimit = 2000;
    public static int songCacheLimit = 100;
    public static int lyricCacheLimit = 2000;

    public static final String PROXY_ADDRESS = "proxy.uku.im";
    public static final int PROXY_PORT = 443;
    public static final String PROXY_TEST_ADDRESS = "http://y.qq.com/";
    public static Boolean isProxyEnabled = false;

    public static String getArtistImageUrlBySample(int ref, String artistId) {
        switch (ref) {
            case REF_QQ:
            case REF_163:
                if (!SOURCE[ref][COVER_PATH_FOR_ARTIST].isEmpty()) {
                    return SOURCE[ref][COVER_PATH_FOR_ARTIST].replace(ID_HOLDER, artistId);
                }
        }
        return EMPTY_STRING;
    }

    public static String getAlbumImageUrlBySample(int ref, String albumId) {
        switch (ref) {
            case REF_QQ:
            case REF_163:
                if (!SOURCE[ref][COVER_PATH_FOR_ALBUM].isEmpty()) {
                    return SOURCE[ref][COVER_PATH_FOR_ALBUM].replace(ID_HOLDER, albumId);
                }
        }
        return EMPTY_STRING;
    }

    /**
     * Clear cache
     *
     * @return true if the clearing is ok, otherwise false
     */
    public static void clearCache() {
        for (File file : new File(songCachePath).listFiles()) {
            file.delete();
        }

        for (File file : new File(imageCachePath).listFiles()) {
            file.delete();
        }

        for (File file : new File(lyricCachePath).listFiles()) {
            file.delete();
        }

        setSongCachePath(songCachePath);
        setImageCachePath(imageCachePath);
        setLyricCachePath(lyricCachePath);
    }

    /**
     * Clear image cache
     *
     * @return true if the clearing is ok, otherwise false
     */
    public static void clearImageCache() {
        for (File file : new File(imageCachePath).listFiles()) {
            file.delete();
        }
        setImageCachePath(imageCachePath);
    }

    /**
     * Clear song cache
     *
     * @return true if the clearing is ok, otherwise false
     */
    public static void clearSongCache() {
        for (File file : new File(songCachePath).listFiles()) {
            file.delete();
        }
        setSongCachePath(songCachePath);
    }

    /**
     * Clear lyric cache
     *
     * @return true if the clearing is ok, otherwise false
     */
    public static void clearLyricCache() {
        for (File file : new File(lyricCachePath).listFiles()) {
            file.delete();
        }
        setLyricCachePath(lyricCachePath);
    }

    /**
     * Set songCacheLimit
     *
     * @param limit limit number of songs
     * @return true if the setting is ok, otherwise false
     */
    public static Boolean setSongCacheLimit(int limit) {
        if (limit < 0) {
            return false;
        }

        CacheProperties cacheProperties = CacheProperties.getInstance(songCachePath, cachePropertiesFilename);
        if (cacheProperties.size() > limit) {
            int removeNumber = cacheProperties.size() - limit;
            for (Object key : cacheProperties.keySet()) {
                String oldValue = cacheProperties.remove(key);
                new File(songCachePath + oldValue).delete();

                removeNumber -= 1;
                if (removeNumber <= 0) {
                    break;
                }
            }
        }

        songCacheLimit = limit;
        return true;
    }

    /**
     * Set imageCacheLimit
     *
     * @param limit limit number of images
     * @return true if the setting is ok, otherwise false
     */
    public static Boolean setImageCacheLimit(int limit) {
        if (limit < 0) {
            return false;
        }

        CacheProperties cacheProperties = CacheProperties.getInstance(imageCachePath, cachePropertiesFilename);
        if (cacheProperties.size() > limit) {
            int removeNumber = cacheProperties.size() - limit;
            for (Object key : cacheProperties.keySet()) {
                String oldValue = cacheProperties.remove(key);
                new File(imageCachePath + oldValue).delete();

                removeNumber -= 1;
                if (removeNumber <= 0) {
                    break;
                }
            }
        }

        imageCacheLimit = limit;
        return true;
    }

    /**
     * Set lyricCacheLimit
     *
     * @param limit limit number of images
     * @return true if the setting is ok, otherwise false
     */
    public static Boolean setLyricCacheLimit(int limit) {
        if (limit < 0) {
            return false;
        }

        CacheProperties cacheProperties = CacheProperties.getInstance(lyricCachePath, cachePropertiesFilename);
        if (cacheProperties.size() > limit) {
            int removeNumber = cacheProperties.size() - limit;
            for (Object key : cacheProperties.keySet()) {
                String oldValue = cacheProperties.remove(key);
                new File(lyricCachePath + oldValue).delete();

                removeNumber -= 1;
                if (removeNumber <= 0) {
                    break;
                }
            }
        }

        lyricCacheLimit = limit;
        return true;
    }

    /**
     * Set lyricCachePath
     *
     * @param path new path
     * @return true if the setting is ok, otherwise false
     */
    public static Boolean setLyricCachePath(String path) {
        if (!path.endsWith("/")) {
            return false;
        }

        File original = new File(lyricCachePath);
        File target = new File(path);
        File property = new File(path + cachePropertiesFilename);

        if (original.equals(target)) {
            if (original.exists() && original.isDirectory()) {
                if (!property.exists()) {
                    try {
                        property.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }

        if (target.exists() && target.isDirectory()) {
            for (File file : target.listFiles()) {
                file.delete();
            }
        } else {
            target.mkdirs();
        }

        if (original.exists() && original.isDirectory()) {
            for (File file : original.listFiles()) {
                file.renameTo(new File(path + File.separator + file.getName()));
            }
        }

        if (!property.exists()) {
            try {
                property.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        lyricCachePath = path;
        return true;
    }

    /**
     * Set imageCachePath
     *
     * @param path new path
     * @return true if the setting is ok, otherwise false
     */
    public static Boolean setImageCachePath(String path) {
        if (!path.endsWith("/")) {
            return false;
        }

        File original = new File(imageCachePath);
        File target = new File(path);
        File property = new File(path + cachePropertiesFilename);

        if (original.equals(target)) {
            if (original.exists() && original.isDirectory()) {
                if (!property.exists()) {
                    try {
                        property.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }

        if (target.exists() && target.isDirectory()) {
            for (File file : target.listFiles()) {
                file.delete();
            }
        } else {
            target.mkdirs();
        }

        if (original.exists() && original.isDirectory()) {
            for (File file : original.listFiles()) {
                file.renameTo(new File(path + File.separator + file.getName()));
            }
        }

        if (!property.exists()) {
            try {
                property.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        imageCachePath = path;
        return true;
    }

    /**
     * Set songCachePath
     *
     * @param path new path
     * @return true if the setting is ok, otherwise false
     */
    public static Boolean setSongCachePath(String path) {
        if (!path.endsWith("/")) {
            return false;
        }

        File original = new File(songCachePath);
        File target = new File(path);
        File property = new File(path + cachePropertiesFilename);

        if (original.equals(target)) {
            if (original.exists() && original.isDirectory()) {
                if (!property.exists()) {
                    try {
                        property.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        }

        if (target.exists() && target.isDirectory()) {
            for (File file : target.listFiles()) {
                file.delete();
            }
        } else {
            target.mkdirs();
        }

        if (original.exists() && original.isDirectory()) {
            for (File file : original.listFiles()) {
                file.renameTo(new File(path + File.separator + file.getName()));
            }
        }

        if (!property.exists()) {
            try {
                property.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        songCachePath = path;
        return true;
    }

    /**
     * Enable proxy
     *
     * @return isProxyEnabled
     */
    public static Boolean enableProxy() {
        return isProxyEnabled = isProxyAvailable();
    }

    /**
     * Check if the proxy is available
     *
     * @return isProxyAvailable
     */
    private static Boolean isProxyAvailable() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection connection = Jsoup.connect(PROXY_TEST_ADDRESS)
                        .proxy(PROXY_ADDRESS, PROXY_PORT)
                        .timeout(10 * SECOND);
                try {
                    connection.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    isProxyEnabled = false;
                }
            }
        }).start();

        return true;
    }

    /**
     * Get connection for class LyricCacher
     *
     * @param ref reference
     * @param id  id
     * @return connection
     */
    public static Connection connectionForLyricCacher(int ref, String id) {
        return Jsoup.connect(SOURCE[ref][URL_FOR_LYRIC])
                .userAgent(USER_AGENT)
                .maxBodySize(DEFAULT)
                .ignoreContentType(true)
                .headers(headersFor(ref))
                .data(dataForLyricCacher(ref, id));
    }

    /**
     * Get connection for class SearchBrowser
     *
     * @param urlIndex  url index
     * @param dataIndex data index
     * @param ref       reference
     * @param keyword   keyword
     * @param page      page index
     * @return connection
     */
    public static Connection connectionForSearchService(int urlIndex, int dataIndex, int ref, String keyword, int page) {
        Connection connection = Jsoup.connect(SOURCE[ref][urlIndex]);
        connection.userAgent(USER_AGENT)
                .maxBodySize(DEFAULT)
                .ignoreContentType(true)
                .headers(headersFor(ref))
                .data(dataForSearchService(dataIndex, ref, keyword, page,
                        urlIndex == URL_FOR_SEARCH_PLAYLIST));
        return connection;
    }

    /**
     * Get album connection for class Artist
     *
     * @param ref  reference
     * @param id   id
     * @param page page index
     * @return connection
     */
    public static Connection albumConnectionForArtist(int ref, String id, int page) {
        Connection connection = Jsoup.connect(SOURCE[ref][URL_FOR_ARTIST_ALBUM].replace(ID_HOLDER, id));
        connection.userAgent(USER_AGENT)
                .maxBodySize(DEFAULT)
                .ignoreContentType(true)
                .headers(headersFor(ref))
                .data(albumDataForArtist(ref, id, page));
        return connection;
    }

    /**
     * Get song connection for class Artist
     *
     * @param ref  reference
     * @param id   id
     * @param page page index
     * @return connection
     */
    public static Connection songConnectionForArtist(int ref, String id, int page) {
        Connection connection = Jsoup.connect(SOURCE[ref][URL_FOR_ARTIST_SONG].replace(ID_HOLDER, id));
        connection.userAgent(USER_AGENT)
                .maxBodySize(DEFAULT)
                .ignoreContentType(true)
                .headers(headersFor(ref))
                .data(songDataForArtist(ref, id, page));
        return connection;
    }

    /**
     * Get connection for class Album
     *
     * @param ref reference
     * @param id  id
     * @return connection
     */
    public static Connection connectionForAlbum(int ref, String id) {
        Connection connection = Jsoup.connect(SOURCE[ref][URL_FOR_ALBUM_SONG].replace(ID_HOLDER, id));
        connection.userAgent(USER_AGENT)
                .maxBodySize(DEFAULT)
                .ignoreContentType(true)
                .headers(headersFor(ref))
                .data(dataForAlbum(ref, id));
        return connection;
    }

    /**
     * Get connection for class Playlist
     *
     * @param ref reference
     * @param id  id
     * @return connection
     */
    public static Connection connectionForPlaylist(int ref, String id) {
        Connection connection = Jsoup.connect(SOURCE[ref][URL_FOR_PLAYLIST_SONG].replace(ID_HOLDER, id));
        connection.userAgent(USER_AGENT)
                .maxBodySize(DEFAULT)
                .ignoreContentType(true)
                .headers(headersFor(ref))
                .data(dataForPlaylist(ref, id));
        return connection;
    }

    /**
     * Get connection for class PlaylistBrowser
     *
     * @param ref   reference
     * @param tagId tag id
     * @param page  page index
     * @return connection
     */
    public static Connection connectionForPlaylistBrowser(int ref, String tagId, int page) {
        Connection connection;
        if (tagId == null) { // Get Tags
            connection = Jsoup.connect(SOURCE[ref][URL_FOR_PLAYLIST_TAGS]);
        } else { // Get Playlists
            connection = Jsoup.connect(SOURCE[ref][URL_FOR_PLAYLIST_BROWSE]);
        }
        connection.userAgent(USER_AGENT)
                .maxBodySize(DEFAULT)
                .ignoreContentType(true)
                .headers(headersFor(ref))
                .data(dataForPlaylistBrowser(ref, tagId, page));
        return connection;
    }

    /**
     * Get path connection for the selected song
     *
     * @param ref selected song reference
     * @param id  selected song id
     * @return path connection
     */
    public static Connection pathConnectionFor(int ref, String id) {
        Connection connection = Jsoup.connect(SOURCE[ref][URL_FOR_PATH].replace(ID_HOLDER, id));
        if (isProxyEnabled) {
            connection.proxy(PROXY_ADDRESS, PROXY_PORT);
        }
        connection.userAgent(USER_AGENT)
                .maxBodySize(DEFAULT)
                .ignoreContentType(true)
                .headers(headersFor(ref))
                .data(pathDataFor(ref, id));
        return connection;
    }

    /**
     * Get connection data for class LyricCacher
     *
     * @param ref reference
     * @param id  id
     * @return connection data
     */
    private static Map<String, String> dataForLyricCacher(int ref, String id) {
        Map<String, String> pathData = new HashMap<>();
        String data = SOURCE[ref][DATA_FOR_LYRIC].replace(ID_HOLDER, id); // Fill id
        String[] tags = data.split(DATA_TAGS_DELIMITER);
        for (String tag : tags) { // Split by tags
            String[] headBody = tag.split(DATA_HEAD_BODY_DELIMITER);
            pathData.put(headBody[0], headBody[1]); // Split by head/body
        }
        return pathData;
    }

    /**
     * Get connection data for class SearchBrowser
     *
     * @param index       index
     * @param ref         reference
     * @param keyword     keyword
     * @param page        page index
     * @param flag4QqPage toggle different page calculation for QQ
     * @return connection data
     */
    private static Map<String, String> dataForSearchService(int index, int ref, String keyword, int page, Boolean flag4QqPage) {
        Map<String, String> pathData = new HashMap<>();
        String data = SOURCE[ref][index].replace(KEYWORD_HOLDER, keyword); // Fill keyword
        switch (ref) {
            case REF_QQ:
                if (flag4QqPage) {
                    data = data.replace(PAGE_HOLDER, String.format("%d", page));
                } else {
                    data = data.replace(PAGE_HOLDER, String.format("%d", page + 1));
                }
                break;
            case REF_163:
                data = data.replace(PAGE_HOLDER, String.format("%d", page * PAGE_ITEM_LIMIT));
        }
        String[] tags = data.split(DATA_TAGS_DELIMITER);
        for (String tag : tags) { // Split by tags
            String[] headBody = tag.split(DATA_HEAD_BODY_DELIMITER);
            pathData.put(headBody[0], headBody[1]); // Split by head/body
        }
        return pathData;
    }

    /**
     * Get album connection data for class Artist
     *
     * @param ref  reference
     * @param id   id
     * @param page page index
     * @return connection data
     */
    private static Map<String, String> albumDataForArtist(int ref, String id, int page) {
        Map<String, String> pathData = new HashMap<>();
        String data = SOURCE[ref][DATA_FOR_ARTIST_ALBUM].replace(ID_HOLDER, id); // Fill id
        switch (ref) { // Fill page
            case REF_QQ:
            case REF_163:
                data = data.replace(PAGE_HOLDER, String.format("%d", page * PAGE_ITEM_LIMIT));
        }
        String[] tags = data.split(DATA_TAGS_DELIMITER);
        for (String tag : tags) { // Split by tags
            String[] headBody = tag.split(DATA_HEAD_BODY_DELIMITER);
            pathData.put(headBody[0], headBody[1]); // Split by head/body
        }
        return pathData;
    }

    /**
     * Get song connection data for class Artist
     *
     * @param ref  reference
     * @param id   id
     * @param page page index
     * @return connection data
     */
    private static Map<String, String> songDataForArtist(int ref, String id, int page) {
        Map<String, String> pathData = new HashMap<>();
        String data = SOURCE[ref][DATA_FOR_ARTIST_SONG].replace(ID_HOLDER, id); // Fill id
        switch (ref) { // Fill page
            case REF_QQ:
            case REF_163:
                data = data.replace(PAGE_HOLDER, String.format("%d", page * PAGE_ITEM_LIMIT));
        }
        String[] tags = data.split(DATA_TAGS_DELIMITER);
        for (String tag : tags) { // Split by tags
            String[] headBody = tag.split(DATA_HEAD_BODY_DELIMITER);
            pathData.put(headBody[0], headBody[1]); // Split by head/body
        }
        return pathData;
    }

    /**
     * Get connection data for class Album
     *
     * @param ref reference
     * @param id  id
     * @return connection data
     */
    private static Map<String, String> dataForAlbum(int ref, String id) {
        Map<String, String> pathData = new HashMap<>();
        String data = SOURCE[ref][DATA_FOR_ALBUM_SONG].replace(ID_HOLDER, id); // Fill id
        String[] tags = data.split(DATA_TAGS_DELIMITER);
        for (String tag : tags) { // Split by tags
            String[] headBody = tag.split(DATA_HEAD_BODY_DELIMITER);
            pathData.put(headBody[0], headBody[1]); // Split by head/body
        }
        return pathData;
    }

    /**
     * Get connection data for class Playlist
     *
     * @param ref reference
     * @param id  id
     * @return connection data
     */
    private static Map<String, String> dataForPlaylist(int ref, String id) {
        Map<String, String> pathData = new HashMap<>();
        String data = SOURCE[ref][DATA_FOR_PLAYLIST_SONG].replace(ID_HOLDER, id); // Fill id
        String[] tags = data.split(DATA_TAGS_DELIMITER);
        for (String tag : tags) { // Split by tags
            String[] headBody = tag.split(DATA_HEAD_BODY_DELIMITER);
            pathData.put(headBody[0], headBody[1]); // Split by head/body
        }
        return pathData;
    }

    /**
     * Get connection data for class PlaylistBrowser
     *
     * @param ref   reference
     * @param tagId tag id
     * @param page  page index
     * @return connection data
     */
    private static Map<String, String> dataForPlaylistBrowser(int ref, String tagId, int page) {
        Map<String, String> pathData = new HashMap<>();
        String data;
        if (tagId == null) { // Get Tags
            data = SOURCE[ref][DATA_FOR_PLAYLIST_TAGS];
        } else { // Get Playlists
            data = SOURCE[ref][DATA_FOR_PLAYLIST_BROWSE].replace(TAG_HOLDER, tagId);
            switch (ref) {
                case REF_QQ:
                    data = data.replace(PAGE_HOLDER, String.format("%d", page * PAGE_ITEM_LIMIT));
                    data = data.replace(EXTRA_HOLDER, String.format("%d", (page + 1) * PAGE_ITEM_LIMIT - 1));
                    break;
                case REF_163:
                    data = data.replace(PAGE_HOLDER, String.format("%d", page * PAGE_ITEM_LIMIT));
            }
        }
        String[] tags = data.split(DATA_TAGS_DELIMITER);
        for (String t : tags) { // Split by tags
            String[] headBody = t.split(DATA_HEAD_BODY_DELIMITER);
            pathData.put(headBody[0], headBody[1]); // Split by head/body
        }
        return pathData;
    }

    /**
     * Get path connection data for the selected song
     *
     * @param ref selected song reference
     * @param id  selected song id
     * @return connection data
     */
    private static Map<String, String> pathDataFor(int ref, String id) {
        Map<String, String> pathData = new HashMap<>();
        String data = SOURCE[ref][DATA_FOR_PATH].replace(ID_HOLDER, id); // Fill id
        String[] tags = data.split(DATA_TAGS_DELIMITER);
        for (String tag : tags) { // Split by tags
            String[] headBody = tag.split(DATA_HEAD_BODY_DELIMITER);
            pathData.put(headBody[0], headBody[1]); // Split by head/body
        }
        return pathData;
    }

    /**
     * Get headers for the selected reference
     *
     * @param ref selected reference
     * @return headers
     */
    public static Map<String, String> headersFor(int ref) {
        Map<String, String> headers = new HashMap<>();
        String[] tags = SOURCE[ref][HEADERS].split(HEADERS_TAGS_DELIMITER);
        for (String tag : tags) { // Split by tags
            String[] headBody = tag.split(HEADERS_HEAD_BODY_DELIMITER);
            headers.put(headBody[0], headBody[1]); // Split by head/body
        }
        return headers;
    }

    /**
     * trustEveryone
     */
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
