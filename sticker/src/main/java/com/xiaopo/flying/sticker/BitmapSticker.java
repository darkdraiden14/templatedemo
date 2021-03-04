package com.xiaopo.flying.sticker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

public class BitmapSticker extends Sticker {

    private Bitmap mBbitmap;
    private Rect realBounds;
    private BitmapDrawable drawable;
    private final RectF imageRect;
    private boolean isOverlay;

    public BitmapSticker(Bitmap bitmap) {
       this(bitmap,false);
    }

    public BitmapSticker(Bitmap bitmap, boolean overlay){
        xDistance = 1;
        yDistance = 1;

        drawable = new BitmapDrawable(null, bitmap);
//        realBounds = new Rect(0, 0, width, height);
        realBounds = new Rect(0, 0, getWidth(), getHeight());
        imageRect = new RectF(0, 0, getFWidth(), getFHeight());
        isOverlay=overlay;
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.save();
        canvas.concat(getMatrix());
        canvas.clipRect(realBounds);
        canvas.drawBitmap(drawable.getBitmap(), null, imageRect, new Paint());
        canvas.restore();
    }


    @Override
    public int getWidth() {
        return (int) (drawable.getIntrinsicWidth() * xDistance * xScale);
    }

    @Override
    public int getHeight() {
        return (int) (drawable.getIntrinsicHeight() *yDistance* yScale);
    }

    @Override
    public float getFWidth() {
        return (drawable.getIntrinsicWidth() * xDistance * xScale);
    }

    @Override
    public float getFHeight() {
        return (drawable.getIntrinsicHeight() *yDistance* yScale);
    }

    @Override
    public void movedLeftHorizontally(float distance) {
        xScale = distance;
        moveHorizontally();
    }

    @Override
    public void movedRightHorizontally(float distance) {
        xScale = distance;
        moveHorizontally();
    }

    @Override
    public void movedTopVertically(float distance) {
        yScale = distance;
        moveVertically();
    }

    @Override
    public void movedBottomVertically(float distance) {
        yScale = distance;
        moveVertically();
    }

    @Override
    public void moveHorizontally() {
        realBounds.set(0,0, getWidth(), getHeight());
        float aspectRatio = drawable.getIntrinsicWidth()*1f/drawable.getIntrinsicHeight();
        float height = (drawable.getIntrinsicHeight()*xDistance*xScale);

        if(isOverlay){
            imageRect.set(0,0,getWidth(),getHeight());
        }
        else{
            if (height >= getFHeight()) {
                float top = ((height-getFHeight())/2f);//drawable.getIntrinsicHeight())/2);
                imageRect.set(0,-top, getFWidth(), height-top);
            } else {
                float width = (getFHeight()*aspectRatio);
                float left = ((width-getFWidth())/2f);//drawable.getIntrinsicWidth())/2);
                imageRect.set(-left,0,width-left,getFHeight());
            }
        }

    }

    @Override
    public void moveVertically() {
        realBounds.set(0,0, getWidth(), getHeight());
        float aspectRatio = drawable.getIntrinsicWidth()*1f/drawable.getIntrinsicHeight();
        float width = (drawable.getIntrinsicWidth()*yDistance*yScale);
        if(isOverlay){
            imageRect.set(0,0,getWidth(),getHeight());
        }
        else {
            if (width >= getWidth()) {
                float left = ((width-getWidth())/2f);//drawable.getIntrinsicHeight())/2);
                imageRect.set(-left,0, width-left,getHeight());
            } else
            {
                float height = (getWidth()/aspectRatio);
                float top = ((height-getHeight())/2f);//drawable.getIntrinsicWidth())/2);
                imageRect.set(0,-top,getWidth(),height-top);
            }
        }

    }

    @Override
    public void upRightHorizontally(float distance) {
        xDistance*=distance;
        xScale = 1;
        moveHorizontally();
    }

    @Override
    public void upLeftHorizontally(float distance) {
        xDistance*=distance;
        xScale = 1;
        moveHorizontally();
    }

    @Override
    public void upTopVertically(float distance) {
        yDistance*=distance;
        yScale = 1;
        moveVertically();
    }

    @Override
    public void upBottomVertically(float distance) {
        yDistance*=distance;
        yScale = 1;
        moveVertically();
    }

    @Override
    public Sticker setDrawable(@NonNull Drawable drawable) {
        this.drawable = (BitmapDrawable) drawable;
        return this;
    }

    @NonNull
    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @NonNull
    @Override
    public Sticker setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        drawable.setAlpha(alpha);
        return this;
    }


    @Override
    public void release() {
        super.release();
        if (drawable != null) {
            drawable = null;
        }
    }
}
