package ca.jinyao.ma.yaocollection.audio.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import ca.jinyao.ma.yaocollection.audio.cachers.ImageCacher;

/**
 * Class RoundAngleImageView
 * create by jinyaoMa 0014 2018/8/14 14:46
 */
public class RoundAngleImageView extends android.support.v7.widget.AppCompatImageView {
    public static final float ANGLE_TYPE_CIRCLE = 0.5f;
    public static final float ANGLE_TYPE_ROUND = 0.25f;
    public static final float ANGLE_TYPE_SQUARE = 0f;

    public static final float DEFAULT_MAX_SCALE = 1.25f;

    private Paint paint;
    private Paint paint2;
    private float roundWidth;
    private float roundHeight;
    private float currentAngleType;
    private float maxScale;

    public RoundAngleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RoundAngleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundAngleImageView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        paint2 = new Paint();
        paint2.setXfermode(null);

        currentAngleType = ANGLE_TYPE_ROUND;
        maxScale = DEFAULT_MAX_SCALE;
    }

    public void setImagePath(String path) {
        ImageCacher.getImage(path, new ImageCacher.ImageCacheListener() {
            @Override
            public void onCompleted(Bitmap bitmap, String path) {
                setImageBitmap(bitmap);
            }
        });
    }

    public void setAngleType(float angle) {
        currentAngleType = angle;
        invalidate();
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
        invalidate();
    }

    public float getMaxScale() {
        return maxScale;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (widthMeasureSpec < heightMeasureSpec) {
            widthMeasureSpec = heightMeasureSpec;
        } else {
            heightMeasureSpec = widthMeasureSpec;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void draw(Canvas canvas) {
        roundWidth = getWidth() * currentAngleType;
        roundHeight = getHeight() * currentAngleType;

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
        Canvas clearAngleCanvas = new Canvas(bitmap);
        super.draw(clearAngleCanvas);
        drawLeftUp(clearAngleCanvas);
        drawLeftDown(clearAngleCanvas);
        drawRightUp(clearAngleCanvas);
        drawRightDown(clearAngleCanvas);
        canvas.drawBitmap(bitmap, 0, 0, paint2);
        bitmap.recycle();
    }

    private void drawLeftUp(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, roundHeight);
        path.lineTo(0, 0);
        path.lineTo(roundWidth, 0);
        path.arcTo(new RectF(0, 0, roundWidth * 2, roundHeight * 2), -90, -90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawLeftDown(Canvas canvas) {
        Path path = new Path();
        path.moveTo(0, getHeight() - roundHeight);
        path.lineTo(0, getHeight());
        path.lineTo(roundWidth, getHeight());
        path.arcTo(new RectF(0, getHeight() - roundHeight * 2, roundWidth * 2, getHeight()), 90, 90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRightDown(Canvas canvas) {
        Path path = new Path();
        path.moveTo(getWidth() - roundWidth, getHeight());
        path.lineTo(getWidth(), getHeight());
        path.lineTo(getWidth(), getHeight() - roundHeight);
        path.arcTo(new RectF(getWidth() - roundWidth * 2, getHeight() - roundHeight * 2, getWidth(), getHeight()), -0, 90);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawRightUp(Canvas canvas) {
        Path path = new Path();
        path.moveTo(getWidth(), roundHeight);
        path.lineTo(getWidth(), 0);
        path.lineTo(getWidth() - roundWidth, 0);
        path.arcTo(new RectF(getWidth() - roundWidth * 2, 0, getWidth(), roundHeight * 2), -90, 90);
        path.close();
        canvas.drawPath(path, paint);
    }

}