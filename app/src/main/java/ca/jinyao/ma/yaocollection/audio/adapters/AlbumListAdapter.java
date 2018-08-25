package ca.jinyao.ma.yaocollection.audio.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.jinyao.ma.yaocollection.R;
import ca.jinyao.ma.yaocollection.audio.components.Song;
import ca.jinyao.ma.yaocollection.audio.components.SongList;
import ca.jinyao.ma.yaocollection.audio.views.RoundAngleImageView;

/**
 * Class AlbumListAdapter
 * create by jinyaoMa 0023 2018/8/23 17:44
 */
public class AlbumListAdapter extends BaseAdapter {
    private Context context;
    private SongList songs;

    @BindView(R.id.cover)
    RoundAngleImageView cover;
    @BindView(R.id.title)
    TextView title;

    public AlbumListAdapter(Context context) {
        this.context = context;
        songs = new SongList();
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
            view = LayoutInflater.from(context).inflate(R.layout.album_list_item, null);
        }

        ButterKnife.bind(this, view);

        cover.setImageResource(R.drawable.ic_action_song);
        title.setText(songs.get(i).songTitle);

        return view;
    }
}
