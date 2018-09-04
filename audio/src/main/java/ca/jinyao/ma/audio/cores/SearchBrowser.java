package ca.jinyao.ma.audio.cores;

import android.os.AsyncTask;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;

import java.io.IOException;

import ca.jinyao.ma.audio.components.Album;
import ca.jinyao.ma.audio.components.AlbumList;
import ca.jinyao.ma.audio.components.Artist;
import ca.jinyao.ma.audio.components.ArtistList;
import ca.jinyao.ma.audio.components.Playlist;
import ca.jinyao.ma.audio.components.PlaylistList;
import ca.jinyao.ma.audio.components.Song;
import ca.jinyao.ma.audio.components.SongList;

import static ca.jinyao.ma.audio.cores.AudioConfig.DATA_FOR_SEARCH_ALBUM;
import static ca.jinyao.ma.audio.cores.AudioConfig.DATA_FOR_SEARCH_ARTIST;
import static ca.jinyao.ma.audio.cores.AudioConfig.DATA_FOR_SEARCH_PLAYLIST;
import static ca.jinyao.ma.audio.cores.AudioConfig.DATA_FOR_SEARCH_SONG;
import static ca.jinyao.ma.audio.cores.AudioConfig.NONE;
import static ca.jinyao.ma.audio.cores.AudioConfig.PAGE_ITEM_LIMIT;
import static ca.jinyao.ma.audio.cores.AudioConfig.REF_163;
import static ca.jinyao.ma.audio.cores.AudioConfig.REF_QQ;
import static ca.jinyao.ma.audio.cores.AudioConfig.TAB_ALBUM;
import static ca.jinyao.ma.audio.cores.AudioConfig.TAB_ARTIST;
import static ca.jinyao.ma.audio.cores.AudioConfig.TAB_PLAYLIST;
import static ca.jinyao.ma.audio.cores.AudioConfig.TAB_SONG;
import static ca.jinyao.ma.audio.cores.AudioConfig.URL_FOR_SEARCH_ALBUM;
import static ca.jinyao.ma.audio.cores.AudioConfig.URL_FOR_SEARCH_ARTIST;
import static ca.jinyao.ma.audio.cores.AudioConfig.URL_FOR_SEARCH_PLAYLIST;
import static ca.jinyao.ma.audio.cores.AudioConfig.URL_FOR_SEARCH_SONG;
import static ca.jinyao.ma.audio.cores.AudioConfig.connectionForSearchService;

/**
 * Class SearchBrowser
 * Contain basic service of searching
 */
public class SearchBrowser {
    private int reference;
    private int tab;

    public SearchBrowser(AsyncListener asyncListener) {
        this.asyncListener = asyncListener;
    }

    public void search(int reference, int tab, String keyword) {
        switch (reference) {
            case REF_QQ:
            case REF_163:
                this.reference = reference;
                this.tab = tab;
        }
        GetResultTask getResultTask = new GetResultTask();
        getResultTask.execute(keyword);
    }

    public void search(int reference, int tab, String keyword, int page) {
        switch (reference) {
            case REF_QQ:
            case REF_163:
                this.reference = reference;
                this.tab = tab;
        }
        GetResultTask getResultTask = new GetResultTask();
        getResultTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, keyword, page);
    }

    public void set(int reference, int tab) {
        this.reference = reference;
        this.tab = tab;
    }

    // Listener
    private AsyncListener asyncListener;

    public interface AsyncListener {
        void onSearchCompleted(SongList list, String keyword, int nextPage, Boolean hasNextPage);

        void onSearchCompleted(ArtistList list, String keyword, int nextPage, Boolean hasNextPage);

        void onSearchCompleted(AlbumList list, String keyword, int nextPage, Boolean hasNextPage);

        void onSearchCompleted(PlaylistList list, String keyword, int nextPage, Boolean hasNextPage);
    }

    // Async Task
    private class GetResultTask extends AsyncTask {
        private int pageNumber;
        private Boolean hasNextPage;
        private String keyword;

        @Override
        protected Object doInBackground(Object... objects) {
            Object result = new Object();

            keyword = "";
            if (objects.length > 0) {
                keyword = (String) objects[0];
            }

            int page = 0;
            if (objects.length > 1) {
                page = (int) objects[1];
                pageNumber = page;
            }

            try {
                switch (tab) {
                    case TAB_SONG:
                        result = getSong(page);
                        break;
                    case TAB_ALBUM:
                        result = getAlbum(page);
                        break;
                    case TAB_ARTIST:
                        result = getArtist(page);
                        break;
                    case TAB_PLAYLIST:
                        result = getPlaylist(page);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Object object) {
            if (object != null) {
                if (object instanceof SongList) {
                    asyncListener.onSearchCompleted((SongList) object, keyword, pageNumber, hasNextPage);
                } else if (object instanceof ArtistList) {
                    asyncListener.onSearchCompleted((ArtistList) object, keyword, pageNumber, hasNextPage);
                } else if (object instanceof AlbumList) {
                    asyncListener.onSearchCompleted((AlbumList) object, keyword, pageNumber, hasNextPage);
                } else if (object instanceof PlaylistList) {
                    asyncListener.onSearchCompleted((PlaylistList) object, keyword, pageNumber, hasNextPage);
                }
            }
        }

        private SongList getSong(int page) throws IOException, JSONException {
            SongList songlist = new SongList();

            Connection connection = connectionForSearchService(URL_FOR_SEARCH_SONG, DATA_FOR_SEARCH_SONG,
                    reference, keyword, page);
            JSONObject jsonObject;
            JSONArray jsonArray;
            int length;
            switch (reference) {
                case REF_QQ:
                    jsonObject = new JSONObject(connection.execute().body()).optJSONObject("data").optJSONObject("song");
                    jsonArray = jsonObject.optJSONArray("list");
                    length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject temp = jsonArray.optJSONObject(i);
                        JSONArray tmp = temp.optJSONArray("singer");

                        Song song = new Song(reference, temp.optString("songmid"), temp.optString("songname"));
                        song.setAlbum(temp.optString("albummid"), temp.optString("albumname"));
                        for (int j = 0; j < tmp.length(); j++) {
                            JSONObject artist = tmp.optJSONObject(j);
                            song.addArtist(artist.optString("mid"), artist.optString("name"), null);
                        }

                        songlist.add(song);
                    }

                    if ((pageNumber * PAGE_ITEM_LIMIT + PAGE_ITEM_LIMIT) < jsonObject.optInt("totalnum")) {
                        pageNumber += 1;
                        hasNextPage = true;
                    } else {
                        pageNumber = NONE;
                        hasNextPage = false;
                    }
                    break;
                case REF_163:
                    jsonObject = new JSONObject(connection.post().body().text()).optJSONObject("result");
                    jsonArray = jsonObject.optJSONArray("songs");
                    length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject temp = jsonArray.optJSONObject(i);
                        JSONArray tmp = temp.optJSONArray("artists");

                        Song song = new Song(reference, temp.optString("id"), temp.optString("name"));
                        song.setAlbum(temp.optJSONObject("album").optString("id"), temp.optJSONObject("album").optString("name"));
                        song.setCoverPath(temp.optJSONObject("album").optString("picUrl"));
                        for (int j = 0; j < tmp.length(); j++) {
                            JSONObject artist = tmp.optJSONObject(j);
                            song.addArtist(artist.optString("id"), artist.optString("name"), artist.optString("picUrl"));
                        }

                        songlist.add(song);
                    }

                    if ((pageNumber * PAGE_ITEM_LIMIT + PAGE_ITEM_LIMIT) < jsonObject.optInt("songCount")) {
                        pageNumber += 1;
                        hasNextPage = true;
                    } else {
                        pageNumber = NONE;
                        hasNextPage = false;
                    }
            }

            return songlist;
        }

        private AlbumList getAlbum(int page) throws IOException, JSONException {
            AlbumList albumlist = new AlbumList();

            Connection connection = connectionForSearchService(URL_FOR_SEARCH_ALBUM, DATA_FOR_SEARCH_ALBUM,
                    reference, keyword, page);
            JSONObject jsonObject;
            JSONArray jsonArray;
            int length;
            switch (reference) {
                case REF_QQ:
                    jsonObject = new JSONObject(connection.execute().body()).optJSONObject("data").optJSONObject("album");
                    jsonArray = jsonObject.optJSONArray("list");
                    length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject temp = jsonArray.optJSONObject(i);
                        JSONArray tmp = temp.optJSONArray("singer_list");

                        Album album = new Album(reference, temp.optString("albumMID"), temp.optString("albumName"));
                        for (int j = 0; j < tmp.length(); j++) {
                            JSONObject artist = tmp.optJSONObject(j);
                            album.addArtist(artist.optString("mid"), artist.optString("name"), null);
                        }

                        albumlist.add(album);
                    }

                    if ((pageNumber * PAGE_ITEM_LIMIT + PAGE_ITEM_LIMIT) < jsonObject.optInt("totalnum")) {
                        pageNumber += 1;
                        hasNextPage = true;
                    } else {
                        pageNumber = NONE;
                        hasNextPage = false;
                    }
                    break;
                case REF_163:
                    jsonObject = new JSONObject(connection.post().body().text()).optJSONObject("result");
                    jsonArray = jsonObject.optJSONArray("albums");
                    length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject temp = jsonArray.optJSONObject(i);
                        JSONArray tmp = temp.optJSONArray("artists");
                        String backupArtistCoverPath = temp.optJSONObject("artist").optString("picUrl");

                        Album album = new Album(reference, temp.optString("id"), temp.optString("name"));
                        album.coverPath = temp.optString("picUrl");
                        for (int j = 0; j < tmp.length(); j++) {
                            JSONObject artist = tmp.optJSONObject(j);
                            if (artist.optInt("picId") == 0) {
                                album.addArtist(artist.optString("id"), artist.optString("name"), backupArtistCoverPath);
                            } else {
                                album.addArtist(artist.optString("id"), artist.optString("name"), artist.optString("picUrl"));
                            }
                        }

                        albumlist.add(album);
                    }

                    if ((pageNumber * PAGE_ITEM_LIMIT + PAGE_ITEM_LIMIT) < jsonObject.optInt("albumCount")) {
                        pageNumber += 1;
                        hasNextPage = true;
                    } else {
                        pageNumber = NONE;
                        hasNextPage = false;
                    }
            }

            return albumlist;
        }

        private ArtistList getArtist(int page) throws IOException, JSONException {
            ArtistList artistlist = new ArtistList();

            Connection connection = connectionForSearchService(URL_FOR_SEARCH_ARTIST, DATA_FOR_SEARCH_ARTIST,
                    reference, keyword, page);
            JSONObject jsonObject;
            JSONArray jsonArray;
            int length;
            switch (reference) {
                case REF_QQ:
                    jsonObject = new JSONObject(connection.execute().body()).optJSONObject("data").optJSONObject("singer");
                    jsonArray = jsonObject.optJSONArray("list");
                    length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject temp = jsonArray.optJSONObject(i);

                        Artist artist = new Artist(reference, temp.optString("singerMID"), temp.optString("singerName"));

                        artistlist.add(artist);
                    }

                    if ((pageNumber * PAGE_ITEM_LIMIT + PAGE_ITEM_LIMIT) < jsonObject.optInt("totalnum")) {
                        pageNumber += 1;
                        hasNextPage = true;
                    } else {
                        pageNumber = NONE;
                        hasNextPage = false;
                    }
                    break;
                case REF_163:
                    jsonObject = new JSONObject(connection.post().body().text()).optJSONObject("result");
                    jsonArray = jsonObject.optJSONArray("artists");
                    length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject temp = jsonArray.optJSONObject(i);

                        Artist artist = new Artist(reference, temp.optString("id"), temp.optString("name"));
                        artist.coverPath = temp.optString("picUrl");

                        artistlist.add(artist);
                    }

                    if ((pageNumber * PAGE_ITEM_LIMIT + PAGE_ITEM_LIMIT) < jsonObject.optInt("artistCount")) {
                        pageNumber += 1;
                        hasNextPage = true;
                    } else {
                        pageNumber = NONE;
                        hasNextPage = false;
                    }
            }

            return artistlist;
        }

        private PlaylistList getPlaylist(int page) throws IOException, JSONException {
            PlaylistList playlists = new PlaylistList();

            Connection connection = connectionForSearchService(URL_FOR_SEARCH_PLAYLIST, DATA_FOR_SEARCH_PLAYLIST,
                    reference, keyword, page);
            JSONObject jsonObject;
            JSONArray jsonArray;
            int length;
            switch (reference) {
                case REF_QQ:
                    jsonObject = new JSONObject(connection.execute().body()).optJSONObject("data");
                    jsonArray = jsonObject.optJSONArray("list");
                    length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject temp = jsonArray.optJSONObject(i);

                        Playlist playlist = new Playlist(reference, temp.optString("dissid"), StringEscapeUtils.unescapeHtml4(temp.optString("dissname")), null);
                        playlist.setCover(temp.optString("imgurl"));

                        playlists.add(playlist);
                    }

                    if ((pageNumber * PAGE_ITEM_LIMIT + PAGE_ITEM_LIMIT) < jsonObject.optInt("sum")) {
                        pageNumber += 1;
                        hasNextPage = true;
                    } else {
                        pageNumber = NONE;
                        hasNextPage = false;
                    }
                    break;
                case REF_163:
                    jsonObject = new JSONObject(connection.post().body().text()).optJSONObject("result");
                    jsonArray = jsonObject.optJSONArray("playlists");
                    length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject temp = jsonArray.optJSONObject(i);

                        Playlist playlist = new Playlist(reference, temp.optString("id"), temp.optString("name"), null);
                        playlist.setCover(temp.optString("coverImgUrl"));

                        playlists.add(playlist);
                    }

                    if ((pageNumber * PAGE_ITEM_LIMIT + PAGE_ITEM_LIMIT) < jsonObject.optInt("playlistCount")) {
                        pageNumber += 1;
                        hasNextPage = true;
                    } else {
                        pageNumber = NONE;
                        hasNextPage = false;
                    }
            }

            return playlists;
        }
    }
}
