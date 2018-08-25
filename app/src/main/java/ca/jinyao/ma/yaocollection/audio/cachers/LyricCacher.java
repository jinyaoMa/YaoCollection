package ca.jinyao.ma.yaocollection.audio.cachers;

import android.os.AsyncTask;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import ca.jinyao.ma.yaocollection.audio.components.Lyric;

import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.CACHE_MIN_FREE_SPACE;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.REF_163;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.REF_QQ;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.TAG;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.cachePropertiesFilename;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.connectionForLyricCacher;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.lyricCacheLimit;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.lyricCachePath;

/**
 * Class LyricCacher
 * create by jinyaoMa 0010 2018/8/10 0:18
 */
public class LyricCacher {
    private static final String ORIGINAL = "original";
    private static final String TRANSLATE = "translate";
    private static LyricCacheListener lyricCacheListener;

    public interface LyricCacheListener {
        void onCompleted(Lyric original, Lyric translate);
    }

    private LyricCacher(LyricCacheListener lyricCacheListener) {
        this.lyricCacheListener = lyricCacheListener;
    }

    public static void getLyric(int ref, String id, LyricCacheListener lyricCacheListener) {
        LyricCacher lyricCacher = new LyricCacher(lyricCacheListener);

        CacheProperties lyricCache = CacheProperties.getInstance(lyricCachePath, cachePropertiesFilename);
        String filename = lyricCache.get(String.format("%d/%s/%s", ref, TAG, id));
        File file = new File(lyricCachePath + filename);
        if (file.exists() && file.isFile()) {
            Map<String, String> lyric = new Gson().fromJson(lyricCacher.getLyric(file), Map.class);
            lyricCacher.localComplete(new Lyric(lyric.get(ORIGINAL)), new Lyric(lyric.get(TRANSLATE)));
        } else {
            lyricCacher.download(ref, id);
        }
    }

    private void localComplete(Lyric orig, Lyric trans) {
        lyricCacheListener.onCompleted(orig, trans);
    }

    private void saveLyric(String path, String data) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(data);

            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getLyric(File target) {
        String laststr = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(target);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String tempString;
            while ((tempString = bufferedReader.readLine()) != null) {
                laststr += tempString;
            }

            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return laststr;
    }

    private void download(int ref, String id) {
        DownloadLyricTask downloadLyricTask = new DownloadLyricTask();
        downloadLyricTask.execute(ref, id);
    }

    private class DownloadLyricTask extends AsyncTask<Object, Map<String, String>, Map<String, String>> {
        @Override
        protected Map<String, String> doInBackground(Object... objects) {
            Map<String, String> lyric = new HashMap<>();

            if (objects.length == 2) {
                int ref = (int) objects[0];
                String id = (String) objects[1];
                Connection connection = connectionForLyricCacher(ref, id);

                try {
                    if (ref == REF_QQ) {
                        String response = connection.execute().body();
                        if (response.startsWith(TAG)) {
                            response = response.replaceFirst(TAG, "");
                            response = response.substring(1, response.length() - 1);
                        }
                        JSONObject jsonObject = new JSONObject(response);
                        lyric.put(ORIGINAL, jsonObject.optString("lyric"));
                        lyric.put(TRANSLATE, jsonObject.optString("trans"));
                    } else if (ref == REF_163) {
                        JSONObject jsonObject = new JSONObject(connection.execute().body());
                        lyric.put(ORIGINAL, jsonObject.optJSONObject("lrc").optString("lyric"));
                        lyric.put(TRANSLATE, jsonObject.optJSONObject("tlyric").optString("lyric"));
                    }

                    File saveDir = new File(lyricCachePath);
                    if (!saveDir.exists()) {
                        saveDir.mkdirs();
                    } else {
                        if (saveDir.getFreeSpace() <= CACHE_MIN_FREE_SPACE) {
                            return lyric;
                        }
                    }

                    CacheProperties cacheProperties = CacheProperties.getInstance(lyricCachePath, cachePropertiesFilename);
                    if (cacheProperties.size() >= lyricCacheLimit) {
                        int removeNumber = cacheProperties.size() - lyricCacheLimit + 1;
                        for (Object key : cacheProperties.keySet()) {
                            String oldValue = cacheProperties.remove(key);
                            new File(lyricCachePath + oldValue).delete();

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

                    filename = String.format("%d", Integer.parseInt(filename) + 1);
                    saveLyric(lyricCachePath + filename, new Gson().toJson(lyric));
                    cacheProperties.put(String.format("%d/%s/%s", ref, TAG, id), filename);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return lyric;
        }

        @Override
        protected void onPostExecute(Map<String, String> lyric) {
            if (lyric != null && !lyric.isEmpty()) {
                lyricCacheListener.onCompleted(new Lyric(lyric.get(ORIGINAL)), new Lyric(lyric.get(TRANSLATE)));
            }
        }
    }
}
