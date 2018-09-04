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
import ca.jinyao.ma.audio.components.Artist;
import ca.jinyao.ma.audio.components.ArtistList;
import ca.jinyao.ma.audio.components.Playlist;
import ca.jinyao.ma.audio.components.PlaylistList;
import ca.jinyao.ma.audio.components.Song;
import ca.jinyao.ma.audio.components.SongList;
import ca.jinyao.ma.audio.views.RoundAngleImageView;

/**
 * Class SearchListAdapter
 * create by jinyaoMa 0022 2018/8/22 16:07
 */
public class SearchListAdapter extends BaseAdapter {
    public static final int TYPE_SONG = 0;
    public static final int TYPE_ALBUM = 1;
    public static final int TYPE_ARTIST = 2;
    public static final int TYPE_SONGLIST = 3;

    private Context context;
    private SongList dataListSong;
    private AlbumList dataListAlbum;
    private ArtistList dataListArtist;
    private PlaylistList dataListSonglist;
    private int currentType;

    public SearchListAdapter(Context context) {
        this.context = context;
        dataListSong = new SongList();
        dataListAlbum = new AlbumList();
        dataListArtist = new ArtistList();
        dataListSonglist = new PlaylistList();
    }

    public void setDataList(SongList dataList) {
        dataListSong = dataList;
    }

    public void setDataList(AlbumList dataList) {
        dataListAlbum = dataList;
    }

    public void setDataList(ArtistList dataList) {
        dataListArtist = dataList;
    }

    public void setDataList(PlaylistList dataList) {
        dataListSonglist = dataList;
    }

    public void addDataList(SongList dataList) {
        if (dataListSong != null) {
            for (Song song : dataList) {
                dataListSong.add(song);
            }
            notifyDataSetChanged();
        }
    }

    public void addDataList(AlbumList dataList) {
        if (dataListAlbum != null) {
            for (Album album : dataList) {
                dataListAlbum.add(album);
            }
            notifyDataSetChanged();
        }
    }

    public void addDataList(ArtistList dataList) {
        if (dataListArtist != null) {
            for (Artist artist : dataList) {
                dataListArtist.add(artist);
            }
            notifyDataSetChanged();
        }
    }

    public void addDataList(PlaylistList dataList) {
        if (dataListSonglist != null) {
            for (Playlist playlist : dataList) {
                dataListSonglist.add(playlist);
            }
            notifyDataSetChanged();
        }
    }

    public void setCurrentType(int currentType) {
        this.currentType = currentType;
    }

    public int getCurrentType() {
        return currentType;
    }

    @Override
    public int getCount() {
        if (currentType == TYPE_SONG) {
            return dataListSong.size();
        } else if (currentType == TYPE_ALBUM) {
            return dataListAlbum.size();
        } else if (currentType == TYPE_ARTIST) {
            return dataListArtist.size();
        } else if (currentType == TYPE_SONGLIST) {
            return dataListSonglist.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        if (currentType == TYPE_SONG) {
            return dataListSong.get(i);
        } else if (currentType == TYPE_ALBUM) {
            return dataListAlbum.get(i);
        } else if (currentType == TYPE_ARTIST) {
            return dataListArtist.get(i);
        } else if (currentType == TYPE_SONGLIST) {
            return dataListSonglist.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    RoundAngleImageView cover;
    TextView name;

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.search_list_item, null);
        }

        cover = view.findViewById(R.id.cover);
        name = view.findViewById(R.id.name);

        if (currentType == TYPE_SONG) {
            cover.setImagePath(dataListSong.get(i).getCoverPath());
            name.setText(dataListSong.get(i).songTitle);
        } else if (currentType == TYPE_ALBUM) {
            cover.setImagePath(dataListAlbum.get(i).coverPath);
            name.setText(dataListAlbum.get(i).albumTitle);
        } else if (currentType == TYPE_ARTIST) {
            cover.setImagePath(dataListArtist.get(i).coverPath);
            name.setText(dataListArtist.get(i).artistName);
        } else if (currentType == TYPE_SONGLIST) {
            cover.setImagePath(dataListSonglist.get(i).cover);
            name.setText(dataListSonglist.get(i).name);
        }

        return view;
    }
}
