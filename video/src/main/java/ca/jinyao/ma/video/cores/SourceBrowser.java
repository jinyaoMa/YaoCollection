package ca.jinyao.ma.video.cores;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.jinyao.ma.video.components.Catalogue;
import ca.jinyao.ma.video.components.CatalogueList;
import ca.jinyao.ma.video.components.CatalogueTable;
import ca.jinyao.ma.video.components.Video;
import ca.jinyao.ma.video.components.VideoList;

import static ca.jinyao.ma.video.cores.VideoConfig.PAGE_HOLDER;

/**
 * class SourceBrowser
 * create by jinyaoMa 0001 2018/9/1 21:29
 */
public class SourceBrowser {
    public final String TAG = "SourceBrowser";
    private final int FIRST = 1;

    public interface Listener {
        void onGetSourceComplete(CatalogueTable catalogueTable, VideoList videoList, int nextPage, Boolean hasNext);

        void onError();
    }

    private Listener listener;
    private String currentType;
    private String currentUrl;
    private CatalogueTable currentCatalogueTable;
    private VideoList currentVideoList;
    private int currentPage;
    private int currentMaxPage;
    private Boolean isByType;

    public SourceBrowser(@NonNull Listener listener) {
        this.listener = listener;
        currentType = VideoConfig.TYPE_ANIME;
        currentUrl = VideoConfig.URL_ANIME;
        currentCatalogueTable = new CatalogueTable();
        currentVideoList = new VideoList();
        currentPage = FIRST;
        currentMaxPage = FIRST;
        isByType = true;
    }

    public String getCurrentType() {
        return currentType;
    }

    public void getByType(String type) {
        isByType = true;
        currentType = type;
        switch (currentType) {
            case VideoConfig.TYPE_MOVIE:
                currentUrl = VideoConfig.URL_MOVIE;
                break;
            case VideoConfig.TYPE_TVP:
                currentUrl = VideoConfig.URL_TVP;
                break;
            case VideoConfig.TYPE_ANIME:
                currentUrl = VideoConfig.URL_ANIME;
        }
        new GetSourceTask().execute();
    }

    public void getByUrl(@NonNull String url) {
        isByType = true;
        Log.e(TAG, url);
        if (url.startsWith("http")) {
            currentUrl = url;
            if (url.contains(VideoConfig.TYPE_MOVIE)) {
                currentType = VideoConfig.TYPE_MOVIE;
            } else if (url.contains(VideoConfig.TYPE_TVP)) {
                currentType = VideoConfig.TYPE_TVP;
            } else if (url.contains(VideoConfig.TYPE_ANIME)) {
                currentType = VideoConfig.TYPE_ANIME;
            }
            currentPage = FIRST;
            new GetSourceTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            listener.onError();
        }
    }

    public void getByUrl(@NonNull String url, int page) {
        isByType = false;
        Log.e(TAG, url);
        if (url.startsWith("http")) {
            currentUrl = url.split(PAGE_HOLDER)[0];
            if (url.contains(VideoConfig.TYPE_MOVIE)) {
                currentType = VideoConfig.TYPE_MOVIE;
            } else if (url.contains(VideoConfig.TYPE_TVP)) {
                currentType = VideoConfig.TYPE_TVP;
            } else if (url.contains(VideoConfig.TYPE_ANIME)) {
                currentType = VideoConfig.TYPE_ANIME;
            }
            if (page <= currentMaxPage) {
                currentPage = page;
            }
            new GetSourceTask().execute();
        } else {
            listener.onError();
        }
    }

    private class GetSourceTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            currentCatalogueTable.clear();
            if (isByType) {
                currentVideoList.clear();
                currentPage = FIRST;
                currentMaxPage = FIRST;
            }
            currentUrl += PAGE_HOLDER + currentPage;

            Connection connection = VideoConfig.getConnectionFor(currentUrl);

            try {
                Element body = connection.get().body();

                Elements tagLineList = body.select("#block3 #block1").first().select(".tags.tlist").not(".tlisth");
                for (Element tagLine : tagLineList) {
                    Elements tags = tagLine.select("li").not(".dt");
                    CatalogueList catalogues = new CatalogueList();

                    catalogues.setTag(tagLine.selectFirst("li.dt").text());
                    for (Element tag : tags) {
                        String name = tag.selectFirst("a").ownText();
                        String url = VideoConfig.URL_BASE + tag.selectFirst("a").attr("href");
                        Catalogue catalogue = new Catalogue(name, url);
                        catalogue.setSelected(currentUrl.startsWith(url));
                        catalogues.add(catalogue);
                    }

                    currentCatalogueTable.add(catalogues);
                }

                Elements videoList = body.select("#block3 #block1").last().select("ul.me1 li");
                for (Element v : videoList) {
                    String url = VideoConfig.URL_BASE + v.selectFirst("a").attr("href");
                    String coverPath = v.selectFirst("a img").attr("_src");
                    String title = v.selectFirst("a").attr("title");
                    String message = v.selectFirst("span.tip").text().trim();
                    String ico = "";
                    if (v.selectFirst(".ico1") != null) {
                        ico = v.selectFirst(".ico1").text().trim();
                    }

                    Video video = new Video(currentType, url, coverPath, title, message, ico);
                    currentVideoList.add(video);
                }

                Element pager = body.select("#block3 #block1").last().selectFirst("div.pager");
                if (pager != null && pager.select("a").size() > 0) {
                    String[] temp = pager.select("a").last().attr("href").split(PAGE_HOLDER);
                    if (temp.length == 2 && !temp[1].isEmpty()) {
                        currentMaxPage = Integer.parseInt(temp[1]);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                currentCatalogueTable.clear();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (currentCatalogueTable.isEmpty()) {
                listener.onError();
            } else {
                listener.onGetSourceComplete(currentCatalogueTable, currentVideoList, currentPage + 1, currentPage < currentMaxPage);
            }
        }
    }
}
