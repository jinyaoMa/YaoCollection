package ca.jinyao.ma.video.cores;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.jinyao.ma.video.components.Catalogue;
import ca.jinyao.ma.video.components.CatalogueList;
import ca.jinyao.ma.video.components.CatalogueTable;
import ca.jinyao.ma.video.components.Video;
import ca.jinyao.ma.video.components.VideoList;

/**
 * class SourceBrowser
 * create by jinyaoMa 0001 2018/9/1 21:29
 */
public class SourceBrowser {
    public final String TAG = "SourceBrowser";
    public final int FIRST = 1;

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

    public void getByUrl(String url, int page) {
        isByType = false;
        if (url.startsWith("http")) {
            currentUrl = url;
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
                        catalogue.setSelected(tag.hasClass("selected"));
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
                    String ico = v.selectFirst("span.ico1").text().trim();

                    Video video = new Video(currentType, url, coverPath, title, message, ico);
                    currentVideoList.add(video);
                }

                Element pager = body.select("#block3 #block1").last().selectFirst("div.pager");
                if (pager.select("a").size() > 0) {
                    String[] temp = pager.select("a").last().attr("href").split(VideoConfig.PAGE_HOLDER);
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
