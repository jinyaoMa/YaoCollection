package ca.jinyao.ma.audio.widgets;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import ca.jinyao.ma.audio.R;
import ca.jinyao.ma.audio.adapters.LyricListAdapter;
import ca.jinyao.ma.audio.components.Lyric;

/**
 * Class LyricWidget
 * create by jinyaoMa 0024 2018/8/24 17:16
 */
public class LyricWidget implements View.OnTouchListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private final String TAG = "LyricWidget";
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

    private LyricListAdapter lyricListAdapter;
    private Lyric currentLyric;
    private Lyric.Line currentLine;

    private int lyricListOffsetY;

    private Listener listener;

    ImageView ivClose;
    ImageView ivMove;
    Switch sMode;
    ConstraintLayout panel;
    TextView line1;
    TextView line2;
    ListView lvLyric;

    public LyricWidget(Service context) {
        this.mContext = context;

        lyricListAdapter = new LyricListAdapter(context);

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
        mFloatLayout = inflater.inflate(R.layout.lyric_widget, null);

        ivClose = mFloatLayout.findViewById(R.id.ivClose);
        ivMove = mFloatLayout.findViewById(R.id.ivMove);
        sMode = mFloatLayout.findViewById(R.id.sMode);
        panel = mFloatLayout.findViewById(R.id.panel);
        line1 = mFloatLayout.findViewById(R.id.line1);
        line2 = mFloatLayout.findViewById(R.id.line2);
        lvLyric = mFloatLayout.findViewById(R.id.lvLyric);

        ivClose.setOnClickListener(this);
        ivMove.setOnTouchListener(this);
        sMode.setOnCheckedChangeListener(this);

        sMode.setChecked(false);

        metrics = new DisplayMetrics();

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        mWindowParams.format = PixelFormat.RGBA_8888;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowParams.width = metrics.widthPixels * 4 / 5;
        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.gravity = Gravity.START | Gravity.TOP;
        mWindowParams.x = 0;
        mWindowParams.y = 0;

        lyricListOffsetY = (metrics.heightPixels / 3 - mContext.getResources().getDimensionPixelOffset(R.dimen.thumb_width_height)) / 3;
    }

    public void create() {
        if (canCreate) {
            mWindowManager.addView(mFloatLayout, mWindowParams);

            canCreate = false;
        }
    }

    public void setLyric(Lyric lyric) {
        String words = lyric.getLineAtIndex(0).getWords();
        String nextWords = lyric.getLineAtIndex(0).getNextWords();
        line1.setText(words);
        line2.setText(nextWords);
        currentLine = lyric.getLineAtIndex(0);

        lyricListAdapter.setLines(lyric.getLines());
        lvLyric.setAdapter(lyricListAdapter);
        currentLyric = lyric;
    }

    public void remove() {
        if (!canCreate) {
            mWindowManager.removeView(mFloatLayout);

            canCreate = true;
        }
    }

    public void goToTimeline(long position) {
        if (currentLyric == null) {
            return;
        }
        Lyric.Line nextLine = currentLyric.getLineAtTimeline(position);
        if (nextLine == null) {
            return;
        }
        line1.setText(nextLine.getWords());
        line1.setTextColor(mContext.getColor(R.color.colorPrimary));
        line2.setText(nextLine.getNextWords());
        line2.setTextColor(Color.BLACK);

        int nextIndex = currentLyric.getIndexAtTimeline(position);
        if (nextIndex < 0) {
            return;
        }
        lyricListAdapter.setHighlight(nextIndex);
        lvLyric.smoothScrollToPositionFromTop(nextIndex, lyricListOffsetY);
    }

    private void setMode(Boolean flag) {
        if (flag) {
            panel.setVisibility(View.GONE);
            lvLyric.setVisibility(View.VISIBLE);
            mWindowParams.height = metrics.heightPixels / 3;
            mWindowManager.updateViewLayout(mFloatLayout, mWindowParams);
        } else {
            panel.setVisibility(View.VISIBLE);
            lvLyric.setVisibility(View.GONE);
            mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mWindowManager.updateViewLayout(mFloatLayout, mWindowParams);
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
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        setMode(b);
    }

    public interface Listener {
        void onClose();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
