package ca.jinyao.ma.yaocollection.audio.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

/**
 * Class LoadingView
 * create by jinyaoMa 0023 2018/8/23 17:45
 */
public class LoadingView extends android.support.v7.widget.AppCompatImageView implements ValueAnimator.AnimatorUpdateListener {
    private float pivotX;
    private float pivotY;
    private ValueAnimator valueAnimator;

    public LoadingView(Context context) {
        super(context);
        init();
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        valueAnimator = ValueAnimator.ofFloat(0, 360);
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.addUpdateListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        pivotX = getWidth() / 2;
        pivotY = getHeight() / 2;
        setPivotX(pivotX);
        setPivotY(pivotY);
    }

    public void start() {
        setVisibility(View.VISIBLE);
        if (valueAnimator != null) {
            valueAnimator.start();
        }
    }

    public void end() {
        setVisibility(View.GONE);
        if (valueAnimator != null) {
            valueAnimator.end();
        }
    }

    public Boolean isLoading() {
        return valueAnimator.isRunning();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float a = (float) valueAnimator.getAnimatedValue();
        setRotation(a);
    }
}
