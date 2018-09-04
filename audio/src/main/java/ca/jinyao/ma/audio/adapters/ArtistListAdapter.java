package ca.jinyao.ma.audio.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ca.jinyao.ma.audio.R;
import ca.jinyao.ma.audio.components.Album;
import ca.jinyao.ma.audio.components.AlbumList;
import ca.jinyao.ma.audio.components.Song;
import ca.jinyao.ma.audio.components.SongList;
import ca.jinyao.ma.audio.views.RoundAngleImageView;

/**
 * Class ArtistListAdapter
 * create by jinyaoMa 0023 2018/8/23 17:44
 */
public class ArtistListAdapter extends BaseAdapter {
    public final int TYPE_SONG = 0;
    public final int TYPE_ALBUM = 1;
    private int currentType;

    private Context context;
    private SongList songs;
    private AlbumList albums;

    RoundAngleImageView cover;
    TextView title;

    public ArtistListAdapter(Context context) {
        this.context = context;
        songs = new SongList();
        albums = new AlbumList();
    }

    public void setType(int type) {
        currentType = type;
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

    public Song getSong(int i) {
        return songs.get(i);
    }

    public SongList getSongs() {
        return songs;
    }

    public void setAlbums(AlbumList albums) {
        this.albums = albums;
    }

    public void addAlbums(AlbumList albums) {
        if (albums != null && !albums.isEmpty()) {
            for (int i = 0; i < albums.size(); i++) {
                this.albums.add(albums.get(i));
            }
            notifyDataSetChanged();
        }
    }

    public Album getAlbum(int i) {
        return albums.get(i);
    }

    public AlbumList getAlbums() {
        return albums;
    }

    @Override
    public int getCount() {
        if (currentType == TYPE_SONG) {
            return songs.size();
        } else if (currentType == TYPE_ALBUM) {
            return albums.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        if (currentType == TYPE_SONG) {
            return songs.get(i);
        } else if (currentType == TYPE_ALBUM) {
            return albums.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.artist_list_item, null);
        }

        cover = view.findViewById(R.id.cover);
        title = view.findViewById(R.id.title);

        if (currentType == TYPE_SONG) {
            cover.setImagePath(songs.get(i).getCoverPath());
            title.setText(songs.get(i).songTitle);
        } else if (currentType == TYPE_ALBUM) {
            cover.setImagePath(albums.get(i).coverPath);
            title.setText(albums.get(i).albumTitle);
        }

        return view;
    }
}
