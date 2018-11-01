package opensource.theboloapp.com.videothumbnailselect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.net.URISyntaxException;

import opensource.theboloapp.com.videothumbselect.ChooseThumbnailActivity;
import opensource.theboloapp.com.videothumbselect.VideoThumbnailSelectHelper;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private ImageView imageView;

    private final int REQUEST_CODE_SELECT_VIDEO = 1009;
    private final int REQUEST_CODE_SELECT_THUMBNAIL = 1010;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        imageView = findViewById(R.id.image_view);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_CODE_SELECT_VIDEO);
            }
        });

    }

    public void startThumbSelectActivity(String videoSource) {
        VideoThumbnailSelectHelper videoThumbnailSelectHelper = new VideoThumbnailSelectHelper();
        videoThumbnailSelectHelper
                .setActivity(this)
                .setRequestCode(REQUEST_CODE_SELECT_THUMBNAIL)
                .setVideoSource(videoSource)
                .setNumThumbnails(15)
                .start();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SELECT_VIDEO) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                try {
                    String videoPath = PathUtil.getPath(this, selectedImageUri);
                    if (videoPath != null)
                        startThumbSelectActivity(videoPath);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == REQUEST_CODE_SELECT_THUMBNAIL) {
            if (resultCode == RESULT_OK) {
                Uri selectedThumbUri = data.getParcelableExtra(ChooseThumbnailActivity.INTENT_RESULT_EXTRA_THUMB_BITMAP_FILE_URI);

                if (selectedThumbUri != null) {
                    imageView.setImageURI(selectedThumbUri);
                }

            }
        }
    }
}
