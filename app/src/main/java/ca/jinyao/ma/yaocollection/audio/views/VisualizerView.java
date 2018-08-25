package ca.jinyao.ma.yaocollection.audio.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

/**
 * Class VisualizerView
 * create by jinyaoMa 0013 2018/8/13 22:06
 */
public class VisualizerView extends View {
    public static final int TYPE_BLOCK = 0;
    public static final int TYPE_BAR = 1;
    public static final int TYPE_WAVE = 2;

    public static final float DEFAULT_STROKE_WIDTH = 1f;
    public static final int DEFAULT_STROKE_COLOR = Color.YELLOW;

    private final int MIN_CAPTURE_SIZE_INDEX = 0;

    private byte[] bytes;
    private float[] points;
    private Paint paint;
    private Rect rect;
    private int type;

    private int captureSizeRange;
    private int waveStep;
    private int canvasBackgroundColor;

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        bytes = null;
        points = null;
        paint = new Paint();
        rect = new Rect();
        type = TYPE_WAVE;
        captureSizeRange = Visualizer.getCaptureSizeRange()[MIN_CAPTURE_SIZE_INDEX];
        waveStep = (int) DEFAULT_STROKE_WIDTH;
        canvasBackgroundColor = Color.TRANSPARENT;

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
        paint.setColor(DEFAULT_STROKE_COLOR);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    public void setStrokeWidth(float width) {
        if (width < DEFAULT_STROKE_WIDTH) {
            return;
        }
        paint.setStrokeWidth(width);
        waveStep = (int) (width - DEFAULT_STROKE_WIDTH);
    }

    public void updateVisualizer(byte[] wave) {
        bytes = wave;
        invalidate();
    }

    public void toggleType() {
        type++;
        if (type > TYPE_WAVE) {
            type = TYPE_BLOCK;
        }
    }

    public void setCanvasBackgroundColor(int color) {
        canvasBackgroundColor = color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bytes == null) {
            return;
        }

        canvas.drawColor(canvasBackgroundColor);

        rect.set(0, 0, getWidth(), getHeight());
        switch (type) {
            case TYPE_BLOCK:
                for (int i = 0; i < bytes.length - 1; i++) {
                    float left = getWidth() * i / (bytes.length - 1);
                    float top = rect.height() - (byte) (bytes[i + 1] + captureSizeRange)
                            * rect.height() / captureSizeRange;
                    float right = left + 1;
                    float bottom = rect.height();
                    canvas.drawRect(left, top - rect.height() / 2 + (bottom - top) / 2,
                            right, bottom - rect.height() / 2 + (bottom - top) / 2, paint);
                }
                break;
            case TYPE_BAR:
                for (int i = 0; i < bytes.length - 1; i += 18) {
                    float left = rect.width() * i / (bytes.length - 1);
                    float top = rect.height() - (byte) (bytes[i + 1] + captureSizeRange)
                            * rect.height() / captureSizeRange;
                    float right = left + 6;
                    float bottom = rect.height();
                    canvas.drawRect(left, top, right, bottom, paint);
                }
                break;
            case TYPE_WAVE:
                if (points == null || points.length < bytes.length * 4) {
                    points = new float[bytes.length * 4];
                }
                for (int i = 0; i < bytes.length - 1; i += waveStep) {
                    points[i * 4] = rect.width() * i / (bytes.length - 1);
                    points[i * 4 + 1] = (rect.height() / 2)
                            + ((byte) (bytes[i] + captureSizeRange)) * captureSizeRange
                            / (rect.height() / 2);
                    points[i * 4 + 2] = rect.width() * (i + 1)
                            / (bytes.length - 1);
                    points[i * 4 + 3] = (rect.height() / 2)
                            + ((byte) (bytes[i + 1] + captureSizeRange)) * captureSizeRange
                            / (rect.height() / 2);
                }
                canvas.drawLines(points, paint);
                break;
        }
    }
}
