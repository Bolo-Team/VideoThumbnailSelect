package opensource.theboloapp.com.videothumbselect.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import opensource.theboloapp.com.videothumbselect.Utils;

public class TimelineSeekView extends View {

    private Context context;

    private int width, height;

    private int thumbSize;

    private boolean allParamsSet = false;

    private Disposable renderDisposable;

    private Paint backgroundPaint, transparentPaint, handlePaint;

    private int windowSize;

    private int draggerWidth = 6, handleCircleRadius = Utils.dpToPixels(5), draggerOvershootHeight = Utils.dpToPixels(5);

    private float thumbPosition = 0;

    private float thumbDragPositionTolerance = Utils.dpToPixels(10);

    private boolean isDragging;

    private ThumbPositionListener thumbPositionListener;

    public interface ThumbPositionListener {
        void thumbPositionChanged(float thumbPositionFactor);
    }

    public void setThumbPositionListener(ThumbPositionListener thumbPositionListener) {
        this.thumbPositionListener = thumbPositionListener;
    }

    public void setThumbSize(int thumbSize) {
        this.thumbSize = thumbSize;
        windowSize = thumbSize;
        allParamsSet = true;
        invalidate();
    }

    public float getThumbPositionFactor() {
        return (float) thumbPosition / (float) width;
    }

    public TimelineSeekView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public TimelineSeekView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public TimelineSeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        width = resolveSizeAndState(minW, widthMeasureSpec, 1);

        final int minH = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        height = resolveSizeAndState(minH, heightMeasureSpec, 1);

        setMeasuredDimension(width, height);

    }

    private void init() {

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(0xAA000000);

        transparentPaint = new Paint();
        transparentPaint.setAntiAlias(true);
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        handlePaint = new Paint();
        handlePaint.setAntiAlias(true);
        handlePaint.setColor(0xFFFFFFFF);

    }

    private void startContinuousRender() {
        stopContinuousRender();
        renderDisposable = Observable.interval(16, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        invalidate();
                    }
                });

    }

    private void stopContinuousRender() {
        if (renderDisposable != null && !renderDisposable.isDisposed()) {
            renderDisposable.dispose();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!allParamsSet) {
            return;
        }

        canvas.drawRect(0, 0, getWidth(), thumbSize, backgroundPaint);

        canvas.drawRect(thumbPosition - windowSize / 2, 0, thumbPosition + windowSize / 2, thumbSize, transparentPaint);

        canvas.drawRect(thumbPosition - draggerWidth / 2, 0, thumbPosition + draggerWidth / 2, thumbSize + draggerOvershootHeight, handlePaint);

        canvas.drawCircle(thumbPosition, thumbSize + draggerOvershootHeight + handleCircleRadius, handleCircleRadius, handlePaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float touchX = event.getX();
        if (touchX > width) {
            touchX = width;
        }
        if (touchX < 0) {
            touchX = 0;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                isDragging = true;
                thumbPosition = touchX;
                startContinuousRender();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (isDragging) {
                    thumbPosition = touchX;
                    if (thumbPositionListener != null) {
                        thumbPositionListener.thumbPositionChanged(getThumbPositionFactor());
                    }
                    return true;
                }
                return false;
            }
            case MotionEvent.ACTION_UP: {
                if (isDragging) {
                    if (thumbPositionListener != null) {
                        thumbPositionListener.thumbPositionChanged(getThumbPositionFactor());
                    }
                    stopContinuousRender();
                    isDragging = false;
                    return true;
                }
                return false;
            }
        }

        return super.onTouchEvent(event);
    }
}
