package com.xiaopo.flying.sticker;

import android.content.Context;
import android.content.res.Resources;

public class Utils {
    public static float convertSpToPx(Context context, float scaledPixels) {
        return scaledPixels * context.getResources().getDisplayMetrics().scaledDensity;
    }

    public static int dpToPx(float dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
