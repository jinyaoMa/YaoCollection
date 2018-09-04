package ca.jinyao.ma.video.widgets;

import android.app.Service;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import ca.jinyao.ma.video.R;

/**
 * Class NavigationWidget
 * create by jinyaoMa 0001 2018/9/1 17:26
 */
public class NavigationWidget implements View.OnTouchListener, View.OnClickListener {
    private final String TAG = "NavigationWidget";

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

    private Listener listener;

    private Boolean isVideoOn;
    private Boolean isBrowseOn;
    private Boolean isSearchOn;
    private Boolean isListOn;

    private boolean canClick;

    ImageView ivVideo;
    ImageView ivBrowse;
    ImageView ivSearch;
    ImageView ivList;

    public NavigationWidget(Service context) {
        this.mContext = context;

        isVideoOn = false;
        isBrowseOn = false;
        isSearchOn = false;
        isListOn = false;

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
        mFloatLayout = inflater.inflate(R.layout.navigation_widget, null);

        ivVideo = mFloatLayout.findViewById(R.id.ivVideo);
        ivBrowse = mFloatLayout.findViewById(R.id.ivBrowse);
        ivSearch = mFloatLayout.findViewById(R.id.ivSearch);
        ivList = mFloatLayout.findViewById(R.id.ivList);

        ivVideo.setOnTouchListener(this);
        ivBrowse.setOnTouchListener(this);
        ivSearch.setOnTouchListener(this);
        ivList.setOnTouchListener(this);
        ivVideo.setOnClickListener(this);
        ivBrowse.setOnClickListener(this);
        ivSearch.setOnClickListener(this);
        ivList.setOnClickListener(this);

        metrics = new DisplayMetrics();

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.gravity = Gravity.START | Gravity.TOP;
        mWindowParams.x = metrics.widthPixels;
        mWindowParams.y = (metrics.heightPixels - statusBarHeight) / 2;
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

    public void setVideoOn(Boolean videoOn) {
        isVideoOn = videoOn;
        if (isVideoOn) {
            ivVideo.setImageResource(R.drawable.ic_action_close_move);
            ivVideo.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorAccent)));

            ivBrowse.setVisibility(View.VISIBLE);
            ivSearch.setVisibility(View.VISIBLE);
            ivList.setVisibility(View.VISIBLE);

        } else {
            ivVideo.setImageResource(R.drawable.ic_action_video);
            ivVideo.setImageTintList(null);

            ivBrowse.setVisibility(View.GONE);
            ivSearch.setVisibility(View.GONE);
            ivList.setVisibility(View.GONE);
        }

        if (!canCreate) {
            mWindowManager.updateViewLayout(mFloatLayout, mWindowParams);
        }
    }

    public void setBrowseOn(Boolean browseOn) {
        isBrowseOn = browseOn;
        if (isBrowseOn) {
            ivBrowse.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorAccent)));
        } else {
            ivBrowse.setImageTintList(null);
        }
    }

    public void setSearchOn(Boolean searchOn) {
        isSearchOn = searchOn;
        if (isSearchOn) {
            ivSearch.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorAccent)));
        } else {
            ivSearch.setImageTintList(null);
        }
    }

    public void setListOn(Boolean listOn) {
        isListOn = listOn;
        if (isListOn) {
            ivList.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorAccent)));
        } else {
            ivList.setImageTintList(null);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == R.id.ivVideo) {
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
                    canClick = false;
                    break;
                case MotionEvent.ACTION_UP:
                    view.setBackground(null);
                    if (canClick && listener != null) {
                        listener.onVideoClick(isVideoOn);
                    }
                    canClick = true;
            }

            return true;

        } else {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setBackgroundResource(R.drawable.round_widget_reverse_rectangle);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setBackground(null);
            }
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            if (view.getId() == R.id.ivBrowse) {
                listener.onBrowseClick(isBrowseOn);
            } else if (view.getId() == R.id.ivSearch) {
                listener.onSearchClick(isSearchOn);
            } else if (view.getId() == R.id.ivList) {
                listener.onListClick(isListOn);
            }
        }
    }

    public interface Listener {
        void onVideoClick(Boolean isVideoOn);

        void onBrowseClick(Boolean isBrowseOn);

        void onSearchClick(Boolean isSearchOn);

        void onListClick(Boolean isListOn);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
