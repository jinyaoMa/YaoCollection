package ca.jinyao.ma.yaocollection.audio.components;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;

import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.NONE;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.PAGE_ITEM_LIMIT;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.REF_163;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.REF_QQ;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.albumConnectionForArtist;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.getArtistImageUrlBySample;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.songConnectionForArtist;

/**
 * Class Artist
 * create by jinyaoMa 0009 2018/8/9 23:44
 */
public class Artist {
    private int reference;

    public String artistId;
    public String artistName;
    public String coverPath;

    public Artist(int reference, String artistId, String artistName) {
        this.reference = reference;
        this.coverPath = getArtistImageUrlBySample(reference, artistId);
        this.artistId = artistId;
        this.artistName = artistName;
    }

    public int getReference() {
        return reference;
    }

    /**
     * Get albums of this artist
     *
     * @param asyncAlbumListener AsyncAlbumListener
     * @param page               page number
     */
    public void getAlbums(AsyncAlbumListener asyncAlbumListener, int page) {
        this.asyncAlbumListener = asyncAlbumListener;
        GetAlbumsTask getAlbumsTask = new GetAlbumsTask();
        getAlbumsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, page);
    }

    public void getAlbums(AsyncAlbumListener asyncAlbumListener) {
        this.asyncAlbumListener = asyncAlbumListener;
        GetAlbumsTask getAlbumsTask = new GetAlbumsTask();
        getAlbumsTask.execute();
    }

    /**
     * Get songs of this artist
     *
     * @param asyncSongListener AsyncSongListener
     * @param page              page number
     */
    public void getSongs(AsyncSongListener asyncSongListener, int page) {
        this.asyncSongListener = asyncSongListener;
        GetSongsTask getSongsTask = new GetSongsTask();
        getSongsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, page);
    }

    public void getSongs(AsyncSongListener asyncSongListener) {
        this.asyncSongListener = asyncSongListener;
        GetSongsTask getSongsTask = new GetSongsTask();
        getSongsTask.execute();
    }

    // Listener
    private AsyncAlbumListener asyncAlbumListener;
    private AsyncSongListener asyncSongListener;

    public interface AsyncAlbumListener {
        void onAlbumsGet(AlbumList albumList, int nextPage, Boolean hasNextPage);
    }

    public interface AsyncSongListener {
        void onSongsGet(SongList songList, int nextPage, Boolean hasNextPage);
    }

    // Async Task
    private class GetAlbumsTask extends AsyncTask<Integer, AlbumList, AlbumList> {
        private int pageNumber;
        private Boolean hasNextPage;

        @Override
        protected AlbumList doInBackground(Integer[] num) {
            if (num.length == 0) {
                pageNumber = 0;
            } else {
                pageNumber = num[0];
            }
            AlbumList albumList = new AlbumList();
            Connection connection = albumConnectionForArtist(reference, artistId, pageNumber);
            JSONObject jsonObject;
            JSONArray jsonArray;
            int length;
            switch (reference) {
                case REF_QQ:
                    try {
                        jsonObject = new JSONObject(connection.execute().body()).optJSONObject("data");

                        artistName = jsonObject.optString("singer_name");

                        jsonArray = jsonObject.optJSONArray("list");
                        length = jsonArray.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject temp = jsonArray.optJSONObject(i);
                            JSONArray tmp = temp.optJSONArray("singers");

                            Album album = new Album(reference, temp.optString("albumMID"), temp.optString("albumName"));
                            for (int j = 0; j < tmp.length(); j++) {
                                JSONObject artist = tmp.optJSONObject(j);
                                album.addArtist(artist.optString("singer_mid"), artist.optString("singer_name"), null);
                            }

                            albumList.add(album);
                        }

                        if ((pageNumber * PAGE_ITEM_LIMIT + PAGE_ITEM_LIMIT) < jsonObject.optInt("total")) {
                            pageNumber += 1;
                            hasNextPage = true;
                        } else {
                            pageNumber = NONE;
                            hasNextPage = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    break;
                case REF_163:
                    try {
                        jsonObject = new JSONObject(connection.execute().body());

                        artistName = jsonObject.optJSONObject("artist").optString("name");

                        jsonArray = jsonObject.optJSONArray("hotAlbums");
                        length = jsonArray.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject temp = jsonArray.optJSONObject(i);
                            JSONArray tmp = temp.optJSONArray("artists");

                            String backupArtistCoverPath = temp.optJSONObject("artist").optString("picUrl");
                            Album album = new Album(reference, temp.optString("id"), temp.optString("name"));
                            album.coverPath = temp.optString("picUrl");
                            for (int j = 0; j < tmp.length(); j++) {
                                JSONObject artist = tmp.optJSONObject(j);
                                if (artist.optString("picUrl").isEmpty()) {
                                    album.addArtist(artist.optString("id"), artist.optString("name"), backupArtistCoverPath);
                                } else {
                                    album.addArtist(artist.optString("id"), artist.optString("name"), artist.optString("picUrl"));
                                }
                            }

                            albumList.add(album);
                        }

                        if (jsonObject.optBoolean("more")) {
                            pageNumber += 1;
                            hasNextPage = true;
                        } else {
                            pageNumber = NONE;
                            hasNextPage = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
            }
            return albumList;
        }

        @Override
        protected void onPostExecute(AlbumList albumList) {
            if (albumList != null) {
                asyncAlbumListener.onAlbumsGet(albumList, pageNumber, hasNextPage);
            }
        }
    }

    private class GetSongsTask extends AsyncTask<Integer, SongList, SongList> {
        private int pageNumber;
        private Boolean hasNextPage;

        @Override
        protected SongList doInBackground(Integer[] num) {
            if (num.length == 0) {
                pageNumber = 0;
            } else {
                pageNumber = num[0];
            }
            SongList songList = new SongList();
            Connection connection = songConnectionForArtist(reference, artistId, pageNumber);
            JSONObject jsonObject;
            JSONArray jsonArray;
            int length;
            switch (reference) {
                case REF_QQ:
                    try {
                        jsonObject = new JSONObject(connection.execute().body()).optJSONObject("data");

                        artistName = jsonObject.optString("singer_name");

                        jsonArray = jsonObject.optJSONArray("list");
                        length = jsonArray.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject temp = jsonArray.optJSONObject(i).optJSONObject("musicData");
                            JSONArray tmp = temp.optJSONArray("singer");

                            Song song = new Song(reference, temp.optString("songmid"), temp.optString("songname"));
                            song.setAlbum(temp.optString("albummid"), temp.optString("albumname"));
                            for (int j = 0; j < tmp.length(); j++) {
                                JSONObject artist = tmp.optJSONObject(j);
                                song.addArtist(artist.optString("mid"), artist.optString("name"), null);
                            }

                            songList.add(song);
                        }

                        if ((pageNumber * PAGE_ITEM_LIMIT + PAGE_ITEM_LIMIT) < jsonObject.optInt("total")) {
                            pageNumber += 1;
                            hasNextPage = true;
                        } else {
                            pageNumber = NONE;
                            hasNextPage = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    break;
                case REF_163:
                    try {
                        jsonObject = new JSONObject(connection.execute().body());

                        artistName = jsonObject.optJSONObject("artist").optString("name");

                        String backupArtistCoverPath = jsonObject.optJSONObject("artist").optString("picUrl");
                        jsonArray = jsonObject.optJSONArray("hotSongs");
                        length = jsonArray.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject temp = jsonArray.optJSONObject(i);
                            JSONArray tmp = temp.optJSONArray("artists");

                            Song song = new Song(reference, temp.optString("id"), temp.optString("name"));
                            song.setAlbum(temp.optJSONObject("album").optString("id"), temp.optJSONObject("album").optString("name"));
                            song.setCoverPath(temp.optJSONObject("album").optString("picUrl"));
                            for (int j = 0; j < tmp.length(); j++) {
                                JSONObject artist = tmp.optJSONObject(j);
                                if (artist.optString("picUrl").isEmpty()) {
                                    song.addArtist(artist.optString("id"), artist.optString("name"), backupArtistCoverPath);
                                } else {
                                    song.addArtist(artist.optString("id"), artist.optString("name"), artist.optString("picUrl"));
                                }
                            }

                            songList.add(song);
                        }

                        pageNumber = NONE;
                        hasNextPage = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
            }
            return songList;
        }

        @Override
        protected void onPostExecute(SongList songList) {
            if (songList != null) {
                asyncSongListener.onSongsGet(songList, pageNumber, hasNextPage);
            }
        }
    }
}
