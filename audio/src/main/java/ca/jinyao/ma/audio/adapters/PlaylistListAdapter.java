package ca.jinyao.ma.audio.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ca.jinyao.ma.audio.R;
import ca.jinyao.ma.audio.components.Song;
import ca.jinyao.ma.audio.components.SongList;
import ca.jinyao.ma.audio.views.RoundAngleImageView;

/**
 * Class PlaylistListAdapter
 * create by jinyaoMa 0022 2018/8/22 17:19
 */
public class PlaylistListAdapter extends BaseAdapter {
    private Context context;
    private SongList songs;

    private int highlightIndex;

    RoundAngleImageView cover;
    TextView title;
    TextView artist;

    public PlaylistListAdapter(Context context) {
        this.context = context;
        songs = new SongList();
        highlightIndex = -1;
    }

    public void setSongs(SongList songs) {
        this.songs = songs;
    }

    public void addSongs(SongList songs) {
        if (songs != null && !songs.isEmpty()) {
            for (int i = 0; i < songs.size(); i++) {
                this.songs.add(songs.get(i));
            }
            notifyDataSetChanged();
        }
    }

    public void addSong(Song song) {
        if (songs.indexOf(song) >= 0) {
            return;
        }
        songs.add(song);
        notifyDataSetChanged();
    }

    public Song getSong(int i) {
        return songs.get(i);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return songs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.playlist_list_item, null);
        }

        cover = view.findViewById(R.id.cover);
        title = view.findViewById(R.id.title);
        artist = view.findViewById(R.id.artist);

        cover.setImagePath(songs.get(i).getCoverPath());
        title.setText(songs.get(i).songTitle);
        artist.setText(songs.get(i).getArtists().getNameString());

        if (highlightIndex == i) {
            view.setBackgroundColor(context.getColor(R.color.colorAccentTransparent));
            artist.setTextColor(context.getColor(R.color.colorWhiteTransparent));
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
            artist.setTextColor(Color.DKGRAY);
        }

        return view;
    }

    public void setHighlight(int index) {
        highlightIndex = index;
        notifyDataSetChanged();
    }

    public int getCurrentReference() {
        if (highlightIndex >= 0) {
            return songs.get(highlightIndex).getReference();
        }
        return -1;
    }

    public String getCurrentSongId() {
        if (highlightIndex >= 0) {
            return songs.get(highlightIndex).songId;
        }
        return "";
    }

    public int getHighlightIndex() {
        return highlightIndex;
    }
}
