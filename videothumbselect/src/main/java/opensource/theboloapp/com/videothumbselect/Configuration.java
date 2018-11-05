package opensource.theboloapp.com.videothumbselect;

import android.os.Parcel;
import android.os.Parcelable;

public class Configuration implements Parcelable {

    private int layoutRESId = Defaults.LAYOUT_RESOURCE_ID;

    private int numThumbnails = Defaults.NUM_THUMBNAILS;

    private int timelineSeekViewHandleColor = Defaults.TIMELINE_SEEK_VIEW_HANDLE_COLOR;
    private int timelineSeekViewSliderWidth = Defaults.TIMELINE_SEEK_VIEW_SLIDER_WIDTH_IN_DP;
    private int timelineSeekViewSliderHandleRadius = Defaults.TIMELINE_SEEK_VIEW_SLIDER_HANDLE_RADIUS_IN_DP;
    private int timelineSeekViewSliderOvershootHeight = Defaults.TIMELINE_SEEK_VIEW_SLIDER_OVERSHOOT_HEIGHT_IN_DP;

    private String videoSource;

    public Configuration() {
    }

    protected Configuration(Parcel in) {
        layoutRESId = in.readInt();
        numThumbnails = in.readInt();
        timelineSeekViewHandleColor = in.readInt();
        timelineSeekViewSliderWidth = in.readInt();
        timelineSeekViewSliderHandleRadius = in.readInt();
        timelineSeekViewSliderOvershootHeight = in.readInt();
        videoSource = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(layoutRESId);
        dest.writeInt(numThumbnails);
        dest.writeInt(timelineSeekViewHandleColor);
        dest.writeInt(timelineSeekViewSliderWidth);
        dest.writeInt(timelineSeekViewSliderHandleRadius);
        dest.writeInt(timelineSeekViewSliderOvershootHeight);
        dest.writeString(videoSource);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Configuration> CREATOR = new Creator<Configuration>() {
        @Override
        public Configuration createFromParcel(Parcel in) {
            return new Configuration(in);
        }

        @Override
        public Configuration[] newArray(int size) {
            return new Configuration[size];
        }
    };

    public Configuration setLayoutRESId(int resID) {
        this.layoutRESId = resID;
        return this;
    }

    public Configuration setNumThumbnails(int numThumbnails) {
        this.numThumbnails = numThumbnails;
        return this;
    }

    public Configuration setTimelineSeekViewHandleColor(int timelineSeekViewHandleColor) {
        this.timelineSeekViewHandleColor = timelineSeekViewHandleColor;
        return this;
    }

    public Configuration setTimelineSeekViewSliderWidth(int timelineSeekViewSliderWidth) {
        this.timelineSeekViewSliderWidth = timelineSeekViewSliderWidth;
        return this;
    }

    public Configuration setTimelineSeekViewSliderHandleRadius(int timelineSeekViewSliderHandleRadius) {
        this.timelineSeekViewSliderHandleRadius = timelineSeekViewSliderHandleRadius;
        return this;
    }

    public Configuration setTimelineSeekViewSliderOvershootHeight(int timelineSeekViewSliderOvershootHeight) {
        this.timelineSeekViewSliderOvershootHeight = timelineSeekViewSliderOvershootHeight;
        return this;
    }

    public Configuration setVideoSource(String videoSource) {
        this.videoSource = videoSource;
        return this;
    }

    public int getLayoutRESId() {
        return layoutRESId;
    }

    public int getNumThumbnails() {
        return numThumbnails;
    }

    public int getTimelineSeekViewHandleColor() {
        return timelineSeekViewHandleColor;
    }

    public int getTimelineSeekViewSliderWidth() {
        return timelineSeekViewSliderWidth;
    }

    public int getTimelineSeekViewSliderHandleRadius() {
        return timelineSeekViewSliderHandleRadius;
    }

    public int getTimelineSeekViewSliderOvershootHeight() {
        return timelineSeekViewSliderOvershootHeight;
    }

    public String getVideoSource() {
        return videoSource;
    }

    public static Creator<Configuration> getCREATOR() {
        return CREATOR;
    }
}
