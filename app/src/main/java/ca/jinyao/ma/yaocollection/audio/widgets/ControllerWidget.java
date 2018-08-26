package ca.jinyao.ma.yaocollection.audio.widgets;

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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.jinyao.ma.yaocollection.R;
import ca.jinyao.ma.yaocollection.audio.adapters.PlaylistListAdapter;
import ca.jinyao.ma.yaocollection.audio.components.Song;
import ca.jinyao.ma.yaocollection.audio.components.SongList;
import ca.jinyao.ma.yaocollection.audio.views.LoadingView;
import ca.jinyao.ma.yaocollection.audio.views.RoundAngleImageView;

import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.MODE_NORMAL;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.MODE_RANDOM;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.MODE_REPEAT;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.MODE_REPEAT_LIST;

/**
 * Class ControllerWidget
 * create by jinyaoMa 0023 2018/8/23 17:46
 */
public class ControllerWidget implements View.OnTouchListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemClickListener {
    private final String TAG = "ControllerWidget";
    private final String DEFAULT_TIME_PASS = "00:00";
    private final String DEFAULT_TIME_LEFT = "99:59";

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

    private int minX;
    private int minY;
    private int maxX;
    private int maxY;

    private Boolean isListOpen;
    private Boolean isLyricOpen;
    private Boolean isTimerOpen;
    private Boolean isSettingOpen;
    private Boolean isAboutOpen;

    private PlaylistListAdapter playlistListAdapter;

    private Listener listener;

    @BindView(R.id.ivMove)
    ImageView ivMove;
    @BindView(R.id.ivSetting)
    ImageView ivSetting;
    @BindView(R.id.ivAbout)
    ImageView ivAbout;
    @BindView(R.id.ivTimer)
    ImageView ivTimer;
    @BindView(R.id.ivLyric)
    ImageView ivLyric;
    @BindView(R.id.ivLoad)
    LoadingView ivLoad;
    @BindView(R.id.cover)
    RoundAngleImageView cover;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.artist)
    TextView artist;
    @BindView(R.id.loading)
    SeekBar loading;
    @BindView(R.id.timePass)
    TextView timePass;
    @BindView(R.id.timeLeft)
    TextView timeLeft;
    @BindView(R.id.processing)
    SeekBar processing;
    @BindView(R.id.ivPlay)
    ImageView ivPlay;
    @BindView(R.id.ivPrev)
    ImageView ivPrev;
    @BindView(R.id.ivNext)
    ImageView ivNext;
    @BindView(R.id.ivList)
    ImageView ivList;
    @BindView(R.id.ivMode)
    ImageView ivMode;
    @BindView(R.id.lvList)
    ListView lvList;

    public ControllerWidget(Service context) {
        this.mContext = context;

        isListOpen = false;
        isLyricOpen = false;
        isTimerOpen = false;
        isSettingOpen = false;
        isAboutOpen = false;
        playlistListAdapter = new PlaylistListAdapter(mContext);

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
        mFloatLayout = inflater.inflate(R.layout.controller_widget, null);

        ButterKnife.bind(this, mFloatLayout);

        ivMove.setOnTouchListener(this);
        ivSetting.setOnTouchListener(this);
        ivAbout.setOnTouchListener(this);
        ivLyric.setOnTouchListener(this);
        ivTimer.setOnTouchListener(this);
        ivList.setOnTouchListener(this);
        ivPrev.setOnTouchListener(this);
        ivPlay.setOnTouchListener(this);
        ivNext.setOnTouchListener(this);
        ivMode.setOnTouchListener(this);
        ivSetting.setOnClickListener(this);
        ivAbout.setOnClickListener(this);
        ivLyric.setOnClickListener(this);
        ivTimer.setOnClickListener(this);
        ivList.setOnClickListener(this);
        ivPrev.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        ivMode.setOnClickListener(this);
        processing.setOnSeekBarChangeListener(this);
        lvList.setOnItemClickListener(this);

        timePass.setText(DEFAULT_TIME_PASS);
        timeLeft.setText(DEFAULT_TIME_LEFT);
        title.setText(TAG);
        artist.setText(TAG);

        metrics = new DisplayMetrics();

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.gravity = Gravity.START | Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = 0;

        // for Motion check
        minX = 0;
        minY = 0;
        maxX = metrics.widthPixels - mContext.getResources().getDimensionPixelOffset(R.dimen.thumb_width_height);
        maxY = metrics.heightPixels - mContext.getResources().getDimensionPixelOffset(R.dimen.thumb_width_height) + statusBarHeight;
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
        switch (view.getId()) {
            case R.id.ivMove:
            case R.id.ivSetting:
            case R.id.ivAbout:
            case R.id.ivLyric:
            case R.id.ivTimer:
            case R.id.ivList:
            case R.id.ivPrev:
            case R.id.ivPlay:
            case R.id.ivNext:
            case R.id.ivMode:
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.setBackgroundResource(R.drawable.round_widget_reverse_rectangle);
                        break;
                    case MotionEvent.ACTION_UP:
                        view.setBackground(null);
                }
        }

        if (view.getId() == R.id.ivMove) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    viewX = motionEvent.getX();
                    viewY = motionEvent.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    screenX = motionEvent.getRawX();
                    screenY = motionEvent.getRawY() - statusBarHeight;
                    mWindowParams.x = (int) (screenX - viewX);
                    if (mWindowParams.x <= minX) {
                        mWindowParams.x = minX;
                    } else if (mWindowParams.x >= maxX) {
                        mWindowParams.x = maxX;
                    }
                    mWindowParams.y = (int) (screenY - viewY);
                    if (mWindowParams.y <= minY) {
                        mWindowParams.y = minY;
                    } else if (mWindowParams.y >= maxY) {
                        mWindowParams.y = maxY;
                    }
                    mWindowManager.updateViewLayout(mFloatLayout, mWindowParams);
            }
            return true;

        } else {
            return false;
        }
    }

    private void toggleList() {
        if (isListOpen) { // Close list
            isListOpen = false;
            ivList.setImageTintList(null);
            mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mWindowManager.updateViewLayout(mFloatLayout, mWindowParams);
        } else { // Open list
            isListOpen = true;
            ivList.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorAccent)));
            mWindowParams.height = metrics.heightPixels / 2;
            mWindowManager.updateViewLayout(mFloatLayout, mWindowParams);
        }
    }

    public void setSettingState(Boolean isSettingOpen) {
        this.isSettingOpen = isSettingOpen;
        if (isSettingOpen) {
            ivSetting.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorAccent)));
        } else {
            ivSetting.setImageTintList(null);
        }
    }

    public void setAboutState(Boolean isAboutOpen) {
        this.isAboutOpen = isAboutOpen;
        if (isAboutOpen) {
            ivAbout.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorAccent)));
        } else {
            ivAbout.setImageTintList(null);
        }
    }

    public void setLyricState(Boolean isLyricOpen) {
        this.isLyricOpen = isLyricOpen;
        if (isLyricOpen) {
            ivLyric.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorAccent)));
        } else {
            ivLyric.setImageTintList(null);
        }
    }

    public void setTimerState(Boolean isTimerOpen) {
        this.isTimerOpen = isTimerOpen;
        if (isTimerOpen) {
            ivTimer.setImageTintList(ColorStateList.valueOf(mContext.getColor(R.color.colorAccent)));
        } else {
            ivTimer.setImageTintList(null);
        }
    }

    public void setModeState(int mode) {
        switch (mode) {
            case MODE_NORMAL:
                ivMode.setImageResource(R.drawable.ic_action_mode_normal);
                break;
            case MODE_RANDOM:
                ivMode.setImageResource(R.drawable.ic_action_mode_random);
                break;
            case MODE_REPEAT:
                ivMode.setImageResource(R.drawable.ic_action_mode_repeat);
                break;
            case MODE_REPEAT_LIST:
                ivMode.setImageResource(R.drawable.ic_action_mode_repeat_list);
        }
    }

    public void setLoading(int percentage) {
        loading.setProgress(percentage, true);
    }

    public void setProgress(long current, long duration) { // in millisecond
        if (duration > 0) {
            processing.setMax((int) duration);
            processing.setProgress((int) current, true);

            long passSecond = current / 1000 % 60;
            long passMinute = current / 1000 / 60;
            long leftSecond = (duration - current) / 1000 % 60;
            long leftMinute = (duration - current) / 1000 / 60;

            timePass.setText(String.format("%02d:%02d", passMinute, passSecond));
            timeLeft.setText(String.format("%02d:%02d", leftMinute, leftSecond));
        } else {
            processing.setProgress(0);
            timePass.setText("00:00");
            timeLeft.setText("??:??");
        }
    }

    public void setSonglist(SongList songlist) {
        playlistListAdapter.setSongs(songlist);
        lvList.setAdapter(playlistListAdapter);
    }

    public void addSong(Song song) {
        playlistListAdapter.addSong(song);
    }

    public void setSongInfo(int index) {
        if (index < playlistListAdapter.getCount()) {
            Song song = playlistListAdapter.getSong(index);
            cover.setImagePath(song.getCoverPath());
            title.setText(song.songTitle);
            artist.setText(song.getArtists().getNameString());
            playlistListAdapter.setHighlight(index);
        }
    }

    public void setPlayState(Boolean isPlaying) {
        if (isPlaying) {
            ivPlay.setImageResource(R.drawable.ic_action_pause);
        } else {
            ivPlay.setImageResource(R.drawable.ic_action_play);
        }
    }

    public void setLoadingNetwork(Boolean isStart) {
        if (isStart) {
            ivLoad.start();
        } else {
            ivLoad.end();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivList:
                toggleList();
                break;
        }

        if (listener != null) {
            switch (view.getId()) {
                case R.id.ivSetting:
                    listener.onSettingClick(isSettingOpen);
                    break;
                case R.id.ivAbout:
                    listener.onAboutClick(isAboutOpen);
                    break;
                case R.id.ivLyric:
                    listener.onLyricClick(playlistListAdapter.getCurrentReference(), playlistListAdapter.getCurrentSongId());
                    break;
                case R.id.ivTimer:
                    listener.onTimerClick(isTimerOpen);
                    break;
                case R.id.ivPrev:
                    listener.onPrevClick();
                    break;
                case R.id.ivPlay:
                    listener.onPlayClick();
                    break;
                case R.id.ivNext:
                    listener.onNextClick();
                    break;
                case R.id.ivMode:
                    listener.onModeClick();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        return;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        return;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (listener != null) {
            listener.onProgressUpdate(seekBar.getProgress(), seekBar.getMax());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (listener != null) {
            listener.onListItemClick(i, playlistListAdapter.getSong(i));
        }
    }

    public interface Listener {
        void onProgressUpdate(int newProgress, int max);

        void onSettingClick(Boolean isSettingOpen);

        void onAboutClick(Boolean isAboutOpen);

        void onLyricClick(int ref, String id);

        void onTimerClick(Boolean isTimerOpen);

        void onPlayClick();

        void onPrevClick();

        void onNextClick();

        void onModeClick();

        void onListItemClick(int index, Song song);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
