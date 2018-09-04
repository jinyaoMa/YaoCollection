package ca.jinyao.ma.audio.widgets;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.design.widget.TabLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ca.jinyao.ma.audio.R;
import ca.jinyao.ma.audio.adapters.ArtistListAdapter;
import ca.jinyao.ma.audio.components.Album;
import ca.jinyao.ma.audio.components.AlbumList;
import ca.jinyao.ma.audio.components.Artist;
import ca.jinyao.ma.audio.components.Song;
import ca.jinyao.ma.audio.components.SongList;
import ca.jinyao.ma.audio.views.LoadingView;
import ca.jinyao.ma.audio.views.RoundAngleImageView;

/**
 * Class ArtistDetailWidget
 * create by jinyaoMa 0022 2018/8/22 21:41
 */
public class ArtistDetailWidget implements View.OnTouchListener, View.OnClickListener, AdapterView.OnItemClickListener, TabLayout.OnTabSelectedListener, Artist.AsyncSongListener, Artist.AsyncAlbumListener, AbsListView.OnScrollListener {
    private final String TAG = "ArtistDetailWidget";

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

    private ArtistListAdapter artistListAdapter;
    private Boolean isFirstLoad;
    private Boolean hasNextPage;
    private int nextPage;
    private Boolean isLoadingMove;
    private Artist currentArtist;

    private Boolean isTabBuilt;
    private final int TAB_SONG = 0;
    private final int TAB_ALBUM = 1;

    private Listener listener;

    ImageView ivPlay;
    ImageView ivClose;
    ImageView ivMove;
    LoadingView ivLoad;
    RoundAngleImageView cover;
    TextView name;
    ListView lvResult;
    TabLayout tlTab;
    ProgressBar progressBar;

    public ArtistDetailWidget(Service context) {
        this.mContext = context;

        artistListAdapter = new ArtistListAdapter(context);
        isFirstLoad = true;
        hasNextPage = false;
        isLoadingMove = false;
        isTabBuilt = false;

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
        mFloatLayout = inflater.inflate(R.layout.artist_detail_widget, null);

        ivPlay = mFloatLayout.findViewById(R.id.ivPlay);
        ivClose = mFloatLayout.findViewById(R.id.ivClose);
        ivMove = mFloatLayout.findViewById(R.id.ivMove);
        ivLoad = mFloatLayout.findViewById(R.id.ivLoad);
        cover = mFloatLayout.findViewById(R.id.cover);
        name = mFloatLayout.findViewById(R.id.name);
        lvResult = mFloatLayout.findViewById(R.id.lvResult);
        tlTab = mFloatLayout.findViewById(R.id.tlTab);
        progressBar = mFloatLayout.findViewById(R.id.progressBar);

        ivPlay.setOnTouchListener(this);
        ivPlay.setOnClickListener(this);
        ivMove.setOnTouchListener(this);
        ivClose.setOnTouchListener(this);
        ivClose.setOnClickListener(this);
        lvResult.setOnItemClickListener(this);
        lvResult.setOnScrollListener(this);
        tlTab.addOnTabSelectedListener(this);

        progressBar.setVisibility(View.GONE);

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

    public void create(Artist artist) {
        ivLoad.start();
        cover.setImagePath(artist.coverPath);
        name.setText(artist.artistName);
        currentArtist = artist;

        if (canCreate) {
            if (!isTabBuilt) {
                isTabBuilt = buildTab();
            } else {
                onTabSelected(tlTab.getTabAt(tlTab.getSelectedTabPosition()));
            }

            mWindowManager.addView(mFloatLayout, mWindowParams);

            canCreate = false;
        } else {
            onTabSelected(tlTab.getTabAt(tlTab.getSelectedTabPosition()));
        }
    }

    public void remove() {
        if (!canCreate) {
            mWindowManager.removeView(mFloatLayout);

            canCreate = true;
        }
    }

    private Boolean buildTab() {
        TabLayout.Tab song = tlTab.newTab();
        song.setTag(TAB_SONG);
        song.setIcon(R.drawable.ic_action_hot);

        TabLayout.Tab album = tlTab.newTab();
        album.setTag(TAB_ALBUM);
        album.setIcon(R.drawable.ic_action_album);

        tlTab.addTab(song, true);
        tlTab.addTab(album);

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
                listener.onPlayAll(artistListAdapter.getSongs());
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        int tab = (int) tlTab.getTabAt(tlTab.getSelectedTabPosition()).getTag();
        if (tab == TAB_SONG) {
            listener.onPlay(artistListAdapter.getSong(i));
        } else if (tab == TAB_ALBUM) {
            listener.onAlbumClick(artistListAdapter.getAlbum(i));
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int i = (int) tab.getTag();
        if (i == TAB_SONG) {
            ivPlay.setVisibility(View.VISIBLE);
            isFirstLoad = true;
            ivLoad.start();
            currentArtist.getSongs(this);
        } else if (i == TAB_ALBUM) {
            ivPlay.setVisibility(View.GONE);
            isFirstLoad = true;
            ivLoad.start();
            currentArtist.getAlbums(this);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        return;
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        return;
    }

    @Override
    public void onSongsGet(SongList songList, int nextPage, Boolean hasNextPage) {
        this.nextPage = nextPage;
        this.hasNextPage = hasNextPage;

        artistListAdapter.setType(artistListAdapter.TYPE_SONG);
        if (isFirstLoad) {
            artistListAdapter.setSongs(songList);
            lvResult.setAdapter(artistListAdapter);
            isFirstLoad = false;
        } else {
            artistListAdapter.addSongs(songList);
        }

        ivLoad.end();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onAlbumsGet(AlbumList albumList, int nextPage, Boolean hasNextPage) {
        this.nextPage = nextPage;
        this.hasNextPage = hasNextPage;

        artistListAdapter.setType(artistListAdapter.TYPE_ALBUM);
        if (isFirstLoad) {
            artistListAdapter.setAlbums(albumList);
            lvResult.setAdapter(artistListAdapter);
            isFirstLoad = false;
        } else {
            artistListAdapter.addAlbums(albumList);
        }

        ivLoad.end();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        return;
    }

    @Override
    public void onScroll(AbsListView absListView, int row, int visibile, int total) {
        if (hasNextPage && (lvResult.getLastVisiblePosition() + 1) == total && !isLoadingMove) {
            isLoadingMove = true;
            progressBar.setVisibility(View.VISIBLE);
            int tab = (int) tlTab.getTabAt(tlTab.getSelectedTabPosition()).getTag();
            if (tab == TAB_SONG) {
                currentArtist.getSongs(this, nextPage);
            } else if (tab == TAB_ALBUM) {
                currentArtist.getAlbums(this, nextPage);
            }
        }
    }

    public interface Listener {
        void onPlayAll(SongList songs);

        void onPlay(Song song);

        void onAlbumClick(Album album);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
