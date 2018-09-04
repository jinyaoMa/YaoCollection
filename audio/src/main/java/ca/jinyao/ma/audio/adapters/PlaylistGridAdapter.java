package ca.jinyao.ma.audio.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ca.jinyao.ma.audio.R;
import ca.jinyao.ma.audio.components.Playlist;
import ca.jinyao.ma.audio.components.PlaylistList;
import ca.jinyao.ma.audio.views.RoundAngleImageView;

/**
 * Class PlaylistGridAdapter
 * create by jinyaoMa 0022 2018/8/22 16:06
 */
public class PlaylistGridAdapter extends BaseAdapter {
    Context context;
    PlaylistList playlists;

    RoundAngleImageView cover;
    TextView name;

    public PlaylistGridAdapter(Context context) {
        this.context = context;
    }

    public void setPlaylists(PlaylistList playlists) {
        this.playlists = playlists;
    }

    public void addPlaylists(PlaylistList playlists) {
        if (this.playlists != null) {
            for (Playlist playlist : playlists) {
                this.playlists.add(playlist);
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    public Playlist getItem(int i) {
        return playlists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.playlist_grid_item, null);
        }

        cover = view.findViewById(R.id.cover);
        name = view.findViewById(R.id.name);

        cover.setImagePath(playlists.get(i).cover);
        name.setText(playlists.get(i).name);

        return view;
    }
}
