package ca.jinyao.ma.yaocollection.audio.widgets;

import android.app.Service;
import android.content.Context;
import android.database.Cursor;
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
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.jinyao.ma.yaocollection.R;
import ca.jinyao.ma.yaocollection.audio.adapters.SearchListAdapter;
import ca.jinyao.ma.yaocollection.audio.components.Album;
import ca.jinyao.ma.yaocollection.audio.components.AlbumList;
import ca.jinyao.ma.yaocollection.audio.components.Artist;
import ca.jinyao.ma.yaocollection.audio.components.ArtistList;
import ca.jinyao.ma.yaocollection.audio.components.Playlist;
import ca.jinyao.ma.yaocollection.audio.components.PlaylistList;
import ca.jinyao.ma.yaocollection.audio.components.Song;
import ca.jinyao.ma.yaocollection.audio.components.SongList;
import ca.jinyao.ma.yaocollection.audio.cores.SearchBrowser;
import ca.jinyao.ma.yaocollection.audio.databases.SearchHistoryDatabaseHelper;
import ca.jinyao.ma.yaocollection.audio.views.LoadingView;

import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.REF_163;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.REF_QQ;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.TAB_ALBUM;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.TAB_ARTIST;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.TAB_PLAYLIST;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.TAB_SONG;

/**
 * Class SearchBrowserWidget
 * create by jinyaoMa 0022 2018/8/22 16:06
 */
public class SearchBrowserWidget implements SearchBrowser.AsyncListener, TabLayout.OnTabSelectedListener, View.OnClickListener, View.OnTouchListener, AbsListView.OnScrollListener, SearchView.OnQueryTextListener, View.OnFocusChangeListener {
    private final String TAG = "SearchBrowserWidget";

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

    private SearchBrowser searchBrowser;

    private int currentRef = 0;
    private int currentTab = 0;

    private final int TAB_REF = 0;
    private final int TAB_CAT = 1;

    private final int TAB_REF_QQ_POS = 0;
    private final int TAB_REF_163_POS = 1;

    private final int TAB_CAT_SONG = 0;
    private final int TAB_CAT_ALBUM = 1;
    private final int TAB_CAT_ARTIST = 2;
    private final int TAB_CAT_SONGLIST = 3;

    private SimpleCursorAdapter simpleCursorAdapter;
    private SearchListAdapter searchListAdapter;
    private Boolean isFirstLoad;
    private Boolean hasNextPage;
    private int nextPage;
    private Boolean isLoadingMove;
    private String currentKeyword;

    private SearchHistoryDatabaseHelper searchHistoryDatabaseHelper;

    private int yChange = 0;
    private Listener listener;

    @BindView(R.id.ivBin)
    ImageView ivBin;
    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.ivSwitch)
    ImageView ivSwitch;
    @BindView(R.id.ivClose)
    ImageView ivClose;
    @BindView(R.id.ivMove)
    ImageView ivMove;
    @BindView(R.id.ivLoad)
    LoadingView ivLoad;
    @BindView(R.id.tlReference)
    TabLayout tlReference;
    @BindView(R.id.tlCategory)
    TabLayout tlCategory;
    @BindView(R.id.lvResult)
    ListView lvResult;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.lvHistory)
    ListView lvHistory;

    public SearchBrowserWidget(Service context) {
        this.mContext = context;

        searchHistoryDatabaseHelper = new SearchHistoryDatabaseHelper(context);
        searchListAdapter = new SearchListAdapter(context);
        searchBrowser = new SearchBrowser(this);
        isFirstLoad = true;
        hasNextPage = false;
        isLoadingMove = false;
        currentKeyword = "";

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
        mFloatLayout = inflater.inflate(R.layout.search_browser_widget, null);

        ButterKnife.bind(this, mFloatLayout);

        ivBin.setOnTouchListener(this);
        ivBin.setOnClickListener(this);
        searchView.setOnQueryTextListener(this);
        searchView.setOnQueryTextFocusChangeListener(this);
        ivSwitch.setOnTouchListener(this);
        ivSwitch.setOnClickListener(this);
        ivMove.setOnTouchListener(this);
        ivClose.setOnTouchListener(this);
        ivClose.setOnClickListener(this);
        tlReference.addOnTabSelectedListener(this);
        tlCategory.addOnTabSelectedListener(this);
        lvResult.setOnScrollListener(this);

        progressBar.setVisibility(View.GONE);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconifiedByDefault(false);

        initOnItemClickListeners();
        buildReference();
        buildCategory();

        metrics = new DisplayMetrics();

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.dimAmount = 0.5f;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        mWindowParams.width = metrics.widthPixels * 4 / 5;
        mWindowParams.height = metrics.heightPixels * 2 / 3;
        mWindowParams.gravity = Gravity.START | Gravity.TOP;
        mWindowParams.x = (metrics.widthPixels - mWindowParams.width) / 2;
        mWindowParams.y = (metrics.heightPixels - statusBarHeight - mWindowParams.height) / 2;
    }

    private void buildReference() {
        TabLayout.Tab qq = tlReference.newTab();
        qq.setIcon(R.drawable.ic_action_qq);

        TabLayout.Tab _163 = tlReference.newTab();
        _163.setIcon(R.drawable.ic_action_163);

        qq.setTag(TAB_REF);
        _163.setTag(TAB_REF);

        tlReference.addTab(qq, TAB_REF_QQ_POS, true);
        tlReference.addTab(_163, TAB_REF_163_POS);
    }

    private void buildCategory() {
        TabLayout.Tab song = tlCategory.newTab();
        song.setIcon(R.drawable.ic_action_song);

        TabLayout.Tab album = tlCategory.newTab();
        album.setIcon(R.drawable.ic_action_album);

        TabLayout.Tab artist = tlCategory.newTab();
        artist.setIcon(R.drawable.ic_action_artist);

        TabLayout.Tab songlist = tlCategory.newTab();
        songlist.setIcon(R.drawable.ic_action_songlist);

        song.setTag(TAB_CAT);
        album.setTag(TAB_CAT);
        artist.setTag(TAB_CAT);
        songlist.setTag(TAB_CAT);

        tlCategory.addTab(song, TAB_CAT_SONG, true);
        tlCategory.addTab(album, TAB_CAT_ALBUM);
        tlCategory.addTab(artist, TAB_CAT_ARTIST);
        tlCategory.addTab(songlist, TAB_CAT_SONGLIST);
    }

    public void create() {
        if (canCreate) {
            mWindowManager.addView(mFloatLayout, mWindowParams);

            canCreate = false;
        }
    }

    public void create(int x, int y) {
        if (canCreate) {
            mWindowParams.x = x;
            mWindowParams.y = y;
            mWindowManager.addView(mFloatLayout, mWindowParams);

            canCreate = false;
        }
    }

    public int getPositionX() {
        return mWindowParams.x;
    }

    public int getPositionY() {
        return mWindowParams.y;
    }

    public void remove() {
        if (!canCreate) {
            mWindowManager.removeView(mFloatLayout);

            canCreate = true;
        }
    }

    private void initOnItemClickListeners() {
        lvResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object object = searchListAdapter.getItem(i);
                if (object instanceof Song) {
                    listener.onSongClick((Song) object);
                } else if (object instanceof Album) {
                    listener.onAlbumClick((Album) object);
                } else if (object instanceof Artist) {
                    listener.onArtistClick((Artist) object);
                } else if (object instanceof Playlist) {
                    listener.onPlaylistClick((Playlist) object);
                }
            }
        });
        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = simpleCursorAdapter.getCursor();
                searchView.setQuery(cursor.getString(cursor.getColumnIndex(searchHistoryDatabaseHelper.KEYWORD)), true);
            }
        });
    }

    @Override
    public void onSearchCompleted(SongList list, String keyword, int nextPage, Boolean hasNextPage) {
        this.nextPage = nextPage;
        this.hasNextPage = hasNextPage;
        this.currentKeyword = keyword;

        searchListAdapter.setCurrentType(SearchListAdapter.TYPE_SONG);
        if(isFirstLoad) {
            searchListAdapter.setDataList(list);
            lvResult.setAdapter(searchListAdapter);
            isFirstLoad = false;
        } else {
            searchListAdapter.addDataList(list);
        }

        ivLoad.end();
        progressBar.setVisibility(View.GONE);
        isLoadingMove = false;
    }

    @Override
    public void onSearchCompleted(ArtistList list, String keyword, int nextPage, Boolean hasNextPage) {
        this.nextPage = nextPage;
        this.hasNextPage = hasNextPage;
        this.currentKeyword = keyword;

        searchListAdapter.setCurrentType(SearchListAdapter.TYPE_ARTIST);
        if(isFirstLoad) {
            searchListAdapter.setDataList(list);
            lvResult.setAdapter(searchListAdapter);
            isFirstLoad = false;
        } else {
            searchListAdapter.addDataList(list);
        }

        ivLoad.end();
        progressBar.setVisibility(View.GONE);
        isLoadingMove = false;
    }

    @Override
    public void onSearchCompleted(AlbumList list, String keyword, int nextPage, Boolean hasNextPage) {
        this.nextPage = nextPage;
        this.hasNextPage = hasNextPage;
        this.currentKeyword = keyword;

        searchListAdapter.setCurrentType(SearchListAdapter.TYPE_ALBUM);
        if(isFirstLoad) {
            searchListAdapter.setDataList(list);
            lvResult.setAdapter(searchListAdapter);
            isFirstLoad = false;
        } else {
            searchListAdapter.addDataList(list);
        }

        ivLoad.end();
        progressBar.setVisibility(View.GONE);
        isLoadingMove = false;
    }

    @Override
    public void onSearchCompleted(PlaylistList list, String keyword, int nextPage, Boolean hasNextPage) {
        this.nextPage = nextPage;
        this.hasNextPage = hasNextPage;
        this.currentKeyword = keyword;

        searchListAdapter.setCurrentType(SearchListAdapter.TYPE_SONGLIST);
        if(isFirstLoad) {
            searchListAdapter.setDataList(list);
            lvResult.setAdapter(searchListAdapter);
            isFirstLoad = false;
        } else {
            searchListAdapter.addDataList(list);
        }

        ivLoad.end();
        progressBar.setVisibility(View.GONE);
        isLoadingMove = false;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int tag = (int) tab.getTag();
        int pos = tab.getPosition();
        switch (tag) {
            case TAB_REF:
                switch (pos) {
                    case TAB_REF_QQ_POS:
                        currentRef = REF_QQ;
                        break;
                    case TAB_REF_163_POS:
                        currentRef = REF_163;
                }
                break;
            case TAB_CAT:
                switch (pos) {
                    case TAB_CAT_SONG:
                        currentTab = TAB_SONG;
                        break;
                    case TAB_CAT_ALBUM:
                        currentTab = TAB_ALBUM;
                        break;
                    case TAB_CAT_ARTIST:
                        currentTab = TAB_ARTIST;
                        break;
                    case TAB_CAT_SONGLIST:
                        currentTab = TAB_PLAYLIST;
                }
        }

        if (currentKeyword.isEmpty()) {
            return;
        }
        ivLoad.start();
        isFirstLoad = true;
        searchBrowser.search(currentRef, currentTab, currentKeyword);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivClose:
                listener.onCloseWindow(mWindowParams.x, mWindowParams.y);
                remove();
                break;
            case R.id.ivBin:
                searchHistoryDatabaseHelper.clearRecords();
                ivBin.setVisibility(View.GONE);
                lvHistory.setVisibility(View.GONE);
                break;
            case R.id.ivSwitch:
                listener.onSwitchClick();
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

        } else if (view.getId() == R.id.ivSwitch) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setBackgroundResource(R.drawable.round_widget_reverse_rectangle);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setBackground(null);
            }
            return false;

        } else if (view.getId() == R.id.ivBin) {
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
    public void onScrollStateChanged(AbsListView absListView, int i) {
        return;
    }

    @Override
    public void onScroll(AbsListView absListView, int row, int visibile, int total) {
        if (hasNextPage && (lvResult.getLastVisiblePosition() + 1) == total && !isLoadingMove) {
            isLoadingMove = true;
            progressBar.setVisibility(View.VISIBLE);
            searchBrowser.search(currentRef, currentTab, currentKeyword, nextPage);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        isFirstLoad = true;
        searchView.clearFocus();
        searchBrowser.search(tlReference.getTabMode(), currentTab, s);
        searchHistoryDatabaseHelper.insertRecord(s);
        lvHistory.setVisibility(View.GONE);
        ivBin.setVisibility(View.GONE);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        Cursor cursor = searchHistoryDatabaseHelper.getRecordsCursor(s);
        simpleCursorAdapter = new SimpleCursorAdapter(mContext, R.layout.search_list_item_history,
                cursor, new String[]{searchHistoryDatabaseHelper.KEYWORD}, new int[]{R.id.record});
        lvHistory.setAdapter(simpleCursorAdapter);
        return false;
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        Cursor cursor = searchHistoryDatabaseHelper.getRecordsCursor(searchView.getQuery().toString());
        if (!b || cursor.getCount() == 0) {
            lvHistory.setVisibility(View.GONE);
            ivBin.setVisibility(View.GONE);

            mWindowParams.y = yChange + statusBarHeight;
            mWindowParams.height = metrics.heightPixels * 2 / 3;
            mWindowManager.updateViewLayout(mFloatLayout, mWindowParams);
        } else {
            simpleCursorAdapter = new SimpleCursorAdapter(mContext, R.layout.search_list_item_history,
                    cursor, new String[]{searchHistoryDatabaseHelper.KEYWORD}, new int[]{R.id.record});
            lvHistory.setAdapter(simpleCursorAdapter);

            lvHistory.setVisibility(View.VISIBLE);
            ivBin.setVisibility(View.VISIBLE);

            yChange = mWindowParams.y - statusBarHeight;
            mWindowParams.y = statusBarHeight;
            mWindowParams.height = metrics.heightPixels / 2;
            mWindowManager.updateViewLayout(mFloatLayout, mWindowParams);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onSongClick(Song song);
        void onAlbumClick(Album album);
        void onArtistClick(Artist artist);
        void onPlaylistClick(Playlist playlist);
        void onSwitchClick();
        void onCloseWindow(int x, int y);
    }
}
