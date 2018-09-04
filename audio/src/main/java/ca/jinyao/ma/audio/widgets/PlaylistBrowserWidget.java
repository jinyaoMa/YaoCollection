package ca.jinyao.ma.audio.widgets;

import android.app.Service;
import android.content.Context;
import android.content.pm.ActivityInfo;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;

import ca.jinyao.ma.audio.R;
import ca.jinyao.ma.audio.adapters.PlaylistGridAdapter;
import ca.jinyao.ma.audio.components.Category;
import ca.jinyao.ma.audio.components.CategoryList;
import ca.jinyao.ma.audio.components.Playlist;
import ca.jinyao.ma.audio.components.PlaylistList;
import ca.jinyao.ma.audio.components.Tag;
import ca.jinyao.ma.audio.cores.PlaylistBrowser;
import ca.jinyao.ma.audio.views.LoadingView;

import static ca.jinyao.ma.audio.cores.AudioConfig.REF_163;
import static ca.jinyao.ma.audio.cores.AudioConfig.REF_QQ;

/**
 * Class PlaylistBrowserWidget
 * create by jinyaoMa 0022 2018/8/22 16:05
 */
public class PlaylistBrowserWidget implements View.OnTouchListener, View.OnClickListener, PlaylistBrowser.PlaylistBrowseListener, TabLayout.OnTabSelectedListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private final String TAG = "PlaylistBrowserWidget";

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

    private PlaylistBrowser playlistBrowser;

    private final int TAB_REF = 0;
    private final int TAB_CAT = 1;
    private final int TAB_TAG = 2;

    private final int TAB_REF_QQ_POS = 0;
    private final int TAB_REF_163_POS = 1;

    private final int DEFAULT = 0;

    private int currentRef = 0;
    private CategoryList currentCategeryList;
    private ArrayList<Tag> currentTags;
    private String currentTagId;

    private PlaylistGridAdapter playlistGridAdapter;
    private Boolean isFirstLoad;
    private Boolean hasNextPage;
    private int nextPage;
    private Boolean isLoadingMove;

    private Listener listener;

    ImageView ivSwitch;
    ImageView ivClose;
    ImageView ivMove;
    LoadingView ivLoad;
    TabLayout tlReference;
    TabLayout tlCategory;
    TabLayout tlTag;
    GridView gvPlaylist;
    ProgressBar progressBar;

    public PlaylistBrowserWidget(Service context) {
        this.mContext = context;

        playlistGridAdapter = new PlaylistGridAdapter(context);
        playlistBrowser = new PlaylistBrowser(this);
        isFirstLoad = true;
        hasNextPage = false;
        isLoadingMove = false;

        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }

        canCreate = true;
        initFloatWindow();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void initFloatWindow() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (inflater == null) {
            return;
        }
        mFloatLayout = inflater.inflate(R.layout.playlist_browser_widget, null);

        ivSwitch = mFloatLayout.findViewById(R.id.ivSwitch);
        ivClose = mFloatLayout.findViewById(R.id.ivClose);
        ivMove = mFloatLayout.findViewById(R.id.ivMove);
        ivLoad = mFloatLayout.findViewById(R.id.ivLoad);
        tlReference = mFloatLayout.findViewById(R.id.tlReference);
        tlCategory = mFloatLayout.findViewById(R.id.tlCategory);
        tlTag = mFloatLayout.findViewById(R.id.tlTag);
        gvPlaylist = mFloatLayout.findViewById(R.id.gvPlaylist);
        progressBar = mFloatLayout.findViewById(R.id.progressBar);

        ivSwitch.setOnTouchListener(this);
        ivSwitch.setOnClickListener(this);
        ivMove.setOnTouchListener(this);
        ivClose.setOnTouchListener(this);
        ivClose.setOnClickListener(this);
        tlReference.addOnTabSelectedListener(this);
        tlCategory.addOnTabSelectedListener(this);
        tlTag.addOnTabSelectedListener(this);
        gvPlaylist.setOnItemClickListener(this);
        gvPlaylist.setOnScrollListener(this);

        progressBar.setVisibility(View.GONE);

        buildReference();

        metrics = new DisplayMetrics();

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        mWindowParams.dimAmount = 0.5f;
        mWindowParams.width = metrics.widthPixels * 4 / 5;
        mWindowParams.height = metrics.heightPixels * 2 / 3;
        mWindowParams.gravity = Gravity.START | Gravity.TOP;
        mWindowParams.x = (metrics.widthPixels - mWindowParams.width) / 2;
        mWindowParams.y = (metrics.heightPixels - statusBarHeight - mWindowParams.height) / 2;
        mWindowParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
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
        if (currentCategeryList != null) {
            tlCategory.removeAllTabs();
            for (Category category : currentCategeryList) {
                TabLayout.Tab tab = tlCategory.newTab();
                tab.setTag(TAB_CAT);
                tab.setText(category.getName());
                tlCategory.addTab(tab);
            }
            tlCategory.getTabAt(DEFAULT).select();
        }
    }

    private void buildTag() {
        if (currentTags != null) {
            tlTag.removeAllTabs();
            for (Tag tag : currentTags) {
                TabLayout.Tab tab = tlTag.newTab();
                tab.setTag(TAB_TAG);
                tab.setText(tag.getName());
                tlTag.addTab(tab);
            }
            tlTag.getTabAt(DEFAULT).select();
        }
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

        } else {
            return false;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ivClose) {
            listener.onCloseWindow(mWindowParams.x, mWindowParams.y);
            remove();
        } else if (view.getId() == R.id.ivSwitch) {
            listener.onSwitchClick();
        }
    }

    @Override
    public void onTagsGet(CategoryList categories, Tag defaultTag, Boolean hasDefaultTag) {
        currentCategeryList = categories;
        buildCategory();
    }

    @Override
    public void onPlaylistsGet(PlaylistList playlists, int nextPage, Boolean hasNextPage) {
        if (isFirstLoad) {
            playlistGridAdapter.setPlaylists(playlists);
            gvPlaylist.setAdapter(playlistGridAdapter);
            isFirstLoad = false;
        } else {
            playlistGridAdapter.addPlaylists(playlists);
        }
        this.hasNextPage = hasNextPage;
        this.nextPage = nextPage;

        ivLoad.end();
        progressBar.setVisibility(View.GONE);
        isLoadingMove = false;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        ivLoad.start();

        int tag = (int) tab.getTag();
        int pos = tab.getPosition();
        switch (tag) {
            case TAB_REF:
                switch (pos) {
                    case TAB_REF_QQ_POS:
                        currentRef = REF_QQ;
                        playlistBrowser.getTagsFor(REF_QQ);
                        break;
                    case TAB_REF_163_POS:
                        currentRef = REF_163;
                        playlistBrowser.getTagsFor(REF_163);
                }
                break;
            case TAB_CAT:
                currentTags = currentCategeryList.get(pos).getTags();
                buildTag();
                break;
            case TAB_TAG:
                isFirstLoad = true;
                currentTagId = currentTags.get(pos).getId();
                playlistBrowser.getPlaylists(currentRef, currentTagId);
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
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (listener != null) {
            listener.onPlaylistClick(playlistGridAdapter.getItem(i));
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        return;
    }

    @Override
    public void onScroll(AbsListView absListView, int row, int visibile, int total) {
        if (hasNextPage && (gvPlaylist.getLastVisiblePosition() + 1) == total && !isLoadingMove) {
            isLoadingMove = true;
            progressBar.setVisibility(View.VISIBLE);
            playlistBrowser.getPlaylists(currentRef, currentTagId, nextPage);
        }
    }

    public interface Listener {
        void onPlaylistClick(Playlist playlist);
        void onSwitchClick();
        void onCloseWindow(int x, int y);
    }
}
