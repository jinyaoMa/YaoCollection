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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.jinyao.ma.yaocollection.R;
import ca.jinyao.ma.yaocollection.audio.cores.AudioConfig;

/**
 * Class SettingWidget
 * create by jinyaoMa 0025 2018/8/25 0:12
 */
public class SettingWidget implements View.OnTouchListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private final String TAG = "SettingWidget";
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

    @BindView(R.id.ivMove)
    ImageView ivMove;
    @BindView(R.id.ivClearAll)
    ImageView ivClearAll;
    @BindView(R.id.ivClearImage)
    ImageView ivClearImage;
    @BindView(R.id.ivClearSong)
    ImageView ivClearSong;
    @BindView(R.id.ivClearLyric)
    ImageView ivClearLyric;
    @BindView(R.id.imageSpace)
    TextView imageSpace;
    @BindView(R.id.songSpace)
    TextView songSpace;
    @BindView(R.id.lyricSpace)
    TextView lyricSpace;
    @BindView(R.id.sProxy)
    Switch sProxy;

    public SettingWidget(Service context) {
        this.mContext = context;


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
        mFloatLayout = inflater.inflate(R.layout.setting_widget, null);

        ButterKnife.bind(this, mFloatLayout);

        ivMove.setOnTouchListener(this);
        ivClearAll.setOnTouchListener(this);
        ivClearImage.setOnTouchListener(this);
        ivClearSong.setOnTouchListener(this);
        ivClearLyric.setOnTouchListener(this);
        ivClearAll.setOnClickListener(this);
        ivClearImage.setOnClickListener(this);
        ivClearSong.setOnClickListener(this);
        ivClearLyric.setOnClickListener(this);
        sProxy.setOnCheckedChangeListener(this);

        sProxy.setChecked(false);

        metrics = new DisplayMetrics();

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowParams.width = metrics.widthPixels * 2 / 3;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.gravity = Gravity.START | Gravity.TOP;
        mWindowParams.x = (metrics.widthPixels - mWindowParams.width) / 2;
        mWindowParams.y = metrics.heightPixels / 3;
    }

    public void setProxyEnable(Boolean flag) {
        sProxy.setChecked(flag);
    }

    public void create() {
        if (!AudioConfig.isProxyEnabled) {
            sProxy.setChecked(false);
        }
        listener.onProxyChange(AudioConfig.isProxyEnabled);

        refreshAll();

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

    private void refreshAll() {
        refreshImage();
        refreshSong();
        refreshLyric();
    }

    private void refreshImage() {
        File imageDir = new File(AudioConfig.imageCachePath);
        if (imageDir.exists() && imageDir.isDirectory()) {
            String space = String.format("%d / %d", imageDir.list().length - 1, AudioConfig.imageCacheLimit);
            imageSpace.setText(space);
        } else {
            String space = String.format("??? / %d", AudioConfig.imageCacheLimit);
            imageSpace.setText(space);
        }
    }

    private void refreshSong() {
        File songDir = new File(AudioConfig.songCachePath);
        if (songDir.exists() && songDir.isDirectory()) {
            String space = String.format("%d / %d", songDir.list().length - 1, AudioConfig.songCacheLimit);
            songSpace.setText(space);
        } else {
            String space = String.format("??? / %d", AudioConfig.songCacheLimit);
            songSpace.setText(space);
        }

    }

    private void refreshLyric() {
        File lyricDir = new File(AudioConfig.lyricCachePath);
        if (lyricDir.exists() && lyricDir.isDirectory()) {
            String space = String.format("%d / %d", lyricDir.list().length - 1, AudioConfig.lyricCacheLimit);
            lyricSpace.setText(space);
        } else {
            String space = String.format("??? / %d", AudioConfig.lyricCacheLimit);
            lyricSpace.setText(space);
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

        } else {
            switch (view.getId()) {
                case R.id.ivClearAll:
                case R.id.ivClearImage:
                case R.id.ivClearSong:
                case R.id.ivClearLyric:
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
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivClearAll:
                AudioConfig.clearCache();
                refreshAll();
                break;
            case R.id.ivClearImage:
                AudioConfig.clearImageCache();
                refreshImage();
                break;
            case R.id.ivClearSong:
                AudioConfig.clearSongCache();
                refreshSong();
                break;
            case R.id.ivClearLyric:
                AudioConfig.clearLyricCache();
                refreshLyric();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        listener.onProxyChange(b);
        if (b) {
            AudioConfig.enableProxy();
        } else {
            AudioConfig.isProxyEnabled = false;
        }
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onProxyChange(Boolean isOn);
    }
}
