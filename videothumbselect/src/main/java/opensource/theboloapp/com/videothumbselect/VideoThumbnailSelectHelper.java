package opensource.theboloapp.com.videothumbselect;

import android.app.Activity;
import android.content.Intent;

public class VideoThumbnailSelectHelper {

    private int requestCode = Defaults.CHOOSE_THUMBNAIL_REQUEST_CODE;

    private Configuration configuration = new Configuration();

    private Activity activity;

    public VideoThumbnailSelectHelper() {
    }

    public VideoThumbnailSelectHelper setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public VideoThumbnailSelectHelper setRequestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public VideoThumbnailSelectHelper setLayoutRESId(int resID) {
        configuration.setLayoutRESId(resID);
        return this;
    }

    public VideoThumbnailSelectHelper setNumThumbnails(int numThumbnails) {
        configuration.setNumThumbnails(numThumbnails);
        return this;
    }

    public VideoThumbnailSelectHelper setTimelineSeekViewHandleColor(int timelineSeekViewHandleColor) {
        configuration.setTimelineSeekViewHandleColor(timelineSeekViewHandleColor);
        return this;
    }

    public VideoThumbnailSelectHelper setTimelineSeekViewSliderWidth(int timelineSeekViewSliderWidth) {
        configuration.setTimelineSeekViewSliderWidth(timelineSeekViewSliderWidth);
        return this;
    }

    public VideoThumbnailSelectHelper setTimelineSeekViewSliderHandleRadius(int timelineSeekViewSliderHandleRadius) {
        configuration.setTimelineSeekViewSliderHandleRadius(timelineSeekViewSliderHandleRadius);
        return this;
    }

    public VideoThumbnailSelectHelper setTimelineSeekViewSliderOvershootHeight(int timelineSeekViewSliderOvershootHeight) {
        configuration.setTimelineSeekViewSliderOvershootHeight(timelineSeekViewSliderOvershootHeight);
        return this;
    }

    public VideoThumbnailSelectHelper setVideoSource(String videoSource) {
        configuration.setVideoSource(videoSource);
        return this;
    }

    public void start() {
        if (activity == null) {
            throw new RuntimeException("Context not specified for thumbnail select");
        }

        if (configuration.getVideoSource() == null || configuration.getVideoSource().equals("")) {
            throw new RuntimeException("Video Source not set for thumbnail select");
        }

        Intent intent = new Intent(activity, ChooseThumbnailActivity.class);
        intent.putExtra(ChooseThumbnailActivity.INTENT_EXTRA_CONFIGURATION, configuration);
        activity.startActivityForResult(intent, requestCode);

    }

}
