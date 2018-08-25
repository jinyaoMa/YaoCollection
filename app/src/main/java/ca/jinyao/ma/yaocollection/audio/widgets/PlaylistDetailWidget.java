package ca.jinyao.ma.yaocollection.audio.widgets;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.jinyao.ma.yaocollection.R;
import ca.jinyao.ma.yaocollection.audio.adapters.PlaylistListAdapter;
import ca.jinyao.ma.yaocollection.audio.components.Playlist;
import ca.jinyao.ma.yaocollection.audio.components.Song;
import ca.jinyao.ma.yaocollection.audio.components.SongList;
import ca.jinyao.ma.yaocollection.audio.views.LoadingView;
import ca.jinyao.ma.yaocollection.audio.views.RoundAngleImageView;

/**
 * Class PlaylistDetailWidget
 * create by jinyaoMa 0022 2018/8/22 17:50
 */
public class PlaylistDetailWidget implements View.OnTouchListener, View.OnClickListener, AdapterView.OnItemClickListener, Playlist.AsyncSongListener {
    private final String TAG = "PlaylistDetailWidget";

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

    private PlaylistListAdapter playlistListAdapter;
    private Playlist currentPlaylist;

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

    public PlaylistDetailWidget(Service context) {
        this.mContext = context;

        playlistListAdapter = new PlaylistListAdapter(context);

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
        mFloatLayout = inflater.inflate(R.layout.playlist_detail_widget, null);

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

    public void create(Playlist playlist) {
        ivLoad.start();
        cover.setImagePath(playlist.cover);
        name.setText(playlist.name);
        playlist.getSongs(this);
        currentPlaylist = playlist;

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
                listener.onPlayAll(currentPlaylist);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (listener != null) {
            listener.onPlay(playlistListAdapter.getSong(i));
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onSongsGet(SongList songList) {
        playlistListAdapter.setSongs(songList);
        lvSongs.setAdapter(playlistListAdapter);

        ivLoad.end();
    }

    public interface Listener {
        void onPlayAll(Playlist playlist);
        void onPlay(Song song);
    }
}
