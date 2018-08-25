package ca.jinyao.ma.yaocollection.audio.widgets;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.jinyao.ma.yaocollection.R;
import ca.jinyao.ma.yaocollection.audio.adapters.AlbumListAdapter;
import ca.jinyao.ma.yaocollection.audio.components.Album;
import ca.jinyao.ma.yaocollection.audio.components.Artist;
import ca.jinyao.ma.yaocollection.audio.components.Song;
import ca.jinyao.ma.yaocollection.audio.components.SongList;
import ca.jinyao.ma.yaocollection.audio.views.LoadingView;
import ca.jinyao.ma.yaocollection.audio.views.RoundAngleImageView;

/**
 * Class AlbumDetailWidget
 * create by jinyaoMa 0023 2018/8/23 17:43
 */
public class AlbumDetailWidget implements View.OnTouchListener, View.OnClickListener, Album.AsyncListener, AdapterView.OnItemClickListener {
    private final String TAG = "AlbumDetailWidget";

    private Context mContext;
    private int statusBarHeight;
    private WindowManager.LayoutParams mWindowParams;
    private WindowManager mWindowManager;
    private DisplayMetrics metrics;
    private Boolean canCreate;

    private View mFloatLayout;
    private float viewX;
    private float viewY;
    private float screenX;
    private float screenY;

    private AlbumListAdapter albumListAdapter;
    private Album currentAlbum;

    private Listener listener;

    @BindView(R.id.ivPlay)
    ImageView ivPlay;
    @BindView(R.id.ivClose)
    ImageView ivClose;
    @BindView(R.id.ivMove)
    ImageView ivMove;
    @BindView(R.id.ivLoad)
    LoadingView ivLoad;
    @BindView(R.id.cover)
    RoundAngleImageView cover;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.lvSongs)
    ListView lvSongs;
    @BindView(R.id.artistContainer)
    LinearLayout artistContainer;

    public AlbumDetailWidget(Service context) {
        this.mContext = context;

        albumListAdapter = new AlbumListAdapter(context);

        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }

        canCreate = true;
        initFloatWindow();
    }

    private void initFloatWindow() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (inflater == null) {
            return;
        }
        mFloatLayout = inflater.inflate(R.layout.album_detail_widget, null);

        ButterKnife.bind(this, mFloatLayout);

        ivPlay.setOnTouchListener(this);
        ivPlay.setOnClickListener(this);
        ivMove.setOnTouchListener(this);
        ivClose.setOnTouchListener(this);
        ivClose.setOnClickListener(this);
        lvSongs.setOnItemClickListener(this);

        metrics = new DisplayMetrics();

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowParams.width = metrics.widthPixels * 4 / 5;
        mWindowParams.height = metrics.heightPixels / 2;
        mWindowParams.gravity = Gravity.START | Gravity.TOP;
        mWindowParams.x = (metrics.widthPixels - mWindowParams.width) / 2;
        mWindowParams.y = (metrics.heightPixels - statusBarHeight - mWindowParams.height) / 2;
    }

    public void create(Album album) {
        ivLoad.start();
        cover.setImagePath(album.coverPath);
        name.setText(album.albumTitle);
        album.getSongs(this);
        currentAlbum = album;
        buildArtist();

        if (canCreate) {
            mWindowManager.addView(mFloatLayout, mWindowParams);

            canCreate = false;
        }
    }

    public void remove() {
        if (!canCreate) {
            mWindowManager.removeView(mFloatLayout);

            canCreate = true;
        }
    }

    private Boolean buildArtist() {
        artistContainer.removeAllViews();

        for (int i = 0, len = currentAlbum.artists.size(); i < len; i++) {
            Artist artist = currentAlbum.artists.get(i);
            Button btn = new Button(mContext);
            btn.setText(artist.artistName);
            btn.setOnClickListener(this);
            btn.setTag(i);

            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setTextColor(mContext.getColor(R.color.colorAccent));
            btn.setAllCaps(false);

            artistContainer.addView(btn);
        }

        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == R.id.ivMove) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    viewX = motionEvent.getX();
                    viewY = motionEvent.getY();
                    view.setBackgroundResource(R.drawable.round_widget_reverse_rectangle);
                    break;
                case MotionEvent.ACTION_MOVE:
                    screenX = motionEvent.getRawX();
                    screenY = motionEvent.getRawY() - statusBarHeight;
                    mWindowParams.x = (int) (screenX - viewX);
                    mWindowParams.y = (int) (screenY - viewY);
                    mWindowManager.updateViewLayout(mFloatLayout, mWindowParams);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setBackground(null);
            }
            return true;

        } else if (view.getId() == R.id.ivClose) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setBackgroundResource(R.drawable.round_widget_reverse_rectangle);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setBackground(null);
            }
            return false;

        } else if (view.getId() == R.id.ivPlay) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setBackgroundResource(R.drawable.round_widget_reverse_rectangle);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setBackground(null);
            }
            return false;

        } else {
            return false;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ivClose) {
            remove();
        } else if (view.getId() == R.id.ivPlay) {
            if (listener != null && !ivLoad.isLoading()) {
                listener.onPlayAll(albumListAdapter.getSongs());
            }
        } else {
            if (view instanceof Button) {
                int index = (int) view.getTag();
                listener.onArtistClick(currentAlbum.artists.get(index));
            }
        }
    }

    @Override
    public void onSongsGet(SongList songList) {
        albumListAdapter.setSongs(songList);
        lvSongs.setAdapter(albumListAdapter);

        ivLoad.end();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        listener.onPlay(albumListAdapter.getSong(i));
    }

    public interface Listener {
        void onPlayAll(SongList songs);

        void onPlay(Song song);

        void onArtistClick(Artist artist);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
