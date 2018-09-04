package ca.jinyao.ma.audio.widgets;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import ca.jinyao.ma.audio.R;
import ca.jinyao.ma.audio.views.RoundAngleImageView;
import ca.jinyao.ma.audio.views.VisualizerView;

/**
 * Class QuickAccessWidget
 * create by jinyaoMa 0014 2018/8/14 18:54
 */
public class QuickAccessWidget implements View.OnTouchListener {
    public static final int DEFAULT_WIDTH = 50;
    public static final int DEFAULT_HEIGHT = 50;

    private static final String TAG = "QuickAccessWidget";
    private Context context;

    private static final int VIEW_ID = 960;
    private RoundAngleImageView roundAngleImageView;
    private ViewGroup.LayoutParams roundAngleParams;
    private int width;
    private int height;
    private float maxScale;

    private static final int LAYOUT_ID = 328;
    private ConstraintLayout constraintLayout;
    private ConstraintLayout.LayoutParams constraintParams;
    private int constraintWidth;
    private int constraintHeight;

    private DisplayMetrics dm;
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;
    private Boolean canCreate;

    private int statusBarHeight = 0;

    private Boolean canClick;
    private OnClickListener onClickListener;

    private static final int VISUAL_ID = 1996;
    private VisualizerView visualizerView;

    public interface OnClickListener {
        void onClick();
    }

    public QuickAccessWidget(Service context) {
        this(context, -1, -1);
    }

    public QuickAccessWidget(Service context, int w, int h) {
        this.context = context;
        dm = new DisplayMetrics();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        constraintLayout = new ConstraintLayout(context);
        roundAngleImageView = new RoundAngleImageView(context);
        visualizerView = new VisualizerView(context);

        // RoundAngleImageView
        width = (int) (w < 0 ? DEFAULT_WIDTH * dm.density : w);
        height = (int) (h < 0 ? DEFAULT_HEIGHT * dm.density : h);
        maxScale = roundAngleImageView.getMaxScale();

        roundAngleParams = new ViewGroup.LayoutParams(width, height);

        roundAngleImageView.setLayoutParams(roundAngleParams);
        roundAngleImageView.setId(VIEW_ID);
        roundAngleImageView.setAngleType(RoundAngleImageView.ANGLE_TYPE_CIRCLE);
        roundAngleImageView.setOnTouchListener(this);

        // VisualizerView
        visualizerView.setStrokeWidth(3);
        visualizerView.setColor(context.getColor(R.color.colorPrimary));
        visualizerView.setId(VISUAL_ID);
        visualizerView.setLayoutParams(new ViewGroup.LayoutParams(width, height));

        // ConstraintLayout
        constraintWidth = (int) (width * maxScale);
        constraintHeight = (int) (height * maxScale);

        constraintParams = new ConstraintLayout.LayoutParams(constraintWidth, constraintHeight);

        constraintLayout.setLayoutParams(constraintParams);
        constraintLayout.addView(visualizerView);
        constraintLayout.addView(roundAngleImageView);
        constraintLayout.setId(LAYOUT_ID);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.connect(roundAngleImageView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.connect(roundAngleImageView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(roundAngleImageView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(roundAngleImageView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        constraintSet.connect(visualizerView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.connect(visualizerView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(visualizerView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        constraintSet.connect(visualizerView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.applyTo(constraintLayout);

        // Status bar
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }

        // Floating window
        windowParams = new WindowManager.LayoutParams();

        windowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        windowParams.format = PixelFormat.RGBA_8888;
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        windowParams.gravity = Gravity.START | Gravity.TOP;
        windowParams.x = dm.widthPixels;
        windowParams.y = dm.heightPixels / 2 - statusBarHeight;

        windowParams.width = constraintWidth;
        windowParams.height = constraintHeight;

        // Others
        canCreate = true;
        canClick = true;
    }

    public VisualizerView getVisualizerView() {
        return visualizerView;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void create() {
        if (canCreate && windowManager != null) {
            windowManager.addView(constraintLayout, windowParams);
            canCreate = false;
        }
    }

    public void remove() {
        if (windowManager != null && constraintLayout != null) {
            windowManager.removeView(constraintLayout);
            canCreate = true;
        }
    }

    public void setPosition(int x, int y) {
        windowParams.x = x;
        windowParams.y = y;

        if (!canCreate && windowManager != null) {
            windowManager.updateViewLayout(constraintLayout, windowParams);
        }
    }

    public int getPositionX() {
        return windowParams.x;
    }

    public int getPositionY() {
        return windowParams.y;
    }

    public void setForeground(int resId) {
        roundAngleImageView.setImageResource(resId);
    }

    public void setBackground(int resId) {
        constraintLayout.setBackgroundResource(resId);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (roundAngleImageView != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            roundAngleImageView.setScaleX(maxScale);
            roundAngleImageView.setScaleY(maxScale);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            roundAngleImageView.setScaleX(1f);
            roundAngleImageView.setScaleY(1f);
        }

        if (windowParams != null && event.getAction() == MotionEvent.ACTION_MOVE) {
            windowParams.x = (int) event.getRawX() - windowParams.width / 2;
            windowParams.y = (int) event.getRawY() - windowParams.height / 2 - statusBarHeight;
            windowManager.updateViewLayout(constraintLayout, windowParams);
            canClick = false;
        }

        if (onClickListener != null && event.getAction() == MotionEvent.ACTION_UP) {
            if (canClick) {
                onClickListener.onClick();
            }
            canClick = true;
        }

        return true;
    }
}
