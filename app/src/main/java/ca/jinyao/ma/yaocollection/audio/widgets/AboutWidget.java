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
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.jinyao.ma.yaocollection.R;

/**
 * Class AboutWidget
 * create by jinyaoMa 0025 2018/8/25 12:36
 */
public class AboutWidget implements View.OnTouchListener, View.OnClickListener {
    private final String TAG = "AboutWidget";
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

    @BindView(R.id.ivMove)
    ImageView ivMove;
    @BindView(R.id.ivClose)
    ImageView ivClose;
    @BindView(R.id.icon)
    ImageView icon;
    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.tvVersion)
    TextView tvVersion;
    @BindView(R.id.tvAuthor)
    TextView tvAuthor;
    @BindView(R.id.tvSource)
    TextView tvSource;
    @BindView(R.id.tvThanks)
    TextView tvThanks;

    public AboutWidget(Service context) {
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
        mFloatLayout = inflater.inflate(R.layout.about_widget, null);

        ButterKnife.bind(this, mFloatLayout);

        ivMove.setOnTouchListener(this);
        ivClose.setOnTouchListener(this);
        ivClose.setOnClickListener(this);

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
        mWindowParams.x = (metrics.widthPixels - mWindowParams.width) / 2;
        mWindowParams.y = metrics.heightPixels / 3;
    }

    public void setInfo(int iconResourceId,
                        String name,
                        String version,
                        String author,
                        String source,
                        String thanks) {
        icon.setImageResource(iconResourceId);
        tvName.setText(name);
        tvVersion.setText(version);
        tvAuthor.setText(author);
        tvSource.setText(source);
        tvThanks.setText(thanks);
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

    public interface Listener {
        void onClose();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
