package opensource.theboloapp.com.videothumbselect.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class VideoTimelineView extends View {

    private Context context;

    private String videoPath;

    private int width, height;
    private int thumbSize;

    private long videoLengthInMicros;

    private int numThumbnails = 10;

    private MediaMetadataRetriever mediaMetadataRetriever;

    private long currTime = 0;

    private ArrayList<Bitmap> visibleThumbnails;

    private HashMap<Long, Bitmap> thumbnailMap;

    private PreparedListener preparedListener;

    private ThumbBitmapLoadedListener thumbBitmapLoadedListener;

    private boolean isPrepared = false;

    private Disposable thumbExtractDisposable;

    public interface PreparedListener {
        void onPrepared();
    }

    public interface ThumbBitmapLoadedListener {
        void onThumbLoaded(Bitmap bitmap, long finalThumbPosition);
    }

    public void setPreparedListener(PreparedListener preparedListener) {
        this.preparedListener = preparedListener;
    }

    public void setThumbBitmapLoadedListener(ThumbBitmapLoadedListener thumbBitmapLoadedListener) {
        this.thumbBitmapLoadedListener = thumbBitmapLoadedListener;
    }

    public int getThumbSize() {
        return thumbSize;
    }

    public void extractThumbForPosition(final float thumbFactor) {

        if (thumbExtractDisposable != null && !thumbExtractDisposable.isDisposed()) {
            thumbExtractDisposable.dispose();
        }

        thumbExtractDisposable = Observable
                .fromCallable(new Callable<Bitmap>() {
                    @Override
                    public Bitmap call() throws Exception {
                        if (mediaMetadataRetriever != null) {
                            return mediaMetadataRetriever.getFrameAtTime((long) (thumbFactor * videoLengthInMicros), MediaMetadataRetriever.OPTION_CLOSEST);
                        } else {
                            mediaMetadataRetriever = new MediaMetadataRetriever();
                            try {
                                mediaMetadataRetriever.setDataSource(new FileInputStream(videoPath).getFD());
                                return mediaMetadataRetriever.getFrameAtTime((long) (thumbFactor * videoLengthInMicros), MediaMetadataRetriever.OPTION_CLOSEST);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Failed to load file", Toast.LENGTH_SHORT).show();
                            }
                        }
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {
                        if (thumbBitmapLoadedListener != null) {
                            thumbBitmapLoadedListener.onThumbLoaded(bitmap, (long) (thumbFactor * videoLengthInMicros));
                        }
                    }
                });

    }

    public long getVideoLengthInMicros() {
        return videoLengthInMicros;
    }

    public VideoTimelineView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public VideoTimelineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public VideoTimelineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        mediaMetadataRetriever = new MediaMetadataRetriever();

        visibleThumbnails = new ArrayList<>();

        thumbnailMap = new HashMap<>();
    }

    public void loadVideoAsync(String videoPath) {
        this.videoPath = videoPath;
        try {
            mediaMetadataRetriever.setDataSource(new FileInputStream(videoPath).getFD());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to load file", Toast.LENGTH_SHORT).show();
        }

        Observable
                .fromCallable(new Callable<ArrayList<Bitmap>>() {
                    @Override
                    public ArrayList<Bitmap> call() throws Exception {

                        videoLengthInMicros = Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;

                        long thumbPositionStep = videoLengthInMicros / numThumbnails;

                        for (int i = 0; i < numThumbnails; i++) {
                            Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(i * thumbPositionStep, MediaMetadataRetriever.OPTION_CLOSEST);
                            float scaleFactor = (float) thumbSize / (float) bitmap.getWidth();
                            try {
                                bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scaleFactor), (int) (bitmap.getHeight() * scaleFactor), false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            visibleThumbnails.add(bitmap);
                        }

                        return visibleThumbnails;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<Bitmap>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ArrayList<Bitmap> bitmaps) {
                        isPrepared = true;
                        if (preparedListener != null) {
                            preparedListener.onPrepared();
                        }

                        invalidate();

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minW = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        width = resolveSizeAndState(minW, widthMeasureSpec, 1);

        thumbSize = width / numThumbnails;

        final int minH = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        height = resolveSizeAndState(minH, heightMeasureSpec, 1);

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isPrepared) {
            return;
        }

        for (int i = 0; i < visibleThumbnails.size(); i++) {
            Bitmap bitmap = visibleThumbnails.get(i);
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, i * ((float) width / (float) numThumbnails), 0, null);
            }
        }

    }

    public void releaseResources() {
        if (mediaMetadataRetriever != null) {
            mediaMetadataRetriever.release();
            mediaMetadataRetriever = null;
        }
    }

}
