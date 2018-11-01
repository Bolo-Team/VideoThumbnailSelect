package opensource.theboloapp.com.videothumbselect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import opensource.theboloapp.com.videothumbselect.widgets.TimelineSeekView;
import opensource.theboloapp.com.videothumbselect.widgets.VideoTimelineView;

public class ChooseThumbnailActivity extends AppCompatActivity implements
        VideoTimelineView.PreparedListener,
        TimelineSeekView.ThumbPositionListener {

    public static final String INTENT_EXTRA_CONFIGURATION = "configuration";

    public static final String INTENT_RESULT_EXTRA_THUMB_POSITION = "result_thumb_position";
    public static final String INTENT_RESULT_EXTRA_THUMB_BITMAP_FILE_URI = "result_thumb_bitmap_file_uri";

    private Configuration configuration;

    private String videoPath;

    private VideoTimelineView videoTimelineView;
    private TimelineSeekView timelineSeekView;

    private ProgressBar progressBar;

    private long finalThumbPosition = 0;
    private Bitmap finalThumbBitmap;

    private Button doneButton;

    private PlayerView thumbPreview;
    private ExoPlayer previewPlayer;

    private TextureView previewTextureView;

    private boolean isPreviewReady = false;
    private boolean isTimelineReady = false;

    private boolean isSeekProcessing = false;

    private RelativeLayout progressWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_thumbnail);

        progressWrapper = findViewById(R.id.progress_wrapper_layout);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra(INTENT_EXTRA_CONFIGURATION)) {
            configuration = receivedIntent.getParcelableExtra(INTENT_EXTRA_CONFIGURATION);
            videoPath = configuration.getVideoSource();
        } else {
            Toast.makeText(this, "Set video path in intent", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        if (videoPath == null) {
            Toast.makeText(this, "Set video path in intent", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        progressBar = findViewById(R.id.progress_bar);

        thumbPreview = findViewById(R.id.thumb_preview);

        videoTimelineView = findViewById(R.id.video_timeline_view);

        timelineSeekView = findViewById(R.id.timeline_seek_view);

        doneButton = findViewById(R.id.done_button);
        doneButton.setVisibility(View.INVISIBLE);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressWrapper.setVisibility(View.VISIBLE);
                Disposable disposable = Observable
                        .create((ObservableOnSubscribe<Uri>) emitter -> {
                            finalThumbBitmap = previewTextureView.getBitmap();
                            emitter.onNext(Utils.createFileForBitmap(ChooseThumbnailActivity.this, finalThumbBitmap));
                        }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(fileUri -> {
                            progressWrapper.setVisibility(View.GONE);
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(INTENT_RESULT_EXTRA_THUMB_POSITION, finalThumbPosition);
                            resultIntent.putExtra(INTENT_RESULT_EXTRA_THUMB_BITMAP_FILE_URI, fileUri);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }, e -> {
                            progressWrapper.setVisibility(View.GONE);
                            setResult(RESULT_CANCELED);
                            finish();
                            e.printStackTrace();
                        });
            }
        });

        if (checkPermissions()) {
            initializeEverything();
        }

    }

    private void initializeEverything() {
        initializePreviewPlayer(videoPath);

        timelineSeekView.setThumbPositionListener(this);

        if (videoPath != null) {
            videoTimelineView.setPreparedListener(this);
            videoTimelineView.loadVideoAsync(videoPath);
        } else {
            Toast.makeText(this, "Set video path in intent", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
    }

    private int REQUEST_PERMISSION = 1069;

    private int checkCount = 0;

    private boolean checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
            return false;
        }
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeEverything();
            } else {
                checkCount++;
                if (checkCount == 1) {
                    Toast.makeText(this, "Please grant permission to select thumbnail", Toast.LENGTH_SHORT).show();
                    checkPermissions();
                } else {
                    Toast.makeText(this, "Thumbnail selection cancelled. Cannot proceed without permission", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }
    }

    private void initializePreviewPlayer(String videoPath) {

        Uri uri = Uri.fromFile(new File(videoPath));

        previewPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), new DefaultTrackSelector(), new DefaultLoadControl());

        previewPlayer.addListener(new Player.DefaultEventListener() {
            @Override
            public void onLoadingChanged(boolean isLoading) {
                super.onLoadingChanged(isLoading);
                if (!isLoading) {
                    isPreviewReady = true;
                    if (isTimelineReady && !isSeekProcessing) {
                        progressBar.setVisibility(View.GONE);
                        doneButton.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onSeekProcessed() {
                super.onSeekProcessed();
                if (isTimelineReady) {
                    progressBar.setVisibility(View.GONE);
                }
                isSeekProcessing = false;
            }
        });

        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };

        MediaSource videoSource = new ExtractorMediaSource.Factory(factory).createMediaSource(fileDataSource.getUri());

        previewPlayer.prepare(videoSource);

        thumbPreview.setPlayer(previewPlayer);

        previewTextureView = (TextureView) thumbPreview.getVideoSurfaceView();

        previewPlayer.seekTo(0);
    }

    @Override
    public void onPrepared() {
        isTimelineReady = true;
        if (isPreviewReady) {
            progressBar.setVisibility(View.GONE);
            doneButton.setVisibility(View.VISIBLE);
        }
        videoTimelineView.setVisibility(View.VISIBLE);
        timelineSeekView.setVisibility(View.VISIBLE);
        timelineSeekView.setThumbSize(videoTimelineView.getHeight());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (videoTimelineView != null) {
            videoTimelineView.releaseResources();
        }
        if (previewPlayer != null) {
            previewPlayer.release();
            previewPlayer = null;
        }
    }

    @Override
    public void thumbPositionChanged(float thumbPositionFactor) {
        if (previewPlayer != null) {
            previewPlayer.seekTo((long) (thumbPositionFactor * previewPlayer.getDuration()));
            progressBar.setVisibility(View.VISIBLE);
            isSeekProcessing = true;
        } else {
            initializePreviewPlayer(videoPath);
        }
        this.finalThumbPosition = (long) (thumbPositionFactor * videoTimelineView.getVideoLengthInMicros());
    }

}
