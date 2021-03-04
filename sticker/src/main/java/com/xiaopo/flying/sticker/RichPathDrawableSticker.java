package com.xiaopo.flying.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.xiaopo.flying.sticker.Sticker;
import com.xiaopo.flying.sticker.richpath.RichPathDrawable;

public class RichPathDrawableSticker  extends Sticker {

    private final static String TAG = RichPathDrawableSticker.class.getSimpleName();

    private RichPathDrawable drawable;
    private Rect realBounds;
    private final Rect imageRect;
    protected boolean shadowIcon = false;
    private Paint shadowPaint;

    public RichPathDrawableSticker(RichPathDrawable drawable) {
        this.drawable = drawable;
        xDistance = 1;
        yDistance = 1;
        realBounds = new Rect(0, 0, drawable.getWidth(), drawable.getHeight());
        imageRect = new Rect(0, 0, drawable.getWidth(), drawable.getHeight());

        drawable.setBounds(imageRect);
        drawable.invalidateSelf();
    }

    public RichPathDrawableSticker(RichPathDrawable drawable, boolean shadowIcon) {
        this(drawable);
        this.shadowIcon = shadowIcon;
        if (this.shadowIcon) {
            shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            shadowPaint.setStyle(Paint.Style.FILL);
            shadowPaint.setAntiAlias(true);
            shadowPaint.setShadowLayer(30, 0, 0, Color.GRAY);
        }
    }


    @NonNull
    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public RichPathDrawableSticker setDrawable(@NonNull Drawable drawable) {
        this.drawable = (RichPathDrawable) drawable;
        return this;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.concat(getMatrix());
        drawable.draw(canvas);
        canvas.restore();
    }

    @NonNull
    @Override
    public RichPathDrawableSticker setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        drawable.setAlpha(alpha);
        return this;
    }

    @Override
    public int getWidth() {


        Log.d(TAG,"getWidth()"+drawable.getDrawableWidth());
        return (int) (drawable.getDrawableWidth() * xDistance * xScale);
    }

    @Override
    public int getHeight() {
        Log.d(TAG,"getHeight()"+drawable.getDrawableHeight());

        return (int) (drawable.getDrawableHeight() * yDistance * yScale);
    }

    @Override
    public float getFWidth() {
        Log.d(TAG,"getFWidth()");

        return (drawable.getDrawableWidth() * xDistance * xScale);
    }

    @Override
    public float getFHeight() {
        Log.d(TAG,"getFHeight()");

        return (drawable.getDrawableHeight() * yDistance * yScale);
    }


    @Override
    public void release() {
        super.release();
        if (drawable != null) {
            drawable = null;
        }
    }

    @Override
    public void movedLeftHorizontally(float distance) {
        Log.d(TAG,"movedLeftHorizontally()");

        xScale = distance;
        moveHorizontally();
    }

    @Override
    public void movedTopVertically(float distance) {
        Log.d(TAG,"movedTopVertically()");

        yScale = distance;
        moveVertically();
    }

    @Override
    public void movedRightHorizontally(float distance) {
        Log.d(TAG,"movedRightHorizontally()");

        xScale = distance;
        moveHorizontally();
    }

    @Override
    public void movedBottomVertically(float distance) {
        Log.d(TAG,"movedBottomVertically()");

        yScale = distance;
        moveVertically();
    }

    @Override
    public void moveHorizontally() {
        imageRect.set(0,0, getWidth(), getHeight());
        drawable.setBounds(imageRect);
    }

    @Override
    public void moveVertically() {
        imageRect.set(0,0, getWidth(), getHeight());
        drawable.setBounds(imageRect);
    }

    @Override
    public void upRightHorizontally(float distance) {
        Log.d(TAG,"upRightHorizontally()");

        xDistance *= distance;
        xScale *= distance;
        moveHorizontally();
    }

    public void upLeftHorizontally(float distance) {
        Log.d(TAG,"upLeftHorizontally()");

        xDistance *= distance;
        yScale *= distance;
        moveHorizontally();
    }

    @Override
    public void upTopVertically(float distance) {
        Log.d(TAG,"upTopVertically()");

        yDistance *= distance;
        yScale = 1;
        moveVertically();
    }

    @Override
    public void upBottomVertically(float distance) {
        Log.d(TAG,"upBottomVertically()");

        yDistance *= distance;
        yScale = 1;
        moveVertically();
    }

}
