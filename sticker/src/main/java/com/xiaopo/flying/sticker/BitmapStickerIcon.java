package com.xiaopo.flying.sticker;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author wupanjie
 */
public class BitmapStickerIcon extends DrawableSticker implements StickerIconEvent {
  private final static String TAG = BitmapStickerIcon.class.getSimpleName();
  public static final float DEFAULT_ICON_RADIUS = 30f;
  public static final float DEFAULT_ICON_EXTRA_RADIUS = 10f;

  @IntDef({ LEFT_TOP, RIGHT_TOP, LEFT_BOTTOM, RIGHT_BOTOM,
          MID_BOTTOM, MID_TOP, MID_LEFT, MID_RIGHT,
          MID_BOTTOM_CENTER,MID_LEFT_CENTER,MID_RIGHT_CENTER,MID_TOP_CENTER,EXT_LEFT_TOP
  }) @Retention(RetentionPolicy.SOURCE)
  public @interface Gravity {

  }

  public static final int LEFT_TOP = 0;
  public static final int RIGHT_TOP = 1;
  public static final int LEFT_BOTTOM = 2;
  public static final int RIGHT_BOTOM = 3;
  public static final int MID_BOTTOM = 4;
  public static final int MID_LEFT = 5;
  public static final int MID_RIGHT = 6;
  public static final int MID_TOP= 7;
  public static final int MID_BOTTOM_CENTER = 8;
  public static final int MID_LEFT_CENTER = 9;
  public static final int MID_RIGHT_CENTER = 10;
  public static final int MID_TOP_CENTER= 11;
  public static final int EXT_LEFT_TOP = 12;

  private float iconRadius = DEFAULT_ICON_RADIUS;
  private float iconExtraRadius = DEFAULT_ICON_EXTRA_RADIUS;
  private float x;
  private float y;
  private float z;
  @Gravity private int position = LEFT_TOP;

  private StickerIconEvent iconEvent;


  public BitmapStickerIcon(Drawable drawable, @Gravity int gravity) {
    super(drawable);
//   this.context=context;
    this.position = gravity;
  }

  public BitmapStickerIcon(Drawable drawable, @Gravity int gravity, boolean shadowIcon){
    super(drawable, shadowIcon);
    this.position = gravity;


  }
  public void draw(Canvas canvas, Paint paint) {
    canvas.drawCircle(x, y, iconRadius, paint);
    super.draw(canvas);
  }

  public void drawT(Canvas canvas, Paint paint) {
    canvas.drawCircle(x, y, iconRadius, paint);
    super.draw(canvas);
  }

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  public float getZ() {
    return z;
  }

  public void setZ(float z) {
    this.z = z;
  }

  public float getIconRadius() {
    return iconRadius;
  }

  public void setIconRadius(float iconRadius) {
    this.iconRadius = iconRadius;
  }

  public float getIconExtraRadius() {
    return iconExtraRadius;
  }

  public void setIconExtraRadius(float iconExtraRadius) {
    this.iconExtraRadius = iconExtraRadius;
  }

  @Override public void onActionDown(StickerView stickerView, MotionEvent event) {
    Log.e(TAG, "onActionDown"+iconEvent);

    if (iconEvent != null) {
      iconEvent.onActionDown(stickerView, event);
    }
  }

  @Override public void onActionMove(StickerView stickerView, MotionEvent event) {
    Log.e(TAG, "onActionMove"+iconEvent);

    if (iconEvent != null) {
      iconEvent.onActionMove(stickerView, event);
    }
  }

  @Override public void onActionUp(StickerView stickerView, MotionEvent event) {
    Log.e(TAG, "onActionUp"+iconEvent);

    if (iconEvent != null) {
      iconEvent.onActionUp(stickerView, event);
    }
  }

  public StickerIconEvent getIconEvent() {
    return iconEvent;
  }

  public void setIconEvent(StickerIconEvent iconEvent) {
    this.iconEvent = iconEvent;
  }

  @Gravity public int getPosition() {
    return position;
  }

  public void setPosition(@Gravity int position) {
    this.position = position;
  }
}
