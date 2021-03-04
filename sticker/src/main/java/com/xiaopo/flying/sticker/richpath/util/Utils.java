package com.xiaopo.flying.sticker.richpath.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;

/**
 * Created by tarek360 on 7/1/17.
 */

public class Utils {

  public static float dpToPixel(Context context, float dp) {
    return dp * context.getResources().getDisplayMetrics().density;
  }

  public static float getDimenFromString(String value) {
    int end = value.charAt(value.length() - 3) == 'd' ? 3 : 2;
    return Float.parseFloat(value.substring(0, value.length() - end));
  }

  public static int getColorFromString(String value) {
    int color = Color.TRANSPARENT;
    if (value.length() == 7 || value.length() == 9) {
      color = Color.parseColor(value);
    } else if (value.length() == 4) {
      color = Color.parseColor("#"
          + value.charAt(1)
          + value.charAt(1)
          + value.charAt(2)
          + value.charAt(2)
          + value.charAt(3)
          + value.charAt(3));
    } else if (value.length() == 2) {
      color = Color.parseColor("#"
          + value.charAt(1)
          + value.charAt(1)
          + value.charAt(1)
          + value.charAt(1)
          + value.charAt(1)
          + value.charAt(1)
          + value.charAt(1)
          + value.charAt(1));
    }
    return color;
  }

  public static float convertSpToPx(Context context, float scaledPixels) {
    return scaledPixels * context.getResources().getDisplayMetrics().scaledDensity;
  }

  public static int dpToPx(float dp) {
    float density = Resources.getSystem().getDisplayMetrics().density;
    return Math.round(dp * density);
  }
}
