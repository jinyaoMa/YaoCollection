package ca.jinyao.ma.yaocollection.audio.components;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;

import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.REF_163;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.REF_QQ;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.connectionForAlbum;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.getAlbumImageUrlBySample;

/**
 * Class Album
 * create by jinyaoMa 0009 2018/8/9 23:45
 */
public class Album {
    private int reference;

    public String albumId;
    public String albumTitle;
    public String coverPath;

    public ArtistList artists;

    public Album(int reference, String albumId, String albumTitle) {
        this.reference = reference;
        this.coverPath = getAlbumImageUrlBySample(reference, albumId);
        this.albumId = albumId;
        this.albumTitle = albumTitle;
        artists = new ArtistList();
    }

    public int getReference() {
        return reference;
    }

    public void addArtist(String artistId, String artistName, String coverPath) {
        Artist artist = new Artist(reference, artistId, artistName);
        if (coverPath != null) {
            artist.coverPath = coverPath;
        }
        artists.add(artist);
    }

    public void clearArtists() {
        artists.clear();
    }

    /**
     * Get songs of this album
     *
     * @param asyncListener AsyncListener
     */
    public void getSongs(AsyncListener asyncListener) {
        this.asyncListener = asyncListener;
        GetSongsTask getSongsTask = new GetSongsTask();
        getSongsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // Listener
    private AsyncListener asyncListener;

    public interface AsyncListener {
        void onSongsGet(SongList songList);
    }

    // Async Task
    private class GetSongsTask extends AsyncTask<String, SongList, SongList> {
        @Override
        protected SongList doInBackground(String[] string) {
            SongList songList = new SongList();
            Connection connection = connectionForAlbum(reference, albumId);
            JSONObject jsonObject;
            JSONArray jsonArray;
            int length;
            switch (reference) {
                case REF_QQ:
                    try {
                        jsonObject = new JSONObject(connection.execute().body()).optJSONObject("data");

                        albumTitle = jsonObject.optString("name");

                        jsonArray = jsonObject.optJSONArray("list");
                        length = jsonArray.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject temp = jsonArray.optJSONObject(i);
                            JSONArray tmp = temp.optJSONArray("singer");

                            Song song = new Song(reference, temp.optString("songmid"), temp.optString("songname"));
                            song.setAlbum(albumId, albumTitle);
                            for (int j = 0; j < tmp.length(); j++) {
                                JSONObject artist = tmp.optJSONObject(j);
                                song.addArtist(artist.optString("mid"), artist.optString("name"), null);
                            }

                            songList.add(song);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    break;
                case REF_163:
                    try {
                        jsonObject = new JSONObject(connection.execute().body()).optJSONObject("album");

                        albumTitle = jsonObject.optString("name");

                        String backupArtistCoverPath = jsonObject.optJSONObject("artist").optString("picUrl");
                        jsonArray = jsonObject.optJSONArray("songs");
                        length = jsonArray.length();
                        for (int i = 0; i < length; i++) {
                            JSONObject temp = jsonArray.optJSONObject(i);
                            JSONArray tmp = temp.optJSONArray("artists");

                            Song song = new Song(reference, temp.optString("id"), temp.optString("name"));
                            song.setAlbum(albumId, albumTitle);
                            song.setCoverPath(jsonObject.optString("picUrl"));
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
                asyncListener.onSongsGet(songList);
            }
        }
    }
}
