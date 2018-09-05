package ca.jinyao.ma.video.widgets;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.design.widget.TabLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;

import ca.jinyao.ma.video.R;
import ca.jinyao.ma.video.adapters.VideoListAdapter;
import ca.jinyao.ma.video.components.Catalogue;
import ca.jinyao.ma.video.components.CatalogueList;
import ca.jinyao.ma.video.components.CatalogueTable;
import ca.jinyao.ma.video.components.Video;
import ca.jinyao.ma.video.components.VideoList;
import ca.jinyao.ma.video.cores.SourceBrowser;
import ca.jinyao.ma.video.cores.VideoConfig;

/**
 * Class SourceBrowserWidget
 * create by jinyaoMa 0002 2018/9/2 0:02
 */
public class SourceBrowserWidget implements View.OnTouchListener, View.OnClickListener, SourceBrowser.Listener, TabLayout.OnTabSelectedListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private final String TAG = "SourceBrowserWidget";

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

    private SourceBrowser sourceBrowser;
    private VideoListAdapter videoListAdapter;

    private int nextPage;
    private Boolean hasNext;
    private Boolean isMore;
    private String currentUrl;

    private final String IS_TYPE = "Is_Type";
    private final int TAB_MOVIE = 0;
    private final int TAB_TVP = 1;
    private final int TAB_ANIME = 2;

    private int[] offsetRecords;

    private Listener listener;

    ImageView ivMove;
    ImageView ivClose;
    ProgressBar pbLoad;
    LinearLayout catalogueContainer;
    ListView lvVideo;
    ProgressBar pbMore;
    TabLayout tlType;

    public SourceBrowserWidget(Service context) {
        this.mContext = context;

        sourceBrowser = new SourceBrowser(this);
        videoListAdapter = new VideoListAdapter(context);
        nextPage = 1;
        hasNext = false;
        isMore = false;
        currentUrl = "";
        offsetRecords = null;

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
        mFloatLayout = inflater.inflate(R.layout.source_browser_widget, null);

        ivMove = mFloatLayout.findViewById(R.id.ivMove);
        ivClose = mFloatLayout.findViewById(R.id.ivClose);
        pbLoad = mFloatLayout.findViewById(R.id.pbLoad);
        catalogueContainer = mFloatLayout.findViewById(R.id.catalogueContainer);
        lvVideo = mFloatLayout.findViewById(R.id.lvVideo);
        pbMore = mFloatLayout.findViewById(R.id.pbMore);
        tlType = mFloatLayout.findViewById(R.id.tlType);

        ivMove.setOnTouchListener(this);
        ivClose.setOnTouchListener(this);
        ivClose.setOnClickListener(this);
        tlType.addOnTabSelectedListener(this);
        lvVideo.setOnItemClickListener(this);
        lvVideo.setOnScrollListener(this);

        buildTypeHeader();
        lvVideo.setAdapter(videoListAdapter);

        metrics = new DisplayMetrics();

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowParams.width = metrics.widthPixels * 4 / 5;
        mWindowParams.height = metrics.heightPixels * 4 / 5;
        mWindowParams.gravity = Gravity.START | Gravity.TOP;
        mWindowParams.x = (metrics.widthPixels - mWindowParams.width) / 2;
        mWindowParams.y = (metrics.heightPixels - mWindowParams.height) / 2;
    }

    private void buildTypeHeader() {
        TabLayout.Tab movie = tlType.newTab();
        movie.setIcon(R.drawable.ic_action_browse_video);
        movie.setTag(IS_TYPE);

        TabLayout.Tab tvp = tlType.newTab();
        tvp.setIcon(R.drawable.ic_action_tv);
        tvp.setTag(IS_TYPE);

        TabLayout.Tab anime = tlType.newTab();
        anime.setIcon(R.drawable.ic_action_anime);
        anime.setTag(IS_TYPE);

        tlType.addTab(movie, TAB_MOVIE);
        tlType.addTab(tvp, TAB_TVP);
        tlType.addTab(anime, TAB_ANIME, true);
    }

    private void buildCatalogueByType(String type) {
        pbLoad.setVisibility(View.VISIBLE);
        sourceBrowser.getByType(type);
    }

    private void buildCatalogueByUrl(String url) {
        if (isMore) {
            pbMore.setVisibility(View.VISIBLE);
        } else {
            pbLoad.setVisibility(View.VISIBLE);
        }

        sourceBrowser.getByUrl(url);
    }

    private void buildCatalogueByUrl(String url, int page) {
        if (isMore) {
            pbMore.setVisibility(View.VISIBLE);
        } else {
            pbLoad.setVisibility(View.VISIBLE);
        }

        sourceBrowser.getByUrl(url, page);
    }

    public void create() {
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
        } else {

            return false;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ivClose) {
            remove();
            if (listener != null) {
                listener.onClose();
            }
        }
    }

    @Override
    public void onGetSourceComplete(CatalogueTable catalogueTable, VideoList videoList, int nextPage, Boolean hasNext) {
        this.nextPage = nextPage;
        this.hasNext = hasNext;

        catalogueContainer.removeAllViews();
        for (int i = 0; i < catalogueTable.size(); i++) {
            CatalogueList catalogues = catalogueTable.get(i);
            final TabLayout tabLayout = new TabLayout(mFloatLayout.getContext());
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            ArrayList<String> tempList = new ArrayList<>();

            for (int j = 0; j < catalogues.size(); j++) {
                Catalogue catalogue = catalogues.get(j);
                TabLayout.Tab tab = tabLayout.newTab();
                tab.setTag(catalogue.getUrl());
                tab.setText(catalogue.getName());
                if (catalogue.isSelected()) {
                    tabLayout.addTab(tab, true);
                } else {
                    tabLayout.addTab(tab);
                }
                tempList.add(catalogue.getName());
            }

            catalogueContainer.addView(tabLayout);
            tabLayout.addOnTabSelectedListener(this);
            if (offsetRecords != null && i < offsetRecords.length) {
                final int expectedOffset = offsetRecords[i];
                tabLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        tabLayout.scrollTo(expectedOffset, 0);
                    }
                });
            }
        }

        videoListAdapter.setVideos(videoList);

        pbLoad.setVisibility(View.GONE);
        pbMore.setVisibility(View.GONE);
        isMore = false;
    }

    @Override
    public void onError() {

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        String msg = (String) tab.getTag();
        if (msg.equals(IS_TYPE)) {
            offsetRecords = null;
            switch (tab.getPosition()) {
                case TAB_MOVIE:
                    buildCatalogueByType(VideoConfig.TYPE_MOVIE);
                    break;
                case TAB_TVP:
                    buildCatalogueByType(VideoConfig.TYPE_TVP);
                    break;
                case TAB_ANIME:
                    buildCatalogueByType(VideoConfig.TYPE_ANIME);
            }
        } else {
            offsetRecords = new int[catalogueContainer.getChildCount()];
            for (int i = 0; i < offsetRecords.length; i++) {
                if (catalogueContainer.getChildAt(i) instanceof TabLayout) {
                    offsetRecords[i] = catalogueContainer.getChildAt(i).getScrollX();
                }
            }
            currentUrl = msg;
            buildCatalogueByUrl(msg);
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
            listener.onVideoClick(videoListAdapter.getVideos().get(i));
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        return;
    }

    @Override
    public void onScroll(AbsListView absListView, int row, int visibile, int total) {
        if (hasNext && (lvVideo.getLastVisiblePosition() + 1) == total && !isMore) {
            isMore = true;
            buildCatalogueByUrl(currentUrl, nextPage);
        }
    }

    public interface Listener {
        void onVideoClick(Video video);

        void onClose();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private int getTablayoutOffsetWidth(ArrayList<String> channelNameList, int index) {
        String str = "";
        for (int i = 0; i < index; i++) {
            str += channelNameList.get(i);
        }
        return str.length() * 50; // 14 = font-size,
    }
}
