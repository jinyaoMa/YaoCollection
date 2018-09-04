package ca.jinyao.ma.audio.components;

import android.os.AsyncTask;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;

import static ca.jinyao.ma.audio.cores.AudioConfig.REF_163;
import static ca.jinyao.ma.audio.cores.AudioConfig.REF_QQ;
import static ca.jinyao.ma.audio.cores.AudioConfig.connectionForPlaylist;

/**
 * Class Playlist
 * create by jinyaoMa 0009 2018/8/9 23:07
 */
public class Playlist {
    private int reference;

    public String id;
    public String name;
    public String cover;
    public SongList songList;

    public Playlist(int reference, String id, String name, SongList songList) {
        this.reference = reference;
        this.id = id;
        this.name = name;
        this.songList = songList;
        if (songList != null && !songList.isEmpty()) {
            cover = songList.get((int) Math.floor(Math.random() * songList.size())).getCoverPath();
        }
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getReference() {
        return reference;
    }

    /**
     * Get songs of this playlist
     *
     * @param asyncSongListener AsyncSongListener
     */
    public void getSongs(AsyncSongListener asyncSongListener) {
        if (songList == null || songList.isEmpty()) {
            this.asyncSongListener = asyncSongListener;
            GetSongsTask getSongsTask = new GetSongsTask();
            getSongsTask.execute();
        } else {
            asyncSongListener.onSongsGet(songList);
        }
    }

    // Listener
    private AsyncSongListener asyncSongListener;

    public interface AsyncSongListener {
        void onSongsGet(SongList songList);
    }

    // Async Task
    private class GetSongsTask extends AsyncTask<Integer, SongList, SongList> {
        @Override
        protected SongList doInBackground(Integer... integers) {
            songList = new SongList();
            Connection connection = connectionForPlaylist(reference, id);
            JSONObject jsonObject;
            JSONArray jsonArray;
            int length;
            switch (reference) {
                case REF_QQ:
                    try {
                        jsonObject = new JSONObject(connection.execute().body()).optJSONArray("cdlist").optJSONObject(0);

                        name = StringEscapeUtils.unescapeHtml4(jsonObject.optString("dissname"));
                        cover = jsonObject.optString("logo");

                        jsonArray = jsonObject.optJSONArray("songlist");
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

                            songList.add(song);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    break;
                case REF_163:
                    try {
                        jsonObject = new JSONObject(connection.execute().body()).optJSONObject("result");

                        name = jsonObject.optString("name");
                        cover = jsonObject.optString("coverImgUrl");

                        jsonArray = jsonObject.optJSONArray("tracks");
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
                asyncSongListener.onSongsGet(songList);
            }
        }
    }
}
