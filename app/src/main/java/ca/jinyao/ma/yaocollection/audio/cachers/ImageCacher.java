package ca.jinyao.ma.yaocollection.audio.cachers;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;

import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.CACHE_MIN_FREE_SPACE;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.SECOND;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.TAG;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.USER_AGENT;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.cachePropertiesFilename;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.imageCacheLimit;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.imageCachePath;

/**
 * Class ImageCacher
 * create by jinyaoMa 0010 2018/8/10 0:20
 */
public class ImageCacher {
    private static Queue<ImageCacher> queue = new LinkedList<>();

    private ImageCacheListener imageCacheListener;
    private String path;

    public interface ImageCacheListener {
        void onCompleted(Bitmap bitmap, String path);
    }

    private ImageCacher(ImageCacheListener imageCacheListener) {
        this.imageCacheListener = imageCacheListener;
    }

    public static void getImage(String path, ImageCacheListener imageCacheListener) {
        ImageCacher imageCacher = new ImageCacher(imageCacheListener);
        imageCacher.setPath(path);
        if (queue.isEmpty()) {
            imageCacher.process();
        } else {
            queue.offer(imageCacher);
        }
    }

    private void setPath(String path) {
        this.path = path;
    }

    private void process() {
        CacheProperties imageCache = CacheProperties.getInstance(imageCachePath, cachePropertiesFilename);
        String filename = imageCache.get(path);
        File file = new File(imageCachePath + filename);
        if (file.exists() && file.isFile()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            localComplete(bitmap, file);
        } else {
            download(path);
        }
    }

    private void localComplete(Bitmap bitmap, File file) {
        imageCacheListener.onCompleted(bitmap, file.getPath());
        if (!queue.isEmpty()) {
            queue.poll().process();
        }
    }

    private void download(String path) {
        DownloadImageTask downloadImageTask = new DownloadImageTask();
        downloadImageTask.execute(path);
    }

    private class DownloadImageTask extends AsyncTask<String, Bitmap, Bitmap> {
        private String path;

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;

            if (strings.length == 1) {
                try {
                    String path = strings[0];
                    URL url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(30 * SECOND);
                    connection.setRequestProperty("User-Agent", USER_AGENT);

                    InputStream inputStream = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);

                    File saveDir = new File(imageCachePath);
                    if (!saveDir.exists()) {
                        saveDir.mkdirs();
                    } else {
                        if (saveDir.getFreeSpace() <= CACHE_MIN_FREE_SPACE) {
                            this.path = path;
                            return bitmap;
                        }
                    }

                    CacheProperties cacheProperties = CacheProperties.getInstance(imageCachePath, cachePropertiesFilename);
                    if (cacheProperties.size() >= imageCacheLimit) {
                        int removeNumber = cacheProperties.size() - imageCacheLimit + 1;
                        for (Object key : cacheProperties.keySet()) {
                            String oldValue = cacheProperties.remove(key);
                            new File(imageCachePath + oldValue).delete();

                            removeNumber -= 1;
                            if (removeNumber <= 0) {
                                break;
                            }
                        }
                    }

                    int filename = 0;
                    for (Object value : cacheProperties.values()) {
                        int n = Integer.parseInt((String) value);
                        if (filename < n) {
                            filename = n;
                        }
                    }

                    File file = new File(imageCachePath + String.format("%d", filename + 1));
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                    cacheProperties.put(path, file.getName());
                    this.path = file.getPath();

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

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageCacheListener.onCompleted(bitmap, path);
            }
            if (!queue.isEmpty()) {
                queue.poll().process();
            }
        }
    }
}
