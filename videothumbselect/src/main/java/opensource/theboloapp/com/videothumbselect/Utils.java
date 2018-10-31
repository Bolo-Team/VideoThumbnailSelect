package opensource.theboloapp.com.videothumbselect;

import android.content.res.Resources;
import android.util.TypedValue;

public class Utils {

    public static int dpToPixels(float dp) {
        Resources r = Resources.getSystem();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static float pixelToDp(int pixel) {
        Resources r = Resources.getSystem();
        return (float) pixel / r.getDisplayMetrics().density;
    }

}
