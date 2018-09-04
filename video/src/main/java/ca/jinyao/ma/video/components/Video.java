package ca.jinyao.ma.video.components;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;

import ca.jinyao.ma.video.cores.VideoConfig;

/**
 * Class Video
 * create by jinyaoMa 0031 2018/8/31 21:30
 */
public class Video {
    private String type;
    private String url;

    private String coverPath;
    private String title;
    private String message;
    private String ico;

    private Listener listener;

    public Video(@NonNull String type, @NonNull String url, String coverPath, String title, String message, String ico) {
        this.type = type;
        this.url = url + "/bd-2";
        this.coverPath = (coverPath == null) ? "" : coverPath;
        this.title = (title == null) ? "" : title;
        this.message = (message == null) ? "" : message;
        this.ico = (ico == null) ? "" : ico;
    }

    public void loadDetail() {
        LoadTask loadTask = new LoadTask();
        loadTask.execute();

        if (listener != null) {
            listener.onDetailLoading();
        }
    }

    private class LoadTask extends AsyncTask<String, EpisodeList, EpisodeList> {
        @Override
        protected EpisodeList doInBackground(String... strings) {
            EpisodeList episodes = new EpisodeList();

            Connection connection = VideoConfig.getConnectionFor(url);
            try {
                Element body = connection.get().body();
                for (Element e : body.select("#myform li").not(".nohover")) {
                    String name = e.selectFirst(".dlname a").text();
                    String size = StringEscapeUtils.unescapeHtml4(e.selectFirst(".dlname span").ownText()).trim();
                    String downloadUrl = e.selectFirst(".dlname a").attr("thunderHref");
                    Episode episode = new Episode(name, size, downloadUrl);
                    episodes.add(episode);
                }

            }catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return episodes;
        }

        @Override
        protected void onPostExecute(EpisodeList episodes) {
            if (listener != null) {
                if (episodes != null) {
                    listener.onDetailLoaded(episodes);
                } else {
                    listener.onError();
                }
            }
        }
    }

    public interface Listener {
        void onDetailLoading();
        void onDetailLoaded(EpisodeList episodes);
        void onError();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getIco() {
        return ico;
    }
}
