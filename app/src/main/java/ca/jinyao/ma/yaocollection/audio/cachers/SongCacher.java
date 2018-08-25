package ca.jinyao.ma.yaocollection.audio.cachers;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.CACHE_MIN_FREE_SPACE;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.NONE;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.REF_163;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.REF_QQ;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.TAG;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.cachePropertiesFilename;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.headersFor;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.pathConnectionFor;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.songCacheLimit;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.songCachePath;

/**
 * Class SongCacher
 * create by jinyaoMa 0010 2018/8/10 0:13
 */
public class SongCacher {
    private static SongCacheListener songCacheListener;

    public interface SongCacheListener {
        void onCompleted(String songPath);

        void onError();
    }

    private SongCacher(SongCacheListener songCacheListener) {
        this.songCacheListener = songCacheListener;
    }

    public static void getSong(int ref, String id, SongCacheListener songCacheListener) {
        SongCacher songCacher = new SongCacher(songCacheListener);

        CacheProperties songCache = CacheProperties.getInstance(songCachePath, cachePropertiesFilename);
        String filename = songCache.get(String.format("%d/%s/%s", ref, TAG, id));
        File file = new File(songCachePath + filename);
        if (file.exists() && file.isFile()) {
            songCacheListener.onCompleted(file.getPath());
        } else {
            songCacher.download(ref, id, false);
        }
    }

    public static void loadSong(int ref, String id) {
        SongCacher songCacher = new SongCacher(songCacheListener);

        CacheProperties songCache = CacheProperties.getInstance(songCachePath, cachePropertiesFilename);
        String filename = songCache.get(String.format("%d/%s/%s", ref, TAG, id));
        File file = new File(songCachePath + filename);
        if (!file.exists() || !file.isFile()) {
            songCacher.download(ref, id, true);
        }
    }

    private void download(int ref, String id, Boolean isPreload) {
        DownloadSongTask downloadSongTask = new DownloadSongTask();
        downloadSongTask.execute(ref, id, isPreload);
    }

    private class DownloadSongTask extends AsyncTask<Object, String, String> {
        private int ref;
        private String id;
        private Boolean isPreload;

        @Override
        protected String doInBackground(Object... objects) {
            String path = null;

            if (objects.length >= 2) {
                isPreload = false;
                if (objects.length == 3) {
                    isPreload = (Boolean) objects[2];
                }
                ref = (int) objects[0];
                id = (String) objects[1];
                Connection connection = pathConnectionFor(ref, id);

                try {
                    if (ref == REF_QQ) {
                        JSONObject jsonObject = new JSONObject(connection.execute().body())
                                .optJSONObject("data").optJSONArray("items").optJSONObject(0);
                        path = "http://dl.stream.qqmusic.qq.com/" + jsonObject.optString("filename") + "?vkey=" + jsonObject.optString("vkey") + "&guid=-1";
                    } else if (ref == REF_163) {
                        JSONObject jsonObject = new JSONObject(connection.execute().body())
                                .optJSONArray("data").optJSONObject(0);
                        path = jsonObject.optString("url");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return path;
        }

        @Override
        protected void onPostExecute(final String path) {
            if (path != null) {
                if (!isPreload) {
                    songCacheListener.onCompleted(path);
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(path);
                            URLConnection urlConnection = url.openConnection();
                            Map<String, String> headers = headersFor(ref);
                            for (String key : headers.keySet()) {
                                urlConnection.addRequestProperty(key, headers.get(key));
                            }

                            File saveDir = new File(songCachePath);
                            if (!saveDir.exists()) {
                                saveDir.mkdirs();
                            } else {
                                if (saveDir.getFreeSpace() <= CACHE_MIN_FREE_SPACE) {
                                    return;
                                }
                            }

                            CacheProperties cacheProperties = CacheProperties.getInstance(songCachePath, cachePropertiesFilename);
                            if (cacheProperties.size() >= songCacheLimit) {
                                int removeNumber = cacheProperties.size() - songCacheLimit + 1;
                                for (Object key : cacheProperties.keySet()) {
                                    String oldValue = cacheProperties.remove(key);
                                    new File(songCachePath + oldValue).delete();

                                    removeNumber -= 1;
                                    if (removeNumber <= 0) {
                                        break;
                                    }
                                }
                            }

                            String filename = "0";
                            for (Object value : cacheProperties.values()) {
                                if (filename.compareTo((String) value) < 0) {
                                    filename = (String) value;
                                }
                            }

                            InputStream inputStream = urlConnection.getInputStream();
                            File file = new File(songCachePath + (Integer.parseInt(filename) + 1));
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            OutputStream outputStream = new BufferedOutputStream(fileOutputStream);

                            int buffer;
                            while ((buffer = inputStream.read()) != NONE) {
                                outputStream.write(buffer);
                            }

                            cacheProperties.put(String.format("%d/%s/%s", ref, TAG, id), file.getName());

                            if (outputStream != null) {
                                outputStream.close();
                            }
                            if (fileOutputStream != null) {
                                fileOutputStream.close();
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                songCacheListener.onError();
            }
        }
    }
}
